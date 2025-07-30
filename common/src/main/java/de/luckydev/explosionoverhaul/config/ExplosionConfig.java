package de.luckydev.explosionoverhaul.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.random.Random;

import java.io.*;

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

    // Screen shake settings
    public boolean enableScreenShake = true;
    public float baseShakeStrength = 0.4f;
    public float maxShakeStrength = 1.0f;
    public float shakeRadius = 20.0f;
    public int minShakeDurationTicks = 10;
    public int maxShakeDurationTicks = 60;
    public boolean shakeScalesWithPower = true;
    public float shakeIntensityMultiplier = 1.0f;

    // Visual effects settings
    public boolean enableVisualEffects = true;
    public boolean enableMushroomCloud = true;
    public boolean enableFlashEffect = true;
    public boolean enableShockwaveEffect = true;
    public boolean enableDebrisParticles = true;
    public float visualEffectsIntensity = 1.0f;
    public boolean enableSecondaryEffects = true;
    public int visualEffectsDistance = 64; // Max distance to render effects

    // Sound
    public boolean playRingingSound = false;

    // Debug
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

        // Clamp visual effects settings
        visualEffectsIntensity = clampFloat(visualEffectsIntensity, 0.1f, 3.0f);
        visualEffectsDistance = clamp(visualEffectsDistance, 16, 256);
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
            try (Writer writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
                // Custom GSON pretty-print with tabs
                String prettyJson = GSON.toJson(JsonParser.parseReader(new FileReader(CONFIG_FILE)));
                prettyJson = prettyJson.replace("  ", "\t");
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write explosion-overhaul config", e);
        }
    }

    // Debris
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

    // Shake
    public int getShakeDuration(Random random) {
        return minShakeDurationTicks + random.nextInt(maxShakeDurationTicks - minShakeDurationTicks + 1);
    }

    public float calculateShakeStrength(float explosionPower) {
        if (!shakeScalesWithPower) {
            return baseShakeStrength * shakeIntensityMultiplier;
        }

        float scaledStrength = baseShakeStrength + (explosionPower * 0.02f);
        return Math.min(scaledStrength * shakeIntensityMultiplier, maxShakeStrength);
    }

    public float calculateShakeRadius(float explosionPower) {
        if (!shakeScalesWithPower) {
            return shakeRadius;
        }
        return Math.min(explosionPower * 3.0f, shakeRadius);
    }

    // Visual Effects
    public boolean shouldRenderVisualEffects(double distanceToPlayer) {
        return enableVisualEffects && distanceToPlayer <= visualEffectsDistance;
    }

    public float getScaledVisualIntensity(float explosionPower) {
        return Math.min(explosionPower * 0.25f * visualEffectsIntensity, 3.0f);
    }
}