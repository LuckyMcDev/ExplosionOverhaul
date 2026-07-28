package dev.perxenic.explosionoverhaul;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

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

    public static Identifier eoLoc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
