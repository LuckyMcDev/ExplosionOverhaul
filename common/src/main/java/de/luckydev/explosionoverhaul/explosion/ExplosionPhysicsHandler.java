package de.luckydev.explosionoverhaul.explosion;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.config.ExplosionConfig;
import de.luckydev.explosionoverhaul.shake.PositionedScreenShake;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ExplosionPhysicsHandler {

    private static final ExplosionConfig CONFIG = ExplosionOverhaul.CONFIG;
    private static final Map<Explosion, ObjectArrayList<BlockState>> STORED_STATES = new WeakHashMap<>();

    public static void onPre(World world, Explosion explosion) {
        if (world.isClient()) return;

        ObjectArrayList<BlockState> snapshot = new ObjectArrayList<>();
        for (BlockPos pos : explosion.getAffectedBlocks()) {
            BlockState state = world.getBlockState(pos);
            if (!CONFIG.allowUnbreakableBlocks && state.getHardness(world, pos) < 0) continue;
            snapshot.add(state);
        }
        STORED_STATES.put(explosion, snapshot);

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Stored {} block states at {}", snapshot.size(), explosion.getPosition());
        }
    }

    public static void onDetonate(World world, Explosion explosion) {
        if (world.isClient() || !CONFIG.enabled) {
            STORED_STATES.remove(explosion);
            return;
        }

        ObjectArrayList<BlockState> stored = STORED_STATES.remove(explosion);
        if (stored == null) return;

        if (!(world instanceof ServerWorld server)) return;

        Vec3d center = explosion.getPosition();
        List<BlockPos> affected = explosion.getAffectedBlocks();

        // Spawn debris
        int spawned = 0;
        for (int i = 0; i < affected.size() && spawned < CONFIG.maxFallingBlocks; i++) {
            BlockPos pos = affected.get(i);
            BlockState state = i < stored.size() ? stored.get(i) : null;
            if (state == null || state.isAir()) continue;
            if (!CONFIG.allowUnbreakableBlocks && state.getBlock().getHardness() < 0) continue;
            if (server.random.nextDouble() > CONFIG.getSpawnProbability()) continue;

            FallingBlockEntity fb = FallingBlockEntity.spawnFromBlock(server, pos, state);
            if (fb == null) continue;

            Vec3d dir = Vec3d.ofCenter(pos).subtract(center);
            if (dir.lengthSquared() < 1e-6) {
                dir = new Vec3d(
                        (server.random.nextDouble() - 0.5) * 2,
                        server.random.nextDouble(),
                        (server.random.nextDouble() - 0.5) * 2
                );
            }
            dir = dir.normalize();

            double hSpeed = CONFIG.getRandomHorizontalSpeed(server.random);
            double vForce = CONFIG.getRandomUpwardForce(server.random);
            fb.setVelocity(dir.multiply(hSpeed).add(0, vForce, 0));
            fb.velocityModified = true;

            if (CONFIG.randomRotation) {
                fb.setYaw(server.random.nextFloat() * 360f);
            }

            server.spawnEntity(fb);
            spawned++;
        }


        int baseY = affected.stream().mapToInt(BlockPos::getY).min().orElse(BlockPos.ofFloored(center).getY() - 1);
        BlockPos centerFloor = BlockPos.ofFloored(center).withY(baseY);

        int radius = 4;
        Random rand = world.getRandom();

        for (BlockPos pos : BlockPos.iterateOutwards(centerFloor, radius, 0, radius)) {
            // Basic distance-based falloff
            double dx = pos.getX() + 0.5 - centerFloor.getX();
            double dz = pos.getZ() + 0.5 - centerFloor.getZ();
            double distSq = dx * dx + dz * dz;

            if (distSq > radius * radius) continue;

            // Simple noise-like variation
            double noise = (Math.sin(dx * 1.2) + Math.cos(dz * 1.7)) * 0.5 + rand.nextDouble() * 0.5;

            // Apply a wavy Y offset (-1 to +1)
            int yOffset = rand.nextInt(3) - 1;
            BlockPos target = pos.down(yOffset);

            BlockState current = world.getBlockState(target);
            if (!current.isReplaceable()) continue;

            // Blend between gray and light gray based on distance and noise
            double weight = MathHelper.clamp((distSq / (radius * radius)) + (noise * 0.2), 0.0, 1.0);
            BlockState newState = (weight < 0.5)
                    ? Blocks.GRAY_CONCRETE_POWDER.getDefaultState()
                    : Blocks.LIGHT_GRAY_CONCRETE_POWDER.getDefaultState();

            server.setBlockState(target, newState);
        }



        // Add screen shake with new config options
        if (CONFIG.enableScreenShake) {
            float shakeStrength = CONFIG.calculateShakeStrength(explosion.getPower());
            float shakeRadius = CONFIG.calculateShakeRadius(explosion.getPower());
            int shakeDuration = CONFIG.getShakeDuration(server.random);

            ScreenShakeHandler.addShake(new PositionedScreenShake(center, shakeRadius, shakeStrength, shakeDuration));

            if (CONFIG.debugLogging) {
                ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Added screen shake - Strength: {}, Radius: {}, Duration: {}",
                        shakeStrength, shakeRadius, shakeDuration);
            }
        }

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Spawned {} debris blocks at {}", spawned, center);
        }
    }
}