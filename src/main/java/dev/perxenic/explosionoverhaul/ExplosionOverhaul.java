package dev.perxenic.explosionoverhaul;

import dev.perxenic.explosionoverhaul.content.EOTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
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
        var level = event.getLevel();
        var explosion = event.getExplosion();

        // Prevent explosions which do not break blocks from triggering effects
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.KEEP) return;
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) return;

        // Create a list of all blocks that have been processed by Explosion Overhaul
        List<BlockPos> processedBlocks = new ArrayList<>();

        for (var pos : event.getAffectedBlocks()) {
            var blockState = level.getBlockState(pos);

            // Do not process blocks tagged to not launch
            if (blockState.is(EOTags.Blocks.DO_NOT_LAUNCH)) continue;

            processedBlocks.add(pos);
            event.getLevel().setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(), 0b0000011);
        }

        // Remove all blocks from explosion handling that have been processed by Explosion Overhaul
        event.getAffectedBlocks().removeAll(processedBlocks);
    }
}
