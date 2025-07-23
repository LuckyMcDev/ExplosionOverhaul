package de.luckydev.explosionoverhaul.fabric;

import de.luckydev.explosionoverhaul.ExplosionOverhaulClient;
import net.fabricmc.api.ClientModInitializer;

public class ExplosionOverhaulFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExplosionOverhaulClient.init();
    }
}
