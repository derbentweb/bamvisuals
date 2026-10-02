package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * Реализация в FullbrightLightmapMixin — он читает FullbrightFeature.ENABLED
 * и, если true, форсирует полную яркость лайтмапа. Оптимизировано: мы не
 * пересчитываем ничего лишнего, просто подменяем итоговый цвет.
 */
public class FullbrightFeature extends Feature {

    public static volatile boolean ENABLED = false;

    public FullbrightFeature() {
        super("fullbright", "Fullbright", "Убирает тени, всё видно одинаково ярко.", FeatureCategory.VISUALS);
    }

    @Override
    protected void onEnable() {
        ENABLED = true;
    }

    @Override
    protected void onDisable() {
        ENABLED = false;
    }
}
