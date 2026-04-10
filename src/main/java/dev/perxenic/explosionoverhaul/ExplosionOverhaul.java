package dev.perxenic.explosionoverhaul;

import dev.perxenic.explosionoverhaul.content.EOTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber
@Mod(ExplosionOverhaul.MODID)
public class ExplosionOverhaul {
    public static final String MODID = "explosionoverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExplosionOverhaul(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    public static ResourceLocation eoLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!ServerConfig.launchFallingBlocks) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        var explosion = event.getExplosion();
        var center = explosion.center();

        // Prevent explosions which do not break blocks from triggering effects
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.KEEP) return;
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) return;

        // Create a list of all blocks that have been processed by Explosion Overhaul to remove from vanilla processing
        List<BlockPos> processedBlocks = new ArrayList<>();

        for (var pos : event.getAffectedBlocks()) {
            var blockState = level.getBlockState(pos);

            // Do not process blocks tagged to not launch
            if (blockState.is(EOTags.Blocks.DO_NOT_LAUNCH)) continue;

            var posVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
            var difference = posVec.subtract(center);
            var distance = difference.length();

            // Do not process blocks incredibly close to explosion center
            if (distance < 1e-6) continue;

            var direction = difference.normalize();

            var fallingBlock = FallingBlockEntity.fall(level, pos, blockState);
            processedBlocks.add(pos);

            if (ServerConfig.blockDefaultKnockback) {
                event.getAffectedEntities().add(fallingBlock);
            } else {
                fallingBlock.push(direction);
                // Hurt marking entity syncs velocity for some reason
                fallingBlock.hurtMarked = true;
            }
        }

        // Remove all blocks from explosion handling that have been processed by Explosion Overhaul
        event.getAffectedBlocks().removeAll(processedBlocks);
    }
}
