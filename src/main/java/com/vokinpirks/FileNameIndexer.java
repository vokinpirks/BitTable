package com.vokinpirks;

import com.vokinpirks.enums.FileFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.System.currentTimeMillis;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static java.util.stream.Collectors.toSet;

public class FileNameIndexer {

    private static final Set<String> AUDIO_FILES_EXTENSIONS = FileFormat.INPUT_FORMATS.stream()
            .map(FileFormat::getExtensions)
            .flatMap(Collection::stream)
            .collect(toSet());

    private final Map<Path, WatchKey> watchableDirectories = new ConcurrentHashMap<>(128);

    private final Map<String, String> filenameToPath = new ConcurrentHashMap<>(1024);

    private final ConsoleLogger logger;

    private final WatchService watchService;

    public FileNameIndexer(ConsoleLogger logger) {
        this.logger = logger;
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            this.watchService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkForChanges() {
        WatchKey wk;
        while ( (wk = watchService.poll()) != null) {
            final Path watchable = (Path) wk.watchable();

            for (WatchEvent<?> event : wk.pollEvents()) {
                final Path fullPath = watchable.resolve((Path) event.context());
                final String filename = fullPath.getFileName().toString();

                if (event.kind().equals(ENTRY_CREATE)) {
                    if (fullPath.toFile().isDirectory()) {
                        // a new directory has been created, scan it and watch it for further changes
                        logger.debug("Indexing new directory: %s", fullPath);
                        indexDirectory(fullPath);
                    } else {
                        // a new file has been created, index it if needed
                        if (isAudioFile(filename)) {
                            if (filenameToPath.put(filename, fullPath.toString()) == null) {
                                logger.debug("New file added to the index: %s", fullPath);
                            }
                        }
                    }
                } else if (event.kind().equals(ENTRY_DELETE)) {
                    if (watchableDirectories.containsKey(fullPath)) {
                        // a directory has been deleted
                        removeDirectory(fullPath);
                    } else {
                        // a file has been deleted
                        if (filenameToPath.remove(filename) != null) {
                            logger.debug("File deleted from the index: %s", fullPath);
                        }
                    }
                }
            }

            wk.reset();
        }
    }

    public void indexDirectory(Path directory) {
        final DirectoryTraversalTask rootTask = new DirectoryTraversalTask(directory,
                this.filenameToPath::put,
                this::watchDirectoryForChanges
        );

        logger.info("Indexing of the %s directory has been started", directory);
        final long timestamp = currentTimeMillis();
        supplyAsync(() -> commonPool().invoke(rootTask))
                .thenAccept(fileCount -> logger.info("Found %s files in the directory '%s'. Indexing took %s ms",
                        fileCount, directory, currentTimeMillis() - timestamp)
                );
    }

    public void clearIndex() {
        for (WatchKey value : watchableDirectories.values()) {
            value.cancel();
        }

        watchableDirectories.clear();
        filenameToPath.clear();
    }

    public String getFullPath(final String fileName) {
        return filenameToPath.get(fileName);
    }

    public boolean doesContain(final String filename) {
        return filenameToPath.containsKey(filename);
    }

    private void removeDirectory(final Path directory) {
        final WatchKey watchKey = watchableDirectories.get(directory);

        if (watchKey == null) {
            return;
        }

        // so yeah, it's a full scan for now..
        for (Map.Entry<String, String> fileNameAndPath : filenameToPath.entrySet()) {
            if (Path.of(fileNameAndPath.getValue()).startsWith(directory)) {
                filenameToPath.remove(fileNameAndPath.getKey());
                logger.debug("File %s has been deleted from the index", fileNameAndPath.getValue());
            }
        }

        watchKey.cancel();
        watchableDirectories.remove(directory);
        logger.debug("The directory %s has been deleted from the index", directory);
    }

    private void watchDirectoryForChanges(final Path directory) {
        try {
            final WatchKey watchKey = directory.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
            watchableDirectories.put(directory, watchKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class DirectoryTraversalTask extends RecursiveTask<Integer> {
        private final Path dir;

        private final BiConsumer<String, String> fileCallback;

        private final Consumer<Path> directoryCallback;

        private DirectoryTraversalTask(
                Path dir,
                BiConsumer<String, String> fileCallback,
                Consumer<Path> directoryCallback
        ) {
            this.dir = dir;
            this.fileCallback = fileCallback;
            this.directoryCallback = directoryCallback;
        }

        @Override
        protected Integer compute() {
            final List<ForkJoinTask<Integer>> subTasks = new ArrayList<>();
            final var filesCount = new AtomicInteger();

            try (final var paths = Files.list(dir)) {
                paths.forEach(path -> {
                    final File file = path.toFile();
                    if (file.isDirectory()) {
                        subTasks.add(new DirectoryTraversalTask(file.toPath(), fileCallback, directoryCallback).fork());
                    } else if (file.isFile() && isAudioFile(path.toString())) {
                        fileCallback.accept(file.getName(), path.toString());
                        filesCount.incrementAndGet();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            directoryCallback.accept(dir);

            return filesCount.get() + subTasks.stream().mapToInt(ForkJoinTask::join).sum();
        }
    }

    private static boolean isAudioFile(final String filename) {
        final int dotIndex = filename.lastIndexOf('.');

        if (dotIndex == -1) {
            return false;
        }

        final String fileExtension = filename.substring(dotIndex).toLowerCase();
        return AUDIO_FILES_EXTENSIONS.contains(fileExtension);
    }
}
