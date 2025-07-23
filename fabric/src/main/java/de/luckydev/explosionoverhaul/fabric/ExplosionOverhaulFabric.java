package de.luckydev.explosionoverhaul.fabric;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import net.fabricmc.api.ModInitializer;

public class ExplosionOverhaulFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ExplosionOverhaul.init();
    }
}