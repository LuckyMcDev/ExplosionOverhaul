package de.luckydev.explosionoverhaul.mixin;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlayerCameraShakeMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyScreenShakeToCamera(CallbackInfo ci) {
        if (!ExplosionOverhaul.CONFIG.enableScreenShake) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        float shakeX = ScreenShakeHandler.getShakeX();
        float shakeY = ScreenShakeHandler.getShakeY();

        if (shakeX != 0f || shakeY != 0f) {
            // Apply shake to camera rotation with configurable multiplier
            float multiplier = ExplosionOverhaul.CONFIG.shakeIntensityMultiplier;
            float pitchShake = shakeY * multiplier;
            float yawShake = shakeX * multiplier;

            // Store original values to restore later
            float originalPitch = player.getPitch();
            float originalYaw = player.getYaw();

            // Apply shake
            player.setPitch(originalPitch + pitchShake);
            player.setYaw(originalYaw + yawShake);
        }
    }
}