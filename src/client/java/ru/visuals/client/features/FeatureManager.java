package ru.visuals.client.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.config.ModConfig;
import ru.visuals.client.features.impl.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FeatureManager {

    private static final Map<String, Feature> FEATURES = new LinkedHashMap<>();

    public static void registerAll() {
        // --- Оптимизация ---
        register(new OptimizationFeature());

        // --- Визуалы ---
        register(new AutoDawnFogFeature());
        register(new SkyStarsFeature());
        register(new WinterFeature());
        register(new FullbrightFeature());
        register(new DamageParticlesFeature());
        register(new ViewModelFeature());
        register(new SwingAnimationFeature());
        register(new TrailFeature());
        register(new JumpCircleFeature());
        register(new GroundLabelFeature());
        register(new ItemPhysicsFeature());
        register(new HandShaderFeature());

        // --- Быстрые действия (не боевая автоматизация, а хоткеи) ---
        register(new ElytraHelperFeature());
        register(new AutoSwapFeature());

        // Загружаем сохранённые состояния/бинды из конфига
        ModConfig.load();
        for (Feature f : FEATURES.values()) {
            ModConfig.applyTo(f);
        }

        // Регистрируем KeyBinding в Minecraft для тех фич, у кого он есть
        for (Feature f : FEATURES.values()) {
            if (f.getKeyBinding() != null) {
                KeyBindingHelper.registerKeyBinding(f.getKeyBinding());
            }
        }

        // Тик: проверяем нажатия триггерных биндов + обычный tick() у фич
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Feature f : FEATURES.values()) {
                KeyBinding kb = f.getKeyBinding();
                if (kb != null && f.isTriggerType()) {
                    while (kb.wasPressed()) {
                        f.onKeyPressed();
                    }
                } else if (kb != null && kb.wasPressed() && !f.isTriggerType()) {
                    // тумблер тоже можно вешать на прямой бинд, если задан
                    f.toggle();
                }
                if (f.isEnabled()) {
                    f.tick();
                }
            }
        });
    }

    private static void register(Feature feature) {
        FEATURES.put(feature.getId(), feature);
    }

    public static Feature get(String id) {
        return FEATURES.get(id);
    }

    public static List<Feature> all() {
        return new ArrayList<>(FEATURES.values());
    }

    public static List<Feature> byCategory(FeatureCategory category) {
        List<Feature> out = new ArrayList<>();
        for (Feature f : FEATURES.values()) {
            if (f.getCategory() == category) out.add(f);
        }
        return out;
    }
}
