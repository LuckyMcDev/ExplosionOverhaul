package de.luckydev.explosionoverhaul.explosion;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.config.ExplosionConfig;
import de.luckydev.explosionoverhaul.shake.PositionedScreenShake;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ExplosionPhysicsHandler {

    private static final ExplosionConfig CONFIG = ExplosionOverhaul.CONFIG;

    private static final Map<Explosion, ObjectArrayList<BlockSnapshot>> BLOCK_SNAPSHOTS = new WeakHashMap<>();

    private record BlockSnapshot(BlockPos pos, BlockState state) {}

    public static void captureBlocksBeforeDestruction(World world, Explosion explosion) {
        if (world.isClient() || !CONFIG.enabled) return;

        ObjectArrayList<BlockSnapshot> snapshots = new ObjectArrayList<>();

        for (BlockPos pos : explosion.getAffectedBlocks()) {
            BlockState state = world.getBlockState(pos);

            if (state.isAir()) continue;
            if (!CONFIG.allowUnbreakableBlocks && state.getHardness(world, pos) < 0) continue;

            snapshots.add(new BlockSnapshot(pos, state));
        }

        BLOCK_SNAPSHOTS.put(explosion, snapshots);

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Captured {} block snapshots at {}",
                    snapshots.size(), explosion.getPosition());
        }
    }

    /**
     * Process the explosion before vanilla logic destroys blocks.
     * This replaces blocks we want to turn into debris with air to prevent duplication.
     */
    public static void processExplosion(World world, Explosion explosion) {
        if (world.isClient() || !CONFIG.enabled) {
            BLOCK_SNAPSHOTS.remove(explosion);
            return;
        }

        ObjectArrayList<BlockSnapshot> snapshots = BLOCK_SNAPSHOTS.remove(explosion);
        if (snapshots == null || snapshots.isEmpty()) return;

        if (!(world instanceof ServerWorld serverWorld)) return;

        Vec3d explosionCenter = explosion.getPosition();

        // Process debris spawning and block replacement
        int debrisSpawned = spawnDebris(serverWorld, explosionCenter, snapshots);

        // Add screen shake
        addScreenShake(explosionCenter, explosion.getPower(), serverWorld);

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Processed explosion at {} - Spawned {} debris blocks",
                    explosionCenter, debrisSpawned);
        }
    }

    private static int spawnDebris(ServerWorld world, Vec3d explosionCenter, ObjectArrayList<BlockSnapshot> snapshots) {
        int spawned = 0;

        for (int i = snapshots.size() - 1; i > 0; i--) {
            int j = world.random.nextInt(i + 1);
            BlockSnapshot temp = snapshots.get(i);
            snapshots.set(i, snapshots.get(j));
            snapshots.set(j, temp);
        }

        for (BlockSnapshot snapshot : snapshots) {
            if (spawned >= CONFIG.maxFallingBlocks) break;

            BlockPos pos = snapshot.pos();
            BlockState state = snapshot.state();

            BlockState currentState = world.getBlockState(pos);
            if (currentState.isAir()) continue;

            // TNT should use default handling to avoid breaking machinery
            if (currentState.isOf(Blocks.TNT)) continue;

            if (world.random.nextDouble() > CONFIG.getSpawnProbability()) {
                continue;
            }


            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);


            FallingBlockEntity fallingBlock = createFallingBlock(world, pos, state, explosionCenter);
            if (fallingBlock != null) {
                spawned++;
            }
        }

        return spawned;
    }

    private static FallingBlockEntity createFallingBlock(ServerWorld world, BlockPos pos, BlockState state, Vec3d explosionCenter) {

        if (!canBeLaunched(state)) {
            return null;
        }

        FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, pos, state);

        Vec3d blockCenter = Vec3d.ofCenter(pos);
        Vec3d direction = blockCenter.subtract(explosionCenter);

        double distance = direction.length();

        if (distance < 1e-6) {
            direction = new Vec3d(
                    (world.random.nextDouble() - 0.5) * 2,
                    world.random.nextDouble(),
                    (world.random.nextDouble() - 0.5) * 2
            );
            distance = 1.0;
        } else {
            direction = direction.normalize();
        }

        double horizontalSpeed = CONFIG.getRandomHorizontalSpeed(world.random);
        double upwardForce = CONFIG.getRandomUpwardForce(world.random);

        double maxDistance = 10.0;
        double distanceFactor = Math.max(0.3, (maxDistance - Math.min(distance, maxDistance)) / maxDistance);

        horizontalSpeed *= (1.0 + distanceFactor * 0.8);
        upwardForce *= (1.0 + distanceFactor * 0.5);

        Vec3d randomOffset = new Vec3d(
                (world.random.nextDouble() - 0.5) * 0.2,
                world.random.nextDouble() * 0.15,
                (world.random.nextDouble() - 0.5) * 0.2
        );

        direction = direction.add(randomOffset).normalize();

        Vec3d velocity = direction.multiply(horizontalSpeed).add(0, upwardForce, 0);
        fallingBlock.setVelocity(velocity);
        fallingBlock.velocityModified = true;

        //Fixes UUID warning
        fallingBlock.setUuid(java.util.UUID.randomUUID());

        if (CONFIG.randomRotation) {
            fallingBlock.setYaw(world.random.nextFloat() * 360f);
        }

        return fallingBlock;
    }

    private static void addScreenShake(Vec3d explosionCenter, float explosionPower, ServerWorld world) {
        if (!CONFIG.enableScreenShake) return;

        float shakeStrength = CONFIG.calculateShakeStrength(explosionPower);
        float shakeRadius = CONFIG.calculateShakeRadius(explosionPower);
        int shakeDuration = CONFIG.getShakeDuration(world.random);

        ScreenShakeHandler.addShake(new PositionedScreenShake(explosionCenter, shakeRadius, shakeStrength, shakeDuration));

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Added screen shake - Strength: {}, Radius: {}, Duration: {}",
                    shakeStrength, shakeRadius, shakeDuration);
        }
    }

    private static boolean canBeLaunched(BlockState state) {
        // Exclude air blocks
        if (state.isAir()) {
            return false;
        }

        // Check if the block is in the replaceable tag
        if (state.isIn(BlockTags.REPLACEABLE)) {
            return false;
        }

        return true;
    }
}