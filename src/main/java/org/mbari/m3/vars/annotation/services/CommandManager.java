package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.commands.Command;
import org.mbari.m3.vars.annotation.commands.Redo;
import org.mbari.m3.vars.annotation.commands.Undo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The CommandManager listens on the eventbus for 3 different kinds of classes: {@link Command},
 * {@link Redo} and {@link Undo}
 * @author Brian Schlining
 * @since 2017-05-10T09:00:00
 */
public class CommandManager {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final int maxUndos = 25;
    private final BlockingQueue<CommandEvent> pendingQueue = new LinkedBlockingQueue<>();
    private final Deque<CommandEvent> undos = new LinkedBlockingDeque<>(maxUndos);
    private final Deque<CommandEvent> redos = new LinkedBlockingDeque<>(maxUndos);
    private final Thread thread;

    private final Runnable runnable = () -> {
        while (true) {
            CommandEvent commandEvent = null;
            try {
                commandEvent = pendingQueue.poll(3600L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // TODO handle error via event
            }
            if (commandEvent != null) {
                Command command = commandEvent.getCommand();
                try {
                    log.debug("Executing Command: " + commandEvent.getState() + " - " +
                            command.getDescription());

                    // Execute the command (can be DO or UNDO operation)
                    Deque<CommandEvent> inverseCommandList = null;
                    switch (commandEvent.getState()) {
                        case DO: {
                            command.apply();
                            inverseCommandList = undos;
                            break;
                        }
                        case UNDO: {
                            command.unapply();
                            inverseCommandList = redos;
                            break;
                        }
                    }

                    // Put the command
                    int size = inverseCommandList.size();
                    if (size >= maxUndos) {
                        inverseCommandList.pollFirst();
                    }
                    inverseCommandList.offerLast(commandEvent);
                } catch (Exception e) {
                    // TODO handle error via event
                }
            }
        }
    };

    public CommandManager(EventBus eventBus) {
        thread = new Thread(runnable, getClass().getName());
        thread.setDaemon(true);
        thread.start();

        // New commands go in the pending queue
        eventBus.toObserverable()
                .ofType(Command.class)
                .map(cmd -> new CommandEvent(cmd, CommandEvent.State.DO))
                .forEach(pendingQueue::offer);

        eventBus.toObserverable()
                .ofType(Undo.class)
                .forEach(u -> undo());

        eventBus.toObserverable()
                .ofType(Redo.class)
                .forEach(u -> redo());
    }

    private void redo() {
        if (redos.size() > 0) {
            CommandEvent commandEvent = redos.removeLast();
            CommandEvent newCommandEvent = new CommandEvent(commandEvent.getCommand(), CommandEvent.State.DO);
            pendingQueue.offer(newCommandEvent);
        }
    }

    private void undo() {
        if (undos.size() > 0) {
            CommandEvent commandEvent = undos.removeLast();
            CommandEvent newCommandEvent = new CommandEvent(commandEvent.getCommand(), CommandEvent.State.UNDO);
            pendingQueue.offer(newCommandEvent);
        }
    }

}

class CommandEvent {

    public static enum State {
        DO,
        UNDO
    }

    private final Command command;
    private final State state;

    public CommandEvent(Command command) {
        this(command, State.DO);
    }

    public CommandEvent(Command command, State state) {
        this.command = command;
        this.state = state;
    }

    public Command getCommand() {
        return command;
    }

    public State getState() {
        return state;
    }
}