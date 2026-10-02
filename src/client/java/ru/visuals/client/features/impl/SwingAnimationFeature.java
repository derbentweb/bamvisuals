package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ЗАГОТОВКА. "Держит меч под наклоном" — тоже mixin в HeldItemRenderer,
 * подменяешь угол наклона предмета в зависимости от swingProgress
 * (прогресс анимации удара) по кастомной кривой вместо ванильной,
 * плюс несколько пресетов кривых = "разные режимы".
 */
public class SwingAnimationFeature extends Feature {
    public int mode = 0; // 0,1,2... разные кривые наклона

    public SwingAnimationFeature() {
        super("swing_animation", "Swing Animation",
                "Наклон меча при ударе, разные режимы. (в разработке)", FeatureCategory.VISUALS);
    }
}
