package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ЗАГОТОВКА. Реалистичная физика предметов на земле — на клиенте
 * (сервер физику ItemEntity не отдаст точнее, чем она есть) можно
 * сделать чисто визуальную надстройку: mixin в ItemEntity#tick или
 * рендер-хук, который дорисовывает "оседание"/поворот предмета по
 * рэйкасту до земли под ним, сглаживая ванильное вращение.
 * Для настоящей физики (наклон по неровностям блока и т.п.) нужен
 * рэйкаст нормали поверхности под предметом каждые несколько тиков
 * (не каждый кадр — дорого).
 */
public class ItemPhysicsFeature extends Feature {
    public ItemPhysicsFeature() {
        super("item_physics", "Физика предметов",
                "Реалистичное падение и лежание предметов на земле. (в разработке)",
                FeatureCategory.VISUALS);
    }
}
