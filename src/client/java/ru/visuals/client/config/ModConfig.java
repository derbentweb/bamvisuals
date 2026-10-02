package ru.visuals.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import ru.visuals.client.features.Feature;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Простое JSON-хранилище: для каждой фичи по id храним enabled + код клавиши.
 * Файл лежит в .minecraft/config/visualsmod.json
 */
public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("visualsmod.json");

    private static Map<String, Entry> data = new HashMap<>();

    public static class Entry {
        public boolean enabled;
        public int key = GLFW.GLFW_KEY_UNKNOWN;
        // произвольные доп. параметры (например слоты предметов для автосвапа)
        public Map<String, String> extra = new HashMap<>();
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            data = new HashMap<>();
            return;
        }
        try (Reader r = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            Map<String, Entry> loaded = GSON.fromJson(r, new com.google.gson.reflect.TypeToken<Map<String, Entry>>(){}.getType());
            data = loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            data = new HashMap<>();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(data, w);
            }
        } catch (IOException ignored) {
        }
    }

    public static void applyTo(Feature feature) {
        Entry e = data.get(feature.getId());
        if (e == null) return;
        feature.setEnabled(e.enabled);
        if (feature.getKeyBinding() != null && e.key != GLFW.GLFW_KEY_UNKNOWN) {
            feature.getKeyBinding().setBoundKey(
                    net.minecraft.client.util.InputUtil.Type.KEYSYM.createFromCode(e.key));
            KeyBinding.updateKeysByCode();
        }
    }

    public static void persist(Feature feature) {
        Entry e = data.computeIfAbsent(feature.getId(), k -> new Entry());
        e.enabled = feature.isEnabled();
        if (feature.getKeyBinding() != null) {
            e.key = feature.getKeyBinding().getDefaultKey().getCode();
        }
        save();
    }

    public static Entry entryFor(String id) {
        return data.computeIfAbsent(id, k -> new Entry());
    }
}
