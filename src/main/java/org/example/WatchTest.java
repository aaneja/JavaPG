package org.example;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CompletableFuture;

public class WatchTest
{
    public static void main(String[] args)
            throws InterruptedException
    {
        registerWatcherAtPluginStartup();
        // The plugin can go and finish registering with Presto and be done
        Thread.sleep(Long.MAX_VALUE);
    }

    private static void registerWatcherAtPluginStartup()
    {
        CompletableFuture.supplyAsync(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                // Register interest in the parent directory of the file
                Path path = Paths.get("/tmp/config.file");
                Path parentDir = path.getParent();
                parentDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();

                    // Process the event(s)
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.endsWith(path.getFileName())) {
                            System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                            System.out.println("Updated file content:");
                            Files.readAllLines(path).forEach(System.out::println);

                            //goUpdateConfigs()
                        }
                    }

                    // Reset the WatchKey
                    key.reset();
                }
            }
            catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
