package org.mbari.vars.ui.commands;

import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.messages.ClearCommandManagerMsg;
import org.mbari.vars.ui.messages.RedoMsg;
import org.mbari.vars.ui.messages.UndoMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The CommandManager listens on the eventbus for 4 different kinds of classes: {@link Command},
 * {@link RedoMsg}, {@link UndoMsg}, {@link ClearCommandManagerMsg}.
 *
 * On a {@link Command} it will immediately execute the command's `apply()` method and add it
 * to the undo queue.
 *
 * ON a {@link UndoMsg}, it will call the commands `unapply()` method and add it to the redo queue
 *
 * On a {@link RedoMsg}, it will call the commands `apply()` method and add it to the undo queue.
 *
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
    private final UIToolBox toolBox = Initializer.getToolBox();

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
                            command.apply(toolBox);
                            inverseCommandList = undos;
                            break;
                        }
                        case UNDO: {
                            command.unapply(toolBox);
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

    public CommandManager() {
        thread = new Thread(runnable, getClass().getName());
        thread.setDaemon(true);
        thread.start();

        EventBus eventBus = toolBox.getEventBus();
        // New commands go in the pending queue
        eventBus.toObserverable()
                .ofType(Command.class)
                .map(cmd -> new CommandEvent(cmd, CommandEvent.State.DO))
                .forEach(pendingQueue::offer);

        eventBus.toObserverable()
                .ofType(UndoMsg.class)
                .forEach(u -> undo());

        eventBus.toObserverable()
                .ofType(RedoMsg.class)
                .forEach(u -> redo());

        eventBus.toObserverable()
                .ofType(ClearCommandManagerMsg.class)
                .forEach(u -> clear());
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

    private void clear() {
        undos.clear();
        redos.clear();
    }

}

class CommandEvent {

    public enum State {
        DO,
        UNDO
    }

    private final Command command;
    private final State state;

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