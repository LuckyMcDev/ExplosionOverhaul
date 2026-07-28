package dev.perxenic.explosionoverhaul.mixin;

import dev.perxenic.explosionoverhaul.ServerConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends Entity {
    public PrimedTntMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("resource")
    @Inject(method = "tick()V", at = @At(value= "TAIL"))
    public void addSparks(CallbackInfo ci)
    {
        if (!ServerConfig.enableFuseSparks) return;

        if (level().isClientSide()) return;
        var serverLevel = (ServerLevel) level();

        var position = position();
        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                position.x,
                position.y + 1,
                position.z,
                2,
                0.0,
                0.0,
                0.0,
                ServerConfig.smokeParticleSpread
        );
    }
}
