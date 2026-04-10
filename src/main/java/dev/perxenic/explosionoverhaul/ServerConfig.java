package dev.perxenic.explosionoverhaul;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LAUNCH_FALLING_BLOCKS = BUILDER
            .comment("Whether to launch falling blocks after an explosion")
            .define("launchFallingBlocks", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
