package dev.perxenic.explosionoverhaul.datagen;

import dev.perxenic.explosionoverhaul.ExplosionOverhaul;
import dev.perxenic.explosionoverhaul.content.EOTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class EOBlockTagProvider extends BlockTagsProvider {
    public EOBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ExplosionOverhaul.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EOTags.Blocks.DO_NOT_LAUNCH)
                .addTag(BlockTags.REPLACEABLE)
                .addTag(BlockTags.BEDS)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.SUGAR_CANE);
    }
}
