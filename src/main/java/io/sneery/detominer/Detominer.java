package io.sneery.detominer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detominer implements ModInitializer {
    public static final String MOD_ID = "detominer";
    public static final String MOD_NAME = "Detominer";
    public static final String MOD_VERSION = "4.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[{} v{}] Mod initializing...", MOD_NAME, MOD_VERSION);

        ConfigManager.loadConfig();
        CubeBreaker.register();
    }
}
