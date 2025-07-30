package de.luckydev.explosionoverhaul.effects;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ExplosionParticleHandler {

    /**
     * Creates a focused crater effect with better particle distribution
     */
    public static void createCraterEffect(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects) return;

        Random random = world.getRandom();
        float effectPower = Math.max(1.0f, power);
        float scaleFactor = (float) Math.pow(effectPower / 4.0f, 0.6);
        scaleFactor = Math.min(scaleFactor, 2.0f);

        int particleCount = (int) (20 + (15 * scaleFactor));
        double craterRadius = 2.0 + (1.5 * scaleFactor);

        // Create concentric rings of particles for better visual impact
        int rings = Math.max(2, (int) (scaleFactor * 2));

        for (int ring = 0; ring < rings; ring++) {
            double ringRadius = (craterRadius / rings) * (ring + 1);
            int particlesInRing = particleCount / rings;

            for (int i = 0; i < particlesInRing; i++) {
                double angle = (i / (double) particlesInRing) * Math.PI * 2;
                double radiusVariation = 1.0 + (random.nextDouble() - 0.5) * 0.4;
                double actualRadius = ringRadius * radiusVariation;

                double x = center.x + Math.cos(angle) * actualRadius;
                double z = center.z + Math.sin(angle) * actualRadius;
                double y = center.y - 0.2 + random.nextDouble() * 0.8;

                // Particles should move outward and upward
                double outwardSpeed = 0.1 + (random.nextDouble() * 0.15);
                double velX = Math.cos(angle) * outwardSpeed;
                double velY = 0.1 + random.nextDouble() * 0.25;
                double velZ = Math.sin(angle) * outwardSpeed;

                // Use different particles based on ring position
                if (ring == 0) {
                    // Inner ring - more intense
                    world.addParticle(ParticleTypes.POOF, x, y, z, velX, velY, velZ);
                    if (random.nextFloat() < 0.4) {
                        world.addParticle(ParticleTypes.SMOKE, x, y + 0.5, z, velX * 0.7, velY * 1.2, velZ * 0.7);
                    }
                } else {
                    // Outer rings - dust clouds
                    world.addParticle(ParticleTypes.CLOUD, x, y, z, velX * 0.8, velY * 0.9, velZ * 0.8);
                }
            }
        }
    }

    /**
     * Creates a more subtle heat haze effect
     */
    public static void createHeatHazeEffect(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects) return;

        Random random = world.getRandom();
        float effectPower = Math.max(1.0f, power);
        float scaleFactor = Math.min(effectPower / 6.0f, 1.5f);

        // Reduce particle count for less overwhelming effect
        int particleCount = (int) (8 + (12 * scaleFactor));

        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.8 + random.nextDouble() * 2.0 * scaleFactor;
            double height = random.nextDouble() * 2.5;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + height;

            // Subtle shimmering motion
            double velX = (random.nextDouble() - 0.5) * 0.05;
            double velY = 0.05 + random.nextDouble() * 0.08;
            double velZ = (random.nextDouble() - 0.5) * 0.05;

            // Only use enchant particles for heat haze - more subtle
            world.addParticle(ParticleTypes.ENCHANT, x, y, z, velX, velY, velZ);
        }
    }

    /**
     * Creates focused spark effects that scale better with power
     */
    public static void createSparkEffect(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects) return;

        Random random = world.getRandom();
        float effectPower = Math.max(1.0f, power);
        float scaleFactor = Math.min(effectPower / 4.0f, 2.5f);

        int sparkCount = (int) (10 + (15 * scaleFactor));

        for (int i = 0; i < sparkCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double pitch = Math.PI * 0.15 + (random.nextDouble() * Math.PI * 0.4); // More upward bias
            double speed = 0.6 + random.nextDouble() * (1.0 + scaleFactor * 0.5);

            double velX = Math.cos(angle) * Math.cos(pitch) * speed;
            double velY = Math.sin(pitch) * speed;
            double velZ = Math.sin(angle) * Math.cos(pitch) * speed;

            double x = center.x + (random.nextDouble() - 0.5) * 0.6;
            double y = center.y + random.nextDouble() * 0.8;
            double z = center.z + (random.nextDouble() - 0.5) * 0.6;

            // Primary spark effect
            world.addParticle(ParticleTypes.CRIT, x, y, z, velX, velY, velZ);

            // Add secondary effects based on power
            if (random.nextFloat() < 0.3 && effectPower > 2.0f) {
                world.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, velX * 0.8, velY * 0.8, velZ * 0.8);
            }

            // Add trailing smoke for larger explosions
            if (random.nextFloat() < 0.15 && effectPower > 3.0f) {
                world.addParticle(ParticleTypes.SMOKE, x, y, z, velX * 0.2, velY * 0.3, velZ * 0.2);
            }
        }
    }

    /**
     * Simplified atmospheric dust - only for larger explosions
     */
    public static void createAtmosphericDust(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects ||
                !ExplosionOverhaul.CONFIG.enableSecondaryEffects ||
                power < 3.0f) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Only create atmospheric effects for significant explosions
        client.execute(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Wait 1 second
                    client.execute(() -> createDustClouds(world, center, power));

                    if (power > 5.0f) {
                        Thread.sleep(2000); // Wait another 2 seconds for large explosions
                        client.execute(() -> createSettlingDust(world, center, power));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    private static void createDustClouds(ClientWorld world, Vec3d center, float power) {
        Random random = world.getRandom();
        float scaleFactor = Math.min(power / 6.0f, 1.8f);
        int dustParticles = (int) (15 + (20 * scaleFactor));

        for (int i = 0; i < dustParticles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 1.5 + random.nextDouble() * 4.0 * scaleFactor;
            double height = random.nextDouble() * 2.5;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + height;

            double velX = Math.cos(angle) * 0.03 * random.nextDouble();
            double velY = 0.04 + random.nextDouble() * 0.08;
            double velZ = Math.sin(angle) * 0.03 * random.nextDouble();

            world.addParticle(ParticleTypes.CLOUD, x, y, z, velX, velY, velZ);

            if (random.nextFloat() < 0.5) {
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y - 0.3, z, velX * 1.5, velY * 0.7, velZ * 1.5);
            }
        }
    }

    private static void createSettlingDust(ClientWorld world, Vec3d center, float power) {
        Random random = world.getRandom();
        float scaleFactor = Math.min(power / 8.0f, 1.2f);
        int settlingParticles = (int) (8 + (12 * scaleFactor));

        for (int i = 0; i < settlingParticles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 3.5 * scaleFactor;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 1.5 + random.nextDouble() * 1.5;

            // Very slowly falling dust
            double velX = (random.nextDouble() - 0.5) * 0.01;
            double velY = -0.03 - random.nextDouble() * 0.03;
            double velZ = (random.nextDouble() - 0.5) * 0.01;

            world.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
        }
    }

    /**
     * Creates all particle effects with better sequencing and power scaling
     */
    public static void createCompleteExplosionEffects(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects) return;

        // Immediate effects - always create these
        createSparkEffect(world, center, power);

        // Only create heat haze for medium+ explosions
        if (power > 1.5f) {
            createHeatHazeEffect(world, center, power);
        }

        MinecraftClient client = MinecraftClient.getInstance();

        // Delayed crater effect
        client.execute(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(150); // Very short delay
                    client.execute(() -> {
                        createCraterEffect(world, center, power);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });

        // Long-term atmospheric effects only for larger explosions
        if (power > 2.5f) {
            createAtmosphericDust(world, center, power);
        }
    }
}