package de.luckydev.explosionoverhaul;

import de.luckydev.explosionoverhaul.config.ExplosionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExplosionOverhaul {
    public static final String MOD_ID = "explosionoverhaul";
    public static ExplosionConfig CONFIG;
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        CONFIG = ExplosionConfig.load();
    }
}