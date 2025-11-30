package io.sneery.detominer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/detominer.json");
    public static DetominerConfig config;

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            config = new DetominerConfig();
            saveConfig();
            Detominer.LOGGER.info("[{} v{}] Config file created at: {}", Detominer.MOD_NAME, Detominer.MOD_VERSION, CONFIG_FILE.getAbsolutePath());
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, DetominerConfig.class);
            } catch (IOException e) {
                config = new DetominerConfig();
            }
        }
    }

    public static void saveConfig() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    Detominer.LOGGER.warn("[{} v{}] Failed to create config directory: {}", Detominer.MOD_NAME, Detominer.MOD_VERSION, parent.getAbsolutePath());
                }
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {}
    }

    public static void startWatcher() {
        Thread watcherThread = new Thread(() -> {
            Detominer.LOGGER.info("[{} v{}] Starting config file watcher for: {}", Detominer.MOD_NAME, Detominer.MOD_VERSION, CONFIG_FILE.getAbsolutePath());
            long[] lastReload = {0}; // Use array for mutability in lambda
            final long debounceMs = 500;
            while (true) {
                try (var watchService = CONFIG_FILE.getParentFile().toPath().getFileSystem().newWatchService()) {
                    CONFIG_FILE.getParentFile().toPath().register(watchService, java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        try {
                            var key = watchService.take();
                            for (var event : key.pollEvents()) {
                                var changed = (java.nio.file.Path) event.context();
                                if (changed.endsWith(CONFIG_FILE.getName())) {
                                    long now = System.currentTimeMillis();
                                    if (now - lastReload[0] > debounceMs) {
                                        lastReload[0] = now;
                                        Detominer.LOGGER.info("[{} v{}] Config file changed, reloading...", Detominer.MOD_NAME, Detominer.MOD_VERSION);
                                        loadConfig();
                                    }
                                }
                            }
                            key.reset();
                        } catch (Exception e) {
                            Detominer.LOGGER.error("[{} v{}] Config watcher error, restarting watcher...", Detominer.MOD_NAME, Detominer.MOD_VERSION, e);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Detominer.LOGGER.warn("[{} v{}] Config watcher stopped due to error, will retry in 5s: {}", Detominer.MOD_NAME, Detominer.MOD_VERSION, e.toString());
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                }
            }
        }, "Detominer-ConfigWatcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }
}
