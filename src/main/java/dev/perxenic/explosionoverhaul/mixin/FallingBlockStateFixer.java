package dev.perxenic.explosionoverhaul.mixin;

import dev.perxenic.explosionoverhaul.ExplosionOverhaul;
import dev.perxenic.explosionoverhaul.ServerConfig;
import dev.perxenic.explosionoverhaul.content.EOTags;
import dev.perxenic.explosionoverhaul.infra.FallingBlockEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockStateFixer extends Entity implements FallingBlockEntityData {
    @Shadow
    private BlockState blockState;

    @Unique
    public boolean explosionOverhaul$createdFromExplosion;

    public FallingBlockStateFixer(EntityType<?> entityType, Level level) {
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
    @Redirect(
            method = "Lnet/minecraft/world/entity/item/FallingBlockEntity;tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    public boolean onBlockPlaced(Level instance, BlockPos pos, BlockState newState, int flags) {
        var blockPos = blockPosition();
        var block = blockState.getBlock();
        ExplosionOverhaul.LOGGER.info("Test123");
        if (level().isClientSide()
                || !explosionOverhaul$getCreatedFromExplosion()
                || !blockState.is(EOTags.Blocks.UPDATE_ON_LAND)
                || !ServerConfig.rePlaceTaggedBlocks
        ) return level().setBlock(blockPos, this.blockState, 3);

        ExplosionOverhaul.LOGGER.info("Test234");

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
}
