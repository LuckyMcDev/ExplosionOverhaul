package de.luckydev.explosionoverhaul.effects;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class ExplosionVisualEffects {

    public static void createMushroomCloud(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects) return;

        Random random = world.getRandom();
        MinecraftClient client = MinecraftClient.getInstance();

        // Better power scaling - less overwhelming for small explosions
        float effectPower = Math.max(1.0f, power);
        float scaleFactor = (float) Math.pow(effectPower / 4.0f, 0.7); // Logarithmic scaling
        scaleFactor = Math.min(scaleFactor, 4.0f);

        // Only create mushroom cloud for larger explosions (power > 3)
        if (power > 3.0f) {
            createAdvancedMushroomCloud(world, center, power, scaleFactor, random);
        } else {
            // For small explosions, create a simpler smoke plume
            createSmokePlume(world, center, power, scaleFactor, random);
        }

        // Always create ground effects
        createImprovedShockwave(world, center, power, scaleFactor, random);

        // Delayed secondary effects
        scheduleDelayedEffects(client, world, center, power, scaleFactor);
    }

    private static void createAdvancedMushroomCloud(ClientWorld world, Vec3d center, float power, float scaleFactor, Random random) {
        // Create a more realistic mushroom cloud with proper physics
        double stemHeight = 6.0 + (power * 1.5);
        double capRadius = 2.5 * scaleFactor;
        int stemParticles = (int) (25 * scaleFactor);
        int capParticles = (int) (20 * scaleFactor);

        // Stem with realistic upward flow
        for (int i = 0; i < stemParticles; i++) {
            double heightProgress = i / (double) stemParticles;
            double height = heightProgress * stemHeight;

            // Stem gets thinner as it goes up
            double maxRadius = (1.0 - heightProgress * 0.7) * 1.5 * scaleFactor;
            double radius = random.nextDouble() * maxRadius;
            double angle = random.nextDouble() * Math.PI * 2;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + height;

            // Realistic updraft velocity
            double velX = Math.cos(angle) * radius * 0.02; // Slight outward motion
            double velY = 0.15 + (random.nextDouble() * 0.1); // Strong updraft
            double velZ = Math.sin(angle) * radius * 0.02;

            // Layer particles by height for realism
            if (height < stemHeight * 0.2) {
                // Hot gases and fire at base
                world.addParticle(ParticleTypes.FLAME, x, y, z, velX, velY * 1.5, velZ);
                if (random.nextFloat() < 0.3) {
                    world.addParticle(ParticleTypes.SMOKE, x, y, z, velX * 0.5, velY, velZ * 0.5);
                }
            } else if (height < stemHeight * 0.6) {
                // Transition zone - hot smoke
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, velX, velY, velZ);
            } else {
                // Upper stem - cooler smoke
                world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, y, z, velX, velY * 0.8, velZ);
            }
        }

        // Mushroom cap with realistic spreading
        double capY = center.y + stemHeight;
        for (int i = 0; i < capParticles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radiusProgress = random.nextDouble();
            double radius = radiusProgress * capRadius;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = capY + (random.nextDouble() - 0.5) * 1.5;

            // Cap spreads outward and slowly descends
            double velX = Math.cos(angle) * radiusProgress * 0.15;
            double velY = -0.02 + (random.nextDouble() * 0.04);
            double velZ = Math.sin(angle) * radiusProgress * 0.15;

            world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, velX, velY, velZ);
            if (random.nextFloat() < 0.4) {
                world.addParticle(ParticleTypes.CLOUD, x, y, z, velX * 0.7, velY, velZ * 0.7);
            }
        }
    }

    private static void createSmokePlume(ClientWorld world, Vec3d center, float power, float scaleFactor, Random random) {
        // Simple smoke plume for smaller explosions
        double plumeHeight = 4.0 + power;
        int particles = (int) (15 * scaleFactor);

        for (int i = 0; i < particles; i++) {
            double height = random.nextDouble() * plumeHeight;
            double radius = (height / plumeHeight) * 0.8 * scaleFactor;
            double angle = random.nextDouble() * Math.PI * 2;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + height;

            double velX = Math.cos(angle) * 0.05;
            double velY = 0.1 + random.nextDouble() * 0.08;
            double velZ = Math.sin(angle) * 0.05;

            if (height < plumeHeight * 0.3) {
                world.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
            } else {
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, velX, velY * 0.8, velZ);
            }
        }
    }

    private static void createImprovedShockwave(ClientWorld world, Vec3d center, float power, float scaleFactor, Random random) {
        // Create multiple shockwave rings for more impact
        int rings = Math.min(3, (int) (power / 2) + 1);

        for (int ring = 0; ring < rings; ring++) {
            double ringDelay = ring * 0.1; // Slight delay between rings
            double baseRadius = (ring + 1) * 2.0 * scaleFactor;
            int particlesPerRing = (int) (20 * scaleFactor / (ring + 1));

            for (int i = 0; i < particlesPerRing; i++) {
                double angle = (i / (double) particlesPerRing) * Math.PI * 2;
                double radiusVariation = 1.0 + (random.nextDouble() - 0.5) * 0.3;
                double radius = baseRadius * radiusVariation;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;
                double y = center.y + (random.nextDouble() - 0.3) * 0.5;

                // Particles move outward from explosion
                double speed = 0.4 + random.nextDouble() * 0.3;
                double velX = Math.cos(angle) * speed;
                double velY = random.nextDouble() * 0.2;
                double velZ = Math.sin(angle) * speed;

                // Different particle types for visual variety
                if (ring == 0) {
                    // First ring - dust and debris
                    world.addParticle(ParticleTypes.POOF, x, y, z, velX, velY, velZ);
                } else {
                    // Outer rings - smoke
                    world.addParticle(ParticleTypes.CLOUD, x, y, z, velX * 0.6, velY, velZ * 0.6);
                }
            }
        }
    }

    private static void scheduleDelayedEffects(MinecraftClient client, ClientWorld world, Vec3d center, float power, float scaleFactor) {
        if (!ExplosionOverhaul.CONFIG.enableSecondaryEffects) return;

        // Staggered secondary effects
        client.execute(() -> {
            new Thread(() -> {
                try {
                    // Dust settling effect
                    Thread.sleep(800);
                    client.execute(() -> createDustSettling(world, center, power, scaleFactor));

                    // Lingering smoke
                    Thread.sleep(1500);
                    client.execute(() -> createLingeringSmoke(world, center, power, scaleFactor));

                    // Final atmospheric effect
                    if (power > 5.0f) {
                        Thread.sleep(2500);
                        client.execute(() -> createAtmosphericHaze(world, center, power, scaleFactor));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    private static void createDustSettling(ClientWorld world, Vec3d center, float power, float scaleFactor) {
        Random random = world.getRandom();
        int particles = (int) (12 * scaleFactor);
        double settleRadius = 3.0 * scaleFactor;

        for (int i = 0; i < particles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * settleRadius;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 1.5 + random.nextDouble() * 2.0;

            // Slowly falling dust
            double velX = (random.nextDouble() - 0.5) * 0.02;
            double velY = -0.03 - random.nextDouble() * 0.02;
            double velZ = (random.nextDouble() - 0.5) * 0.02;

            world.addParticle(ParticleTypes.CLOUD, x, y, z, velX, velY, velZ);
        }
    }

    private static void createLingeringSmoke(ClientWorld world, Vec3d center, float power, float scaleFactor) {
        Random random = world.getRandom();
        int particles = (int) (8 * scaleFactor);

        for (int i = 0; i < particles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * 2.5 * scaleFactor;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + random.nextDouble() * 1.5;

            double velX = (random.nextDouble() - 0.5) * 0.01;
            double velY = 0.03 + random.nextDouble() * 0.05;
            double velZ = (random.nextDouble() - 0.5) * 0.01;

            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, velX, velY, velZ);
        }
    }

    private static void createAtmosphericHaze(ClientWorld world, Vec3d center, float power, float scaleFactor) {
        Random random = world.getRandom();
        int particles = (int) (6 * scaleFactor);
        double hazeRadius = 4.0 * scaleFactor;

        for (int i = 0; i < particles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = random.nextDouble() * hazeRadius;
            double height = random.nextDouble() * 3.0;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + height;

            double velX = (random.nextDouble() - 0.5) * 0.005;
            double velY = 0.01 + random.nextDouble() * 0.02;
            double velZ = (random.nextDouble() - 0.5) * 0.005;

            world.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
        }
    }

    public static void createFlashEffect(ClientWorld world, Vec3d center, float power) {
        if (!ExplosionOverhaul.CONFIG.enableVisualEffects) return;

        Random random = world.getRandom();
        // Scale flash based on power but don't make small explosions too flashy
        float flashIntensity = Math.min(power / 6.0f, 1.0f);
        int flashParticles = (int) (8 + (12 * flashIntensity));

        // Create initial bright flash
        for (int i = 0; i < flashParticles; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double pitch = random.nextDouble() * Math.PI * 0.8; // More horizontal spread
            double radius = 0.5 + random.nextDouble() * (1.0 + flashIntensity);

            double x = center.x + Math.cos(angle) * Math.sin(pitch) * radius;
            double y = center.y + Math.cos(pitch) * radius;
            double z = center.z + Math.sin(angle) * Math.sin(pitch) * radius;

            // Quick, bright particles
            world.addParticle(ParticleTypes.FIREWORK, x, y, z, 0, 0, 0);

            if (random.nextFloat() < 0.3 && flashIntensity > 0.5) {
                world.addParticle(ParticleTypes.FLASH, x, y, z, 0, 0, 0);
            }
        }

        // Add some energy particles for larger explosions
        if (power > 4.0f) {
            for (int i = 0; i < 6; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double speed = 0.3 + random.nextDouble() * 0.4;

                double x = center.x + (random.nextDouble() - 0.5) * 0.5;
                double y = center.y + (random.nextDouble() - 0.5) * 0.5;
                double z = center.z + (random.nextDouble() - 0.5) * 0.5;

                double velX = Math.cos(angle) * speed;
                double velY = (random.nextDouble() - 0.5) * speed;
                double velZ = Math.sin(angle) * speed;

                world.addParticle(ParticleTypes.ENCHANTED_HIT, x, y, z, velX, velY, velZ);
            }
        }

        // Scale sound based on power
        if (power > 3.0f) {
            float volume = Math.min(0.8f, power / 8.0f);
            float pitch = 0.7f + random.nextFloat() * 0.3f;

            world.playSound(center.x, center.y, center.z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                    SoundCategory.AMBIENT, volume, pitch, false);
        }
    }
}