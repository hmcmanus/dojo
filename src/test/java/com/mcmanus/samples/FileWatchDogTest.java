package com.mcmanus.samples;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FileWatchDogTest {

    @Test
    public void shouldGiveNameOfFileAsString() throws Throwable {
        File watchFile = new File(Thread.currentThread().getContextClassLoader().getResource("watchedFile1.txt").getFile());
        FileWatchDog fileWatchDog = new FileWatchDog(watchFile.getParent());

        String fileName = fileWatchDog.getFileName("watchedFile1");

        assertEquals("Returned File name", "watchedFile1.txt", fileName);
    }

    @Test
    public void shouldReloadFileChangeFromFileSystem() throws Throwable {
        // Get the file from the Service and make sure it's cached
        File watchFile = new File(Thread.currentThread().getContextClassLoader().getResource("watchFileNotModified.txt").getFile());
        FileWatchDog fileWatchDog = new FileWatchDog(watchFile.getParent());
        String fileName = fileWatchDog.getFileName("watchFileNotModified");

        assertEquals("Returned File name", "watchFileNotModified.txt", fileName);

        // Modify the file's name and then ask for the old file and make sure it's not cached
        watchFile.renameTo(new File(watchFile.getParent() + "/" + "watchFileModified.txt"));
        // Need to give it a second here for the other thread to clean the cache
        Thread.sleep(1000);

        assertNull("Returned File name", fileWatchDog.getFileName("watchFileNotModified"));

        // Then ask for the new file name and make sure it's available
        assertEquals("Returned File name", "watchFileModified.txt", fileWatchDog.getFileName("watchFileModified"));

    }

    @After
    public void cleanUpFileName() {
        try {
            File watchFile = new File(Thread.currentThread().getContextClassLoader().getResource("watchFileModified.txt").getFile());
            if (watchFile != null && watchFile.exists()) {
                watchFile.renameTo(new File(watchFile.getParent() + "/" + "watchFileNotModified.txt"));
            }
        } catch (Exception e) {
            // Don't care cause the file can't be found anyway
        }
    }
}
