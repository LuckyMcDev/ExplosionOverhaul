package dev.perxenic.explosionoverhaul.mixin;

import dev.perxenic.explosionoverhaul.ServerConfig;
import dev.perxenic.explosionoverhaul.content.EOTags;
import dev.perxenic.explosionoverhaul.infra.FallingBlockEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockMixin extends Entity implements FallingBlockEntityData {
    @Shadow
    private BlockState blockState;

    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    public boolean explosionOverhaul$createdFromExplosion;

    public FallingBlockMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void explosionOverhaul$setCreatedFromExplosion(boolean value) {
        explosionOverhaul$createdFromExplosion = value;
    }

    @Override
    public boolean explosionOverhaul$getCreatedFromExplosion() {
        return explosionOverhaul$createdFromExplosion;
    }

    // If falling block was created from explosion and block has tag telling it to update when it lands run logic as if player replaced block at this location
    // This is used to prevent fences from looking odd when they have been placed after an explosion
    @SuppressWarnings("resource")
    @Redirect(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    public boolean onBlockPlaced(Level instance, BlockPos pos, BlockState newState, int flags) {
        var blockPos = blockPosition();
        var block = blockState.getBlock();
        if (level().isClientSide()
                || !explosionOverhaul$getCreatedFromExplosion()
                || !blockState.is(EOTags.Blocks.UPDATE_ON_LAND)
                || !ServerConfig.rePlaceTaggedBlocks
        ) return level().setBlock(blockPos, this.blockState, 3);

        var posVec = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        var replacementState = block.getStateForPlacement(new BlockPlaceContext(
                level(),
                null,
                InteractionHand.MAIN_HAND,
                new ItemStack(block.asItem()),
                new BlockHitResult(posVec, Direction.UP, blockPos, false)
        ));

        if (replacementState == null) return level().setBlock(blockPos, this.blockState, 3);

        return level().setBlock(blockPos, replacementState, 3);
    }

    @SuppressWarnings("resource")
    @Inject(method = "tick()V", at = @At(value= "TAIL"))
    public void addSmokeParticles(CallbackInfo ci)
    {
        if (!explosionOverhaul$createdFromExplosion) return;
        if (!ServerConfig.enableSmokeTrails) return;

        if (level().isClientSide()) return;
        var serverLevel = (ServerLevel) level();

        if (getRandom().nextDouble() > ServerConfig.smokeParticleChance) return;

        var position = position();
        serverLevel.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                position.x,
                position.y,
                position.z,
                1,
                0.0,
                0.0,
                0.0,
                ServerConfig.smokeParticleSpread
        );
    }
}
