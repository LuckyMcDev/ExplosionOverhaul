package de.luckydev.explosionoverhaul.mixin;

import de.luckydev.explosionoverhaul.explosion.ExplosionPhysicsHandler;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private World world;

    // Note: explosion is "this" in Mixin
    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void onPre(boolean spawnParticles, CallbackInfo ci) {
        ExplosionPhysicsHandler.onPre(world, (Explosion) (Object) this);
    }

    @Inject(method = "affectWorld", at = @At("TAIL"))
    private void onPost(boolean spawnParticles, CallbackInfo ci) {
        ExplosionPhysicsHandler.onDetonate(world, (Explosion) (Object) this);
    }
}

