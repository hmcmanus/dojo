package com.mcmanus.samples;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatchDog {

    private String watchDirectory;
    private ConcurrentHashMap<String, String> fileCache = new ConcurrentHashMap<>();
    private ExecutorService fileHandlerService = Executors.newFixedThreadPool(1);

    public FileWatchDog(String watchDirectory) {
        this.watchDirectory = watchDirectory;
        try {
            processEventsFromFileSystem();
        } catch (IOException io) {
            // Log something here I suppose
        }
    }

    public String getFileName(String fileName) {
        String fileInDirectory = fileCache.get(fileName);
        if (fileInDirectory == null) {
            File searchFile = new File(this.watchDirectory + "/" + fileName + ".txt");
            if (searchFile != null && searchFile.exists()) {
                fileInDirectory = searchFile.getName();
                fileCache.putIfAbsent(fileName, fileInDirectory);
            }
        }
        return fileInDirectory;
    }

    private void processEventsFromFileSystem() throws IOException {
        final WatchService watchService = FileSystems.getDefault().newWatchService();
        Path directoryToWatch = Paths.get(watchDirectory);
        // For this use case we're only interested in the modify and delete meaning that the request for the file name
        // will populate the cache when necessarys
        directoryToWatch.register(watchService, ENTRY_DELETE, ENTRY_MODIFY);

        Runnable worker = new Runnable() {
            @Override
            public void run() {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    // log the interruption and return
                    return;
                }
                for (WatchEvent<?> watchEvent: key.pollEvents()) {
                    WatchEvent.Kind kind = watchEvent.kind();

                    if (kind == OVERFLOW){
                        continue;
                    }
                    WatchEvent<Path> event = (WatchEvent<Path>)watchEvent;
                    Path fileName = event.context();
                    // Need to strip the name
                    String fileWithoutExtension = FilenameUtils.removeExtension(fileName.toString());
                    if (kind == ENTRY_DELETE || kind == ENTRY_MODIFY) {
                        fileCache.remove(fileWithoutExtension);
                    }
                }
                if (!key.reset()) {
                    // Log some problem here with the key that has been fired
                }
            }
        };

        fileHandlerService.submit(worker);
    }
}
