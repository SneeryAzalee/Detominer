package io.sneery.detominer.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ModMenuIntegration implements ModMenuApi {
    private static final Logger LOGGER = Logger.getLogger(ModMenuIntegration.class.getName());

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            File configFile = new File("config/detominer.json");
            String os = System.getProperty("os.name").toLowerCase();
            String[] command;

            if (os.contains("win")) {
                command = new String[] { "explorer", configFile.getAbsolutePath() };
            } else if (os.contains("mac")) {
                command = new String[] { "open", configFile.getAbsolutePath() };
            } else if (os.contains("nux") || os.contains("nix")) {
                command = new String[] { "xdg-open", configFile.getAbsolutePath() };
            } else {
                LOGGER.warning("Unsupported OS for opening config file.");
                return parent;
            }

            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                LOGGER.severe("Failed to open config file: " + e.getMessage());
            }

            return parent;
        };
    }
}
