package de.luckydev.explosionoverhaul.fabric;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ExplosionOverhaulFabric implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        ExplosionOverhaul.init();
    }

    @Override
    public void onInitializeClient() {
    }
}