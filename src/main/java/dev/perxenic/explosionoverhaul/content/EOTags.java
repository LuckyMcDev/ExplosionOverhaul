package dev.perxenic.explosionoverhaul.content;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static dev.perxenic.explosionoverhaul.ExplosionOverhaul.eoLoc;

public class EOTags {
    public static class Blocks {
        public static TagKey<Block> DO_NOT_LAUNCH = BlockTags.create(eoLoc("do_not_launch"));
    }
}
