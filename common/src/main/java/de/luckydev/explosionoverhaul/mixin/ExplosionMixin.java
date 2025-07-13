package de.luckydev.explosionoverhaul.mixin;

import de.luckydev.explosionoverhaul.explosion.ExplosionPhysicsHandler;
import de.luckydev.explosionoverhaul.explosion.ExplosionSoundHandler;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private World world;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void onPre(boolean spawnParticles, CallbackInfo ci) {
        Explosion explosion = (Explosion) (Object) this;
        ExplosionPhysicsHandler.onPre(world, explosion);

        if (!world.isClient) {
            Vec3d pos = explosion.getPosition();
            Identifier sound = Identifier.of("explosionoverhaul", "explosion.ear_ringing_after_explosion");

            for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
                if (player.squaredDistanceTo(pos) < 20.0 * 20.0) {
                    ExplosionSoundHandler.play(world, player.getPos(), sound, SoundCategory.AMBIENT, 1.0f, 1.0f);
                }
            }
        }
    }

    @Inject(method = "affectWorld", at = @At("TAIL"))
    private void onPost(boolean spawnParticles, CallbackInfo ci) {
        Explosion explosion = (Explosion) (Object) this;
        ExplosionPhysicsHandler.onDetonate(world, explosion);
    }
}
