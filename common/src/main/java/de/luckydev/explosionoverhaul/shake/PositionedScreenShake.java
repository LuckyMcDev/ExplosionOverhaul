package de.luckydev.explosionoverhaul.shake;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * A screen shake at a world position that fades with distance.
 */
public class PositionedScreenShake extends ScreenShake {
    private final Vec3d origin;
    private final float radius;

    public PositionedScreenShake(Vec3d origin, float radius, float strength, int duration) {
        super(strength, duration);
        this.origin = origin;
        this.radius = radius;
    }

    @Override
    public float getIntensity(ClientPlayerEntity player) {
        Vec3d playerPos = player.getPos();
        double dist = playerPos.distanceTo(origin);
        if (dist > radius) return 0f;

        float distFade = 1f - (float) dist / radius;
        float ageFade = 1f - (float) age / duration;
        return strength * distFade * ageFade;
    }
}
