package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ЗАГОТОВКА. Замена неба на звёзды — так же через WorldRenderEvents.SKY:
 * рисуешь свой купол/квады со звёздами вместо ванильного рендера звёзд
 * (или поверх него, отключив ванильные через mixin в WorldRenderer#renderSky).
 * Для оптимизации — звёзды как один статический VertexBuffer, который
 * строится один раз и просто вращается матрицей, а не пересобирается
 * каждый кадр.
 */
public class SkyStarsFeature extends Feature {
    public SkyStarsFeature() {
        super("sky_stars", "Звёздное небо",
                "Заменяет небо на космос со звёздами. (в разработке)", FeatureCategory.VISUALS);
    }
}
