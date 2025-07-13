package de.luckydev.explosionoverhaul.explosion;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ExplosionSoundHandler {
    private static final Random RANDOM = new Random();

    /**
     * Plays a registered sound by its identifier.
     */
    public static void play(World world, Vec3d pos, Identifier soundId, SoundCategory category, float volume, float pitch) {
        if (world == null || soundId == null) return;
        world.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvent.of(soundId),
                category,
                volume,
                pitch
        );
    }

    /**
     * Overload for BlockPos instead of Vec3d.
     */
    public static void play(World world, BlockPos pos, Identifier soundId, SoundCategory category, float volume, float pitch) {
        play(world, Vec3d.ofCenter(pos), soundId, category, volume, pitch);
    }

    /**
     * Plays a random sound from a list of sound identifiers.
     */
    public static void playRandom(World world, Vec3d pos, List<Identifier> soundPool, SoundCategory category, float volume, float pitch) {
        if (soundPool == null || soundPool.isEmpty()) return;
        Identifier selected = soundPool.get(RANDOM.nextInt(soundPool.size()));
        play(world, pos, selected, category, volume, pitch);
    }

    /**
     * Convenience overload for pitch variance.
     */
    public static void playRandomPitch(World world, Vec3d pos, Identifier soundId, SoundCategory category, float volume, float minPitch, float maxPitch) {
        float pitch = minPitch + RANDOM.nextFloat() * (maxPitch - minPitch);
        play(world, pos, soundId, category, volume, pitch);
    }
}
