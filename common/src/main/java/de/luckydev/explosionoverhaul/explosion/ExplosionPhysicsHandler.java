package de.luckydev.explosionoverhaul.explosion;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.config.ExplosionConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.block.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ExplosionPhysicsHandler {

    private static final ExplosionConfig CONFIG = ExplosionOverhaul.CONFIG;

    // Custom tags for explosion behavior
    public static final TagKey<Block> MULTIBLOCK_PARTS = TagKey.of(Registries.BLOCK.getKey(), Identifier.of(ExplosionOverhaul.MOD_ID, "multiblock_parts"));
    public static final TagKey<Block> NEVER_LAUNCH = TagKey.of(Registries.BLOCK.getKey(), Identifier.of(ExplosionOverhaul.MOD_ID, "never_launch"));

    /**
     * Process the explosion before vanilla logic destroys block.
     * This replaces block we want to turn into debris with air to prevent duplication.
     */
    public static void processExplosion(World world, Vec3d explosionCenter, ObjectArrayList<BlockPos> affectedBlocks, float power) {
        if (world.isClient() || !CONFIG.enabled) {
            return;
        }

        if (!(world instanceof ServerWorld serverWorld)) return;

        // Process debris spawning and block replacement
        int debrisSpawned = spawnDebris(serverWorld, explosionCenter, affectedBlocks);

        if (CONFIG.debugLogging) {
            ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Processed explosion at {} - Spawned {} debris block",
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

            // Use flag 3 | 16 to update neighbors and notify clients
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3 | 16);

            // Force neighbor updates for connected block like fences
            world.updateNeighbors(pos, Blocks.AIR);

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

        // Fixes UUID warning
        fallingBlock.setUuid(java.util.UUID.randomUUID());

        if (CONFIG.randomRotation) {
            fallingBlock.setYaw(world.random.nextFloat() * 360f);
        }
    }

    private static boolean canBeLaunched(BlockState state) {
        if (state.isAir()) return false;

        Block block = state.getBlock();

        // Check custom tags first
        if (state.isIn(NEVER_LAUNCH)) return false;
        if (state.isIn(MULTIBLOCK_PARTS)) return false;

        // Don't launch block that aren't solid
        if (!state.isSolid()) return false;

        // Don't launch TNT (let it explode normally)
        if (state.isOf(Blocks.TNT)) return false;

        // Don't launch replaceable block (grass, snow, etc.)
        if (state.isIn(BlockTags.REPLACEABLE)) return false;

        // Don't launch block with block entities that could cause issues
        if (state.hasBlockEntity()) {
            // Allow some safe block entities
            if (!(block instanceof ChestBlock ||
                    block instanceof BarrelBlock ||
                    block instanceof ShulkerBoxBlock ||
                    block instanceof EnderChestBlock)) {
                return false;
            }
        }

        // Don't launch liquid-logged block
        if (state.getFluidState() != null && !state.getFluidState().isEmpty()) {
            return false;
        }

        return true;
    }
}