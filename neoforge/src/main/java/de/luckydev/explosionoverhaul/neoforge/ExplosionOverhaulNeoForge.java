package de.luckydev.explosionoverhaul.neoforge;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.ExplosionOverhaulClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(ExplosionOverhaul.MOD_ID)
public final class ExplosionOverhaulNeoForge {

    public ExplosionOverhaulNeoForge(IEventBus modEventBus) {
        // Common setup
        ExplosionOverhaul.init();

        // Register client setup event
        modEventBus.addListener(this::onClientInit);
    }

    private void onClientInit(final FMLClientSetupEvent event) {
        // Only run on client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ExplosionOverhaulClient.init();
        }
    }
}