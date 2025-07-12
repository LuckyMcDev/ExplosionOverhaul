package de.luckydev.explosionoverhaul.util;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static de.luckydev.explosionoverhaul.ExplosionOverhaul.MOD_ID;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

public class RegistryHelper {

    public static <T extends Item> T registerItem(Class<T> itemClass, Item.Settings settings) {
        Identifier id = resolveId(itemClass);
        T item = instantiateItem(itemClass, settings);
        Registry.register(Registries.ITEM, id, item);
        return item;
    }

    // Keep the old method for backward compatibility
    public static <T extends Item> T registerItem(Class<T> itemClass) {
        return registerItem(itemClass, new Item.Settings());
    }

    public static <T extends Block> T registerBlock(Class<T> blockClass) {
        Identifier id = resolveId(blockClass);
        T block = instantiateBlock(blockClass);
        Registry.register(Registries.BLOCK, id, block);

        // Register BlockItem
        BlockItem blockItem = new BlockItem(block, new Item.Settings());
        Registry.register(Registries.ITEM, id, blockItem);

        return block;
    }

    private static Identifier resolveId(Class<?> clazz) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Identifier.class)) {
                    field.setAccessible(true);
                    return (Identifier) field.get(null);
                }
            }
        } catch (Exception ignored) {}

        String fallback = clazz.getSimpleName()
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toLowerCase(Locale.ROOT);
        return id(fallback);
    }

    private static <T extends Item> T instantiateItem(Class<T> clazz, Item.Settings settings) {
        try {
            try {
                return clazz.getConstructor(Item.Settings.class).newInstance(settings);
            } catch (NoSuchMethodException e) {
                return clazz.getConstructor().newInstance(); // fallback to default constructor
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate item: " + clazz.getName(), e);
        }
    }

    private static <T extends Block> T instantiateBlock(Class<T> clazz) {
        try {
            try {
                return clazz.getConstructor(Block.Settings.class).newInstance(Block.Settings.create());
            } catch (NoSuchMethodException e) {
                return clazz.getConstructor().newInstance(); // fallback to default constructor
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate block: " + clazz.getName(), e);
        }
    }

    // Helper method to create Identifier with your mod ID
    private static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}