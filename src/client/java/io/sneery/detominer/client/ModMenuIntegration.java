package io.sneery.detominer.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.util.Util;

import java.io.File;
import java.util.logging.Logger;

public class ModMenuIntegration implements ModMenuApi {
    private static final Logger LOGGER = Logger.getLogger(ModMenuIntegration.class.getName());

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            File configFile = new File("config/detominer.json");
            try {
                Util.getOperatingSystem().open(configFile);
            } catch (Exception e) {
                LOGGER.severe("Failed to open config file: " + e.getMessage());
            }
            return parent;
        };
    }
}
