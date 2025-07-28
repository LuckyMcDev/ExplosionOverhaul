package de.luckydev.explosionoverhaul.shake;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * A camera shake centered on the player.
 */
@Environment(EnvType.CLIENT)
public class ScreenShake {
    protected final float strength;
    protected final int duration;
    protected int age = 0;

    public ScreenShake(float strength, int duration) {
        this.strength = strength;
        this.duration = duration;
    }

    public boolean tickAndExpired() {
        return ++age > duration;
    }

    /**
     * Returns the shake intensity at the given player. Override for positional shake.
     */
    public float getIntensity(ClientPlayerEntity player) {
        float ageFade = 1f - (float) age / duration;
        return strength * ageFade;
    }
}
