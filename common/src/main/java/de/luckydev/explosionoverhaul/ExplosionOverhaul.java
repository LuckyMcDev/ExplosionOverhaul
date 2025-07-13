package de.luckydev.explosionoverhaul;

import de.luckydev.explosionoverhaul.config.ExplosionConfig;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import de.luckydev.explosionoverhaul.util.RegistryHelper;
import dev.architectury.event.events.client.ClientTickEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

public final class ExplosionOverhaul {
    public static final String MOD_ID = "explosionoverhaul";
    public static ExplosionConfig CONFIG;
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Snapshot of one block’s pos+state before detonation */
    private record Snapshot(BlockPos pos, BlockState state) {}

    /** Map each Explosion → the list of Snapshots we took in PRE */
    private static final Map<Explosion, ObjectArrayList<Snapshot>> SNAPSHOTS = new WeakHashMap<>();

    public static void init() {
        CONFIG = ExplosionConfig.load();

        ClientTickEvent.CLIENT_POST.register(client -> {
            if (!client.isPaused()) {
                ScreenShakeHandler.tick();
            }
        });

    }
}
