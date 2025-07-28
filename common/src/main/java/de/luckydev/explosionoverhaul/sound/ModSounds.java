package de.luckydev.explosionoverhaul.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static de.luckydev.explosionoverhaul.ExplosionOverhaul.MOD_ID;

public class ModSounds {
    public static final Identifier RINGING_ID = Identifier.of(MOD_ID, "ear_ringing_after_explosion");
    public static SoundEvent RINGING;

    public static void init() {
        RINGING = Registry.register(Registries.SOUND_EVENT, RINGING_ID, SoundEvent.of(RINGING_ID));
    }
}