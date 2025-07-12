package de.luckydev.explosionoverhaul.shake;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScreenShakeHandler {
    private static final List<ScreenShake> activeShakes = new CopyOnWriteArrayList<>();

    // Store the current shake offset
    private static float currentShakeX = 0f;
    private static float currentShakeY = 0f;

    public static void addShake(ScreenShake shake) {
        activeShakes.add(shake);
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || player.isSpectator()) {
            currentShakeX = 0f;
            currentShakeY = 0f;
            return;
        }

        float totalIntensity = 0f;

        // Remove expired shakes and calculate total intensity
        activeShakes.removeIf(ScreenShake::tickAndExpired);

        for (ScreenShake shake : activeShakes) {
            totalIntensity += shake.getIntensity(player);
        }

        // Apply shake offset
        if (totalIntensity > 0f) {
            double angle = Math.random() * Math.PI * 2;
            currentShakeX = (float) (Math.cos(angle) * totalIntensity);
            currentShakeY = (float) (Math.sin(angle) * totalIntensity);
        } else {
            currentShakeX = 0f;
            currentShakeY = 0f;
        }
    }

    public static float getShakeX() {
        return currentShakeX;
    }

    public static float getShakeY() {
        return currentShakeY;
    }
}