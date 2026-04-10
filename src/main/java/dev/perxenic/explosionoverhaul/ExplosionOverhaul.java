package dev.perxenic.explosionoverhaul;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(ExplosionOverhaul.MODID)
public class ExplosionOverhaul {
    public static final String MODID = "explosionoverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExplosionOverhaul(ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
