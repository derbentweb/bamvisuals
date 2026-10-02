package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ЗАГОТОВКА. "Шейдеры рук" — кастомный GLSL шейдер (core-шейдер, формат
 * Minecraft 1.21.x, JSON + .vsh/.fsh в assets/visualsmod/shaders/core),
 * применяемый только к RenderLayer рук/предмета в руке. Это отдельная
 * большая тема (ShaderProgram API в 1.21.11 могли поменять — сверься
 * с актуальным Fabric wiki по core shaders перед реализацией).
 */
public class HandShaderFeature extends Feature {
    public HandShaderFeature() {
        super("hand_shader", "Шейдер рук",
                "Красивый кастомный шейдер для рук/предмета. (в разработке)", FeatureCategory.VISUALS);
    }
}
