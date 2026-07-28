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

    public static final ModConfigSpec.DoubleValue LAUNCH_BLOCK_CHANCE = BUILDER
            .comment("Chance to launch a given block during an explosion")
            .defineInRange("launchBlockChance", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue BLOCK_DEFAULT_KNOCKBACK = BUILDER
            .comment("Whether to use default explosion knockback for launched falling blocks")
            .define("blockDefaultKnockback", false);

    public static final ModConfigSpec.DoubleValue BLOCK_KNOCKBACK_FORCE = BUILDER
            .comment("Amount the blocks are launched when using custom knockback")
            .defineInRange("blockKnockbackForce", 1.0, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue RANDOM_DIRECTION_MAGNITUDE = BUILDER
            .comment("Amount the direction blocks are launched is randomised")
            .defineInRange("randomDirectionMagnitude", 0.5, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue DIRECTION_UPWARDS_BIAS = BUILDER
            .comment("Amount the direction is biased upwards when launching blocks")
            .defineInRange("directionUpwardsBias", 3.0, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ENABLE_SMOKE_TRAILS = BUILDER
            .comment("Whether exploded blocks should have smoke trails")
            .define("enableSmokeTrails", true);

    public static final ModConfigSpec.DoubleValue SMOKE_PARTICLE_CHANCE = BUILDER
            .comment("Chance for a smoking block to emit a trail particle")
            .defineInRange("smokeParticleChance", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SMOKE_PARTICLE_SPREAD = BUILDER
            .comment("How fast smoke particles spread out from their original position")
            .defineInRange("smokeParticleSpread", 0.025, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue RE_PLACE_TAGGED_BLOCKS = BUILDER
            .comment("Whether to use custom logic to attempt to re place blocks in update on land tag")
            .define("rePlaceTaggedBlocks", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean launchFallingBlocks;
    public static double launchBlockChance;
    public static boolean blockDefaultKnockback;
    public static double randomDirectionMagnitude;
    public static double blockKnockbackForce;
    public static double directionUpwardsBias;
    public static boolean enableSmokeTrails;
    public static double smokeParticleChance;
    public static double smokeParticleSpread;
    public static boolean rePlaceTaggedBlocks;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event instanceof ModConfigEvent.Unloading) return;

        launchFallingBlocks = LAUNCH_FALLING_BLOCKS.get();
        launchBlockChance = LAUNCH_BLOCK_CHANCE.get();
        blockDefaultKnockback = BLOCK_DEFAULT_KNOCKBACK.get();
        randomDirectionMagnitude = RANDOM_DIRECTION_MAGNITUDE.get();
        blockKnockbackForce = BLOCK_KNOCKBACK_FORCE.get();
        directionUpwardsBias = DIRECTION_UPWARDS_BIAS.get();
        enableSmokeTrails = ENABLE_SMOKE_TRAILS.get();
        smokeParticleChance = SMOKE_PARTICLE_CHANCE.get();
        smokeParticleSpread = SMOKE_PARTICLE_SPREAD.get();
        rePlaceTaggedBlocks = RE_PLACE_TAGGED_BLOCKS.get();
    }
}
