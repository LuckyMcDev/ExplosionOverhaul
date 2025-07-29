package de.luckydev.explosionoverhaul.sound;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKeys;

import static de.luckydev.explosionoverhaul.ExplosionOverhaul.MOD_ID;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MOD_ID, RegistryKeys.SOUND_EVENT);

    public static final Identifier RINGING_ID = Identifier.of(MOD_ID, "ear_ringing_after_explosion");

    public static final RegistrySupplier<SoundEvent> RINGING = SOUNDS.register("ear_ringing_after_explosion",
            () -> SoundEvent.of(RINGING_ID));

    public static void init() {
        SOUNDS.register();
    }
}