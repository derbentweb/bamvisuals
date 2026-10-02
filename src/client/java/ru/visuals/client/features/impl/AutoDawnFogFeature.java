package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ЗАГОТОВКА. Реализация красивого тумана/рассвета/радуги требует:
 *  1. Mixin в BackgroundRenderer (метод applyFog / render) — подмена
 *     плотности/цвета тумана по FogShape в зависимости от времени суток.
 *  2. Отдельный рендер солнца/радуги — либо через WorldRenderEvents.SKY
 *     (Fabric API) рисованием доп. геометрии, либо кастомная текстура
 *     через SkyProperties (DimensionType), если хочешь подменить рендер
 *     ванильного неба целиком.
 *  3. Три режима — просто enum RUS: "Ночной", "Рассвет", "Дневной" — и
 *     набор параметров тумана/цвета под каждый, переключаемые в GUI.
 *  4. Дешевизна: не пересчитывать цвет тумана каждый кадр с нуля — кэшировать
 *     на изменение currentTime/погоды, а не на каждый draw call.
 *
 * Начни с WorldRenderEvents.SKY — это безопаснее для конфликтов с шейдерпаками
 * и другими модами неба, чем полная замена SkyProperties.
 */
public class AutoDawnFogFeature extends Feature {
    public AutoDawnFogFeature() {
        super("auto_dawn_fog", "Auto Dawn/Fog",
                "Красивый туман, радуга и солнце. Режимы: ночной/рассвет/другой. (в разработке)",
                FeatureCategory.VISUALS);
    }
}
