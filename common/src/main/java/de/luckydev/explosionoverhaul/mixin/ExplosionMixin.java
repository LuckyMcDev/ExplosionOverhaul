package de.luckydev.explosionoverhaul.mixin;

import de.luckydev.explosionoverhaul.ExplosionOverhaul;
import de.luckydev.explosionoverhaul.explosion.ExplosionPhysicsHandler;
import de.luckydev.explosionoverhaul.sound.ModSounds;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow @Final private World world;

    @Shadow public abstract Vec3d getPosition();

    @Shadow @Final private ObjectArrayList<BlockPos> affectedBlocks;

    @Shadow public float power;

    // Hook into affectWorld at HEAD to handle our custom logic before vanilla processing
    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void onPreAffectWorld(boolean spawnParticles, CallbackInfo ci) {
        Vec3d pos = getPosition();

        // Handle sound effects
        if (ExplosionOverhaul.CONFIG.playRingingSound) {
            System.out.println("Ringing sound: " + ModSounds.RINGING);
            world.playSound(pos.x, pos.y, pos.z, ModSounds.RINGING, SoundCategory.AMBIENT, 10.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        // Process our custom explosion physics before vanilla block destruction
        ExplosionPhysicsHandler.processExplosion(world, pos, affectedBlocks, power);
    }
}