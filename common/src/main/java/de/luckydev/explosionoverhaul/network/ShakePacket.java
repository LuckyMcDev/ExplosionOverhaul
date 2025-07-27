package de.luckydev.explosionoverhaul.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import de.luckydev.explosionoverhaul.ExplosionOverhaul;

public record ShakePacket(Vec3d origin, float radius, float strength, int duration) implements CustomPayload {
    public static final Id<ShakePacket> TYPE = new Id<>(Identifier.of(ExplosionOverhaul.MOD_ID, "screen_shake"));

    public static final PacketCodec<RegistryByteBuf, ShakePacket> CODEC = PacketCodec.of((value, buf) -> {
        buf.writeDouble(value.origin().x);
        buf.writeDouble(value.origin().y);
        buf.writeDouble(value.origin().z);
        buf.writeFloat(value.radius());
        buf.writeFloat(value.strength());
        buf.writeInt(value.duration());
    }, buf -> {
        Vec3d origin = new Vec3d(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
        float radius = buf.readFloat();
        float strength = buf.readFloat();
        int duration = buf.readInt();
        return new ShakePacket(origin, radius, strength, duration);
    });

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}