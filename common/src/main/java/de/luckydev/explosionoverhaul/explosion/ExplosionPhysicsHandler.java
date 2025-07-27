package de.luckydev.explosionoverhaul.explosion;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.config.ExplosionConfig;
import de.luckydev.explosionoverhaul.shake.PositionedScreenShake;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class ExplosionPhysicsHandler {

    private static final ExplosionConfig CONFIG = ExplosionOverhaul.CONFIG;

    private static final Set<Class<? extends Block>> MULTIBLOCK_CLASSES = new HashSet<>();

    static {
        MULTIBLOCK_CLASSES.add(BedBlock.class);
        MULTIBLOCK_CLASSES.add(DoorBlock.class);
        MULTIBLOCK_CLASSES.add(StairsBlock.class);
        MULTIBLOCK_CLASSES.add(FenceGateBlock.class);
        MULTIBLOCK_CLASSES.add(TrapdoorBlock.class);
        MULTIBLOCK_CLASSES.add(ChestBlock.class);
        MULTIBLOCK_CLASSES.add(TrappedChestBlock.class);
        MULTIBLOCK_CLASSES.add(BarrelBlock.class);
        MULTIBLOCK_CLASSES.add(BannerBlock.class);
        MULTIBLOCK_CLASSES.add(WallBannerBlock.class);
    }

    /**
     * Process the explosion before vanilla logic destroys blocks.
     * This replaces blocks we want to turn into debris with air to prevent duplication.
     */
    public static void processExplosion(World world, Vec3d explosionCenter, ObjectArrayList<BlockPos> affectedBlocks, float power) {
        if (world.isClient() || !CONFIG.enabled) {
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) return;

        // Process debris spawning and block replacement
        int debrisSpawned = spawnDebris(serverWorld, explosionCenter, affectedBlocks);

        // Add screen shake
        addScreenShake(explosionCenter, power, serverWorld);

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Processed explosion at {} - Spawned {} debris blocks",
                    explosionCenter, debrisSpawned);
        }
    }

    private static int spawnDebris(ServerWorld world, Vec3d explosionCenter, ObjectArrayList<BlockPos> affectedBlocks) {
        int spawned = 0;

        ObjectListIterator<BlockPos> affectedBlocksIterator = affectedBlocks.iterator();

        while (affectedBlocksIterator.hasNext()) {
            BlockPos pos = affectedBlocksIterator.next();

            if (spawned >= CONFIG.maxFallingBlocks) break;

            BlockState state = world.getBlockState(pos);

            if (!canBeLaunched(state)) continue;

            if (world.random.nextDouble() > CONFIG.getSpawnProbability()) continue;


            createFallingBlock(world, pos, state, explosionCenter);

            affectedBlocks.remove(pos);

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

            spawned++;
        }

        return spawned;
    }

    private static void createFallingBlock(ServerWorld world, BlockPos pos, BlockState state, Vec3d explosionCenter) {

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

    private static boolean isMultiblockPart(Block block) {
        for (Class<? extends Block> clazz : MULTIBLOCK_CLASSES) {
            if (clazz.isInstance(block)) return true;
        }
        return false;
    }

    private static boolean canBeLaunched(BlockState state) {
        if (state.isAir()) return false;

        if(!state.isSolid()) return false;

        if (state.isOf(Blocks.TNT)) return false;

        if (state.isIn(BlockTags.REPLACEABLE)) return false;

        if (isMultiblockPart(state.getBlock())) return false;

        return true;
    }
}