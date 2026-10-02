package ru.visuals.client.config;

/**
 * Разделы меню. Добавляй новую категорию сюда, если нужна отдельная вкладка.
 */
public enum FeatureCategory {
    PERFORMANCE("Оптимизация"),
    VISUALS("Визуалы"),
    COMBAT_QOL("Быстрые действия"),
    CUSTOM("Твой мод");

    public final String ruName;

    FeatureCategory(String ruName) {
        this.ruName = ruName;
    }
}
