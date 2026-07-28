package de.luckydev.explosionoverhaul.mixin;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.explosion.ExplosionPhysicsHandler;
import de.luckydev.explosionoverhaul.sound.ModSounds;
import de.luckydev.explosionoverhaul.shake.PositionedScreenShake;
import de.luckydev.explosionoverhaul.shake.ScreenShakeHandler;
import de.luckydev.explosionoverhaul.effects.ExplosionVisualEffects;
import de.luckydev.explosionoverhaul.effects.ExplosionParticleHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow @Final private World world;

    @Shadow public abstract Vec3d getPosition();

    @Shadow @Final private ObjectArrayList<BlockPos> affectedBlocks;

    @Shadow public float power;

    @Shadow
    @Final
    private Explosion.DestructionType destructionType;

    @Unique
    private boolean explosionoverhaul$damagesWorld() {
        return destructionType != Explosion.DestructionType.KEEP &&
                destructionType != Explosion.DestructionType.TRIGGER_BLOCK;
    }

    // Hook into affectWorld at HEAD to handle our custom logic before vanilla processing
    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void onPreAffectWorld(boolean spawnParticles, CallbackInfo ci) {
        Vec3d pos = getPosition();

        // Handle client-side effects
        if (world.isClient()) {
            ExplosionMixin$handleClientSideEffects(pos);
        }

        // Handle server-side physics (debris spawning)
        if (!world.isClient() && explosionoverhaul$damagesWorld()) {
            ExplosionPhysicsHandler.processExplosion(world, pos, affectedBlocks, power);
        }
    }

    @Unique
    @Environment(EnvType.CLIENT)
    private void ExplosionMixin$handleClientSideEffects(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        ClientWorld clientWorld = (ClientWorld) world;
        double distanceToPlayer = client.player.getPos().distanceTo(pos);

        // Handle sound effects
        if (ExplosionOverhaul.CONFIG.playRingingSound) {
            // Use the registry supplier's get() method to retrieve the sound event
            SoundEvent ringingSound = ModSounds.RINGING.get();
            if (ringingSound != null) {
                world.playSound(
                        pos.x, pos.y, pos.z,
                        ringingSound,
                        SoundCategory.AMBIENT,
                        10.0F,
                        (float) ((1.0F + (Math.random()) * 0.2F) * 0.7F),
                        false
                );

                if (ExplosionOverhaul.CONFIG.debugLogging) {
                    ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Playing ringing sound at {}", pos);
                }
            } else {
                ExplosionOverhaul.LOGGER.warn("[ExplosionOverhaul] RINGING sound event is null!");
            }
        }

        // Handle visual effects
        if (ExplosionOverhaul.CONFIG.shouldRenderVisualEffects(distanceToPlayer)) {
            // Create flash effect first (immediate)
            if (ExplosionOverhaul.CONFIG.enableFlashEffect) {
                ExplosionVisualEffects.createFlashEffect(clientWorld, pos, power);
            }

            // Create mushroom cloud effect
            if (ExplosionOverhaul.CONFIG.enableMushroomCloud) {
                ExplosionVisualEffects.createMushroomCloud(clientWorld, pos, power);
            }

            // Create additional particle effects
            if (ExplosionOverhaul.CONFIG.enableDebrisParticles) {
                ExplosionParticleHandler.createCompleteExplosionEffects(clientWorld, pos, power);
            }

            if (ExplosionOverhaul.CONFIG.debugLogging) {
                ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Created visual effects at {} with power {}", pos, power);
            }
        }

        // Handle screen shake
        if (ExplosionOverhaul.CONFIG.enableScreenShake) {
            float shakeStrength = ExplosionOverhaul.CONFIG.calculateShakeStrength(power);
            float shakeRadius = ExplosionOverhaul.CONFIG.calculateShakeRadius(power);
            int shakeDuration = ExplosionOverhaul.CONFIG.getShakeDuration(world.random);

            ScreenShakeHandler.addShake(new PositionedScreenShake(pos, shakeRadius, shakeStrength, shakeDuration));

            if (ExplosionOverhaul.CONFIG.debugLogging) {
                ExplosionOverhaul.LOGGER.info("[ExplosionOverhaul] Added screen shake - Strength: {}, Radius: {}, Duration: {}",
                        shakeStrength, shakeRadius, shakeDuration);
            }
        }
    }
}