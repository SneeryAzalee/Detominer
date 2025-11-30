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
                    Detominer.LOGGER.warn("Failed to create config directory: {}", parent.getAbsolutePath());
                }
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {}
    }
}
