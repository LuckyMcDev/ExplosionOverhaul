package dev.perxenic.explosionoverhaul;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LAUNCH_FALLING_BLOCKS = BUILDER
            .comment("Whether to launch falling blocks after an explosion")
            .define("launchFallingBlocks", true);

    public static final ModConfigSpec.BooleanValue BLOCK_DEFAULT_KNOCKBACK = BUILDER
            .comment("Whether to use default explosion knockback for launched falling blocks")
            .define("blockDefaultKnockback", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean launchFallingBlocks;
    public static boolean blockDefaultKnockback;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        launchFallingBlocks = LAUNCH_FALLING_BLOCKS.get();
        blockDefaultKnockback = BLOCK_DEFAULT_KNOCKBACK.get();
    }
}
