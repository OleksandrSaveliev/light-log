package com.tmdna;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Lightweight recursive watcher for development resource reloads.
 */
public class ResourceWatcher implements AutoCloseable {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final Consumer<Path> onChange;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "resource-watcher");
        t.setDaemon(true);
        return t;
    });

    /**
     * Constructs a new ResourceWatcher.
     *
     * @param dir      The directory to watch.
     * @param onChange The consumer to call when a resource changes.
     * @throws IOException If an I/O error occurs.
     */
    public ResourceWatcher(Path dir, Consumer<Path> onChange) throws IOException {
        this.watcher = dir.getFileSystem().newWatchService();
        this.onChange = onChange;
        registerAll(dir);
        startLoop();
    }

    /**
     * Closes the watcher service.
     *
     * @throws Exception If an error occurs.
     */
    @Override
    public void close() throws Exception {
        executor.shutdownNow();
        watcher.close();
    }

    /**
     * Registers a directory with the watcher service.
     *
     * @param dir The directory to register.
     * @throws IOException If an I/O error occurs.
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        keys.put(key, dir);
    }

    /**
     * Registers all subdirectories of a given directory with the watcher service.
     *
     * @param start The starting directory.
     * @throws IOException If an I/O error occurs.
     */
    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Starts the watcher loop.
     */
    private void startLoop() {
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    Path dir = keys.get(key);
                    if (dir == null) {
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == OVERFLOW) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path changed = dir.resolve(ev.context());
                        onChange.accept(changed);
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        keys.remove(key);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
