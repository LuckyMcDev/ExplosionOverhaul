// ExplosionConfig.java
package de.luckydev.explosionoverhaul.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ExplosionConfig {
    public boolean enabled = true;

    public int maxFallingBlocks = 50;
    public int spawnProbabilityPercent = 30;

    public int minHorizontalSpeedPercent = 30;
    public int maxHorizontalSpeedPercent = 70;

    public int minUpwardForcePercent = 10;
    public int maxUpwardForcePercent = 30;

    public boolean allowUnbreakableBlocks = false;
    public boolean randomRotation = true;

    public boolean enableScreenShake = true;
    public int shakeIntensityPercent = 100;
    public int maxShakeRadius = 20;
    public int minShakeDurationTicks = 20;
    public int maxShakeDurationTicks = 60;
    public int shakeRotationMultiplier = 200;
    public boolean shakeScalesWithPower = true;

    public boolean debugLogging = false;

    // Optional clamping logic
    public void clamp() {
        maxFallingBlocks = clamp(maxFallingBlocks, 1, 200);
        spawnProbabilityPercent = clamp(spawnProbabilityPercent, 0, 100);

        minHorizontalSpeedPercent = clamp(minHorizontalSpeedPercent, 0, 200);
        maxHorizontalSpeedPercent = clamp(maxHorizontalSpeedPercent, 0, 200);
        if (maxHorizontalSpeedPercent < minHorizontalSpeedPercent) {
            int tmp = maxHorizontalSpeedPercent;
            maxHorizontalSpeedPercent = minHorizontalSpeedPercent;
            minHorizontalSpeedPercent = tmp;
        }

        minUpwardForcePercent = clamp(minUpwardForcePercent, 0, 100);
        maxUpwardForcePercent = clamp(maxUpwardForcePercent, 0, 100);
        if (maxUpwardForcePercent < minUpwardForcePercent) {
            int tmp = maxUpwardForcePercent;
            maxUpwardForcePercent = minUpwardForcePercent;
            minUpwardForcePercent = tmp;
        }

        shakeIntensityPercent = clamp(shakeIntensityPercent, 0, 500);
        maxShakeRadius = clamp(maxShakeRadius, 1, 50);
        minShakeDurationTicks = clamp(minShakeDurationTicks, 10, 1000);
        maxShakeDurationTicks = clamp(maxShakeDurationTicks, 10, 1000);
        if (maxShakeDurationTicks < minShakeDurationTicks) {
            int tmp = maxShakeDurationTicks;
            maxShakeDurationTicks = minShakeDurationTicks;
            minShakeDurationTicks = tmp;
        }
        shakeRotationMultiplier = clamp(shakeRotationMultiplier, 1, 1000);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/explosion-overhaul.json");

    public static ExplosionConfig load() {
        if (!CONFIG_FILE.exists()) {
            ExplosionConfig defaultConfig = new ExplosionConfig();
            defaultConfig.save(); // write defaults
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ExplosionConfig config = GSON.fromJson(reader, ExplosionConfig.class);
            config.clamp();
            return config;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read explosion-overhaul config", e);
        }
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write explosion-overhaul config", e);
        }
    }
}
