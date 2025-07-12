package de.luckydev.explosionoverhaul.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.math.random.Random;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ExplosionConfig {
    public boolean enabled = true;

    // Debris settings
    public int maxFallingBlocks = 50;
    public int spawnProbabilityPercent = 30;
    public int minHorizontalSpeedPercent = 30;
    public int maxHorizontalSpeedPercent = 70;
    public int minUpwardForcePercent = 10;
    public int maxUpwardForcePercent = 30;
    public boolean allowUnbreakableBlocks = false;
    public boolean randomRotation = true;

    // Screen shake settings - much more reasonable values
    public boolean enableScreenShake = true;
    public float baseShakeStrength = 0.4f;        // Base shake strength (0.0 to 1.0)
    public float maxShakeStrength = 1.0f;         // Maximum shake strength
    public float shakeRadius = 20.0f;             // Radius in blocks where shake is felt
    public int minShakeDurationTicks = 10;        // Minimum shake duration
    public int maxShakeDurationTicks = 60;        // Maximum shake duration
    public boolean shakeScalesWithPower = true;   // Whether shake scales with explosion power
    public float shakeIntensityMultiplier = 1.0f; // Global multiplier for shake intensity

    public boolean debugLogging = false;

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

        // Clamp shake values to reasonable ranges
        baseShakeStrength = clampFloat(baseShakeStrength, 0.0f, 1.0f);
        maxShakeStrength = clampFloat(maxShakeStrength, 0.0f, 2.0f);
        shakeRadius = clampFloat(shakeRadius, 1.0f, 100.0f);
        shakeIntensityMultiplier = clampFloat(shakeIntensityMultiplier, 0.0f, 3.0f);

        minShakeDurationTicks = clamp(minShakeDurationTicks, 1, 200);
        maxShakeDurationTicks = clamp(maxShakeDurationTicks, 1, 200);
        if (maxShakeDurationTicks < minShakeDurationTicks) {
            int tmp = maxShakeDurationTicks;
            maxShakeDurationTicks = minShakeDurationTicks;
            minShakeDurationTicks = tmp;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/explosion-overhaul.json");

    public static ExplosionConfig load() {
        if (!CONFIG_FILE.exists()) {
            ExplosionConfig defaultConfig = new ExplosionConfig();
            defaultConfig.save();
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

    // Debris methods
    public double getSpawnProbability() {
        return spawnProbabilityPercent / 100.0;
    }

    public double getMinHorizontalSpeed() {
        return minHorizontalSpeedPercent / 100.0;
    }

    public double getMaxHorizontalSpeed() {
        return maxHorizontalSpeedPercent / 100.0;
    }

    public double getMinUpwardForce() {
        return minUpwardForcePercent / 100.0;
    }

    public double getMaxUpwardForce() {
        return maxUpwardForcePercent / 100.0;
    }

    public double getRandomHorizontalSpeed(Random random) {
        double min = getMinHorizontalSpeed();
        double max = getMaxHorizontalSpeed();
        return min + random.nextDouble() * (max - min);
    }

    public double getRandomUpwardForce(Random random) {
        double min = getMinUpwardForce();
        double max = getMaxUpwardForce();
        return min + random.nextDouble() * (max - min);
    }

    // Screen shake methods - much more reasonable calculations
    public int getShakeDuration(Random random) {
        return minShakeDurationTicks + random.nextInt(maxShakeDurationTicks - minShakeDurationTicks + 1);
    }

    public float calculateShakeStrength(float explosionPower) {
        if (!shakeScalesWithPower) {
            return baseShakeStrength * shakeIntensityMultiplier;
        }

        // Scale with explosion power but keep it reasonable
        float scaledStrength = baseShakeStrength + (explosionPower * 0.02f);
        return Math.min(scaledStrength * shakeIntensityMultiplier, maxShakeStrength);
    }

    public float calculateShakeRadius(float explosionPower) {
        if (!shakeScalesWithPower) {
            return shakeRadius;
        }
        // Scale radius with explosion power
        return Math.min(explosionPower * 3.0f, shakeRadius);
    }
}