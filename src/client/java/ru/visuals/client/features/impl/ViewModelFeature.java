package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ЗАГОТОВКА. Настройка положения рук — mixin в HeldItemRenderer
 * (методы renderFirstPersonItem / renderItem), где перед рендером
 * применяешь дополнительный MatrixStack.translate/rotate по трём осям
 * со значениями из конфига (X/Y/Z offset + pitch/yaw/roll), которые
 * выставляются ползунками в GUI.
 */
public class ViewModelFeature extends Feature {
    public float offsetX = 0, offsetY = 0, offsetZ = 0;
    public float pitch = 0, yaw = 0, roll = 0;

    public ViewModelFeature() {
        super("view_model", "View Model",
                "Настройка положения рук от первого лица. (в разработке)", FeatureCategory.VISUALS);
    }
}
