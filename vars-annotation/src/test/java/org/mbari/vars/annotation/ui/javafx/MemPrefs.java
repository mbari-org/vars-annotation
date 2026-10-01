package org.mbari.vars.annotation.ui.javafx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.AbstractPreferences;
import javafx.application.Platform;

/** In-memory prefs whose child listing can be held up, like a slow remote service. */
public class MemPrefs extends AbstractPreferences {
    private final Map<String, String> values = new HashMap<>();
    private final Map<String, MemPrefs> children = new HashMap<>();
    private final AtomicReference<CountDownLatch> gate;
    private final AtomicBoolean fxAccess;

    public MemPrefs(MemPrefs parent, String name, AtomicReference<CountDownLatch> gate) {
        super(parent, name);
        this.gate = gate;
        this.fxAccess = parent == null ? new AtomicBoolean(false) : parent.fxAccess;
    }

    /**
     * Set if any node in this tree was read on the JavaFX thread. A real remote store would
     * block the UI there. Reset it with {@code set(false)}.
     */
    public AtomicBoolean fxThreadAccess() {
        return fxAccess;
    }

    private void noteAccess() {
        if (Platform.isFxApplicationThread()) {
            fxAccess.set(true);
        }
    }

    @Override protected void putSpi(String key, String value) { values.put(key, value); }
    @Override protected String getSpi(String key) { noteAccess(); return values.get(key); }
    @Override protected void removeSpi(String key) { values.remove(key); }
    @Override protected void removeNodeSpi() { children.clear(); values.clear(); }
    @Override protected String[] keysSpi() { return values.keySet().toArray(String[]::new); }

    @Override
    protected String[] childrenNamesSpi() {
        noteAccess();
        var latch = gate.get();
        if (latch != null) {
            try {
                latch.await(10, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return children.keySet().toArray(String[]::new);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        noteAccess();
        return children.computeIfAbsent(name, n -> new MemPrefs(this, n, gate));
    }

    @Override protected void syncSpi() { }
    @Override protected void flushSpi() { }
}
