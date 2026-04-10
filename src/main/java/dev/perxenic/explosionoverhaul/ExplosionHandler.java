package dev.perxenic.explosionoverhaul;

import dev.perxenic.explosionoverhaul.content.EOTags;
import dev.perxenic.explosionoverhaul.infra.FallingBlockEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class ExplosionHandler {
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!ServerConfig.launchFallingBlocks) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        var explosion = event.getExplosion();

        // Prevent explosions which do not break blocks from triggering effects
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.KEEP) return;
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) return;

        // Create a list of all blocks that have been processed by Explosion Overhaul to remove from vanilla processing
        List<BlockPos> processedBlocks = new ArrayList<>();

        for (var pos : event.getAffectedBlocks()) {
            var blockState = level.getBlockState(pos);

            // Do not process blocks tagged to not launch
            if (blockState.is(EOTags.Blocks.DO_NOT_LAUNCH)) continue;

            var fallingBlock = FallingBlockEntity.fall(level, pos, blockState);
            ((FallingBlockEntityData)fallingBlock).explosionOverhaul$setCreatedFromExplosion(true);

            processedBlocks.add(pos);

            if (ServerConfig.blockDefaultKnockback) {
                event.getAffectedEntities().add(fallingBlock);
            } else {
                launchEntity(fallingBlock, explosion);
            }
        }

        // Remove all blocks from explosion handling that have been processed by Explosion Overhaul
        event.getAffectedBlocks().removeAll(processedBlocks);
    }
    
    public static void launchEntity(Entity entity, Explosion explosion) {
        var random = entity.getRandom();
        var pos = entity.position();
        var difference = pos.subtract(explosion.center());
        var distance = difference.length();

        // If very close to centre, launch directly up to avoid issues with precision errors
        var direction = (distance < 1e-6) ? new Vec3(0, 1, 0) : directionOffset(difference, random).normalize();

        entity.push(direction);
        // Hurt marking entity syncs velocity for some reason
        entity.hurtMarked = true;
    }

    public static Vec3 directionOffset(Vec3 original, RandomSource randomSource) {
        return original.add(
                (randomSource.nextDouble() * 2 - 1) * ServerConfig.randomDirectionMagnitude,
                (randomSource.nextDouble() * 2 - 1) * ServerConfig.randomDirectionMagnitude + ServerConfig.directionUpwardsBias,
                (randomSource.nextDouble() * 2 - 1) * ServerConfig.randomDirectionMagnitude
        );
    }
}
