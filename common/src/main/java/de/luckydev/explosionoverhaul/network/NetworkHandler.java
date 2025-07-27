package de.luckydev.explosionoverhaul.network;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.shake.PositionedScreenShake;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collection;

public class NetworkHandler {

    @Environment(EnvType.CLIENT)
    public static void initClient() {

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                ShakePacket.TYPE,
                ShakePacket.CODEC,
                (packet, context) -> {
                    if (context.getEnvironment() == Env.CLIENT) {
                        context.queue(() -> {
                            ScreenShakeHandler.addShake(new PositionedScreenShake(
                                    packet.origin(),
                                    packet.radius(),
                                    packet.strength(),
                                    packet.duration()
                            ));
                        });
                    }
                }
        );
        ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Client network handler initialized");
    }

    public static void initServer() {

        NetworkManager.registerS2CPayloadType(ShakePacket.TYPE, ShakePacket.CODEC);

        ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Server network handler initialized");
    }

    public static void sendScreenShake(ServerWorld world, Vec3d origin, float radius, float strength, int duration) {
        double broadcastRadius = radius * 2;
        Collection<ServerPlayerEntity> players = world.getPlayers(player ->
                player.getPos().distanceTo(origin) <= broadcastRadius
        );

        ShakePacket packet = new ShakePacket(origin, radius, strength, duration);

        ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Sending shake packet: pos={}, radius={}, strength={}, duration={}",
                origin, radius, strength, duration);

        for (ServerPlayerEntity player : players) {
            NetworkManager.sendToPlayer(player, packet);
        }
    }
}