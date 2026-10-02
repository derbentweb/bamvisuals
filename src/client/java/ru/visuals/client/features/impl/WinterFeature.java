package ru.visuals.client.features.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;
import ru.visuals.client.gui.SnowRenderer;

/**
 * "Зима везде" без нагрузки на мир. Снег рисуется поверх экрана (HUD-слой):
 * до 150 маленьких прямоугольников и два прозрачных градиента по краям для
 * холодной дымки. Мир, чанки, частицы и туман не трогаем, поэтому нет конфликтов
 * с Sodium/Iris и просадок от тысяч частиц.
 *
 * Защита по FPS: если кадров меньше 45 — хлопьев вдвое меньше, меньше 25 — снег
 * временно выключается. Режим "Оптимизация визуалов" тоже вдвое режет количество.
 */
public class WinterFeature extends Feature {

    public static volatile boolean ENABLED = false;

    private static final int MAX_FLAKES = 150;
    private static final SnowRenderer SNOW = new SnowRenderer(MAX_FLAKES);

    public WinterFeature() {
        super("winter", "Зима",
                "Лёгкий снегопад и морозная дымка поверх экрана, почти не влияет на FPS.",
                FeatureCategory.VISUALS);
    }

    @Override
    protected void onEnable() {
        ENABLED = true;
    }

    @Override
    protected void onDisable() {
        ENABLED = false;
    }

    /** Вызывается из HudRenderCallback (см. VisualsModClient). */
    public static void renderHud(DrawContext ctx) {
        if (!ENABLED) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;

        int count = MAX_FLAKES;
        if (OptimizationFeature.PERFORMANCE_MODE) count /= 2;
        int fps = mc.getCurrentFps();
        if (fps > 0 && fps < 25) return;
        if (fps > 0 && fps < 45) count /= 2;

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();

        // морозная дымка сверху и снизу
        ctx.fillGradient(0, 0, w, h / 6, 0x40DDEEFF, 0x00DDEEFF);
        ctx.fillGradient(0, h - h / 6, w, h, 0x00DDEEFF, 0x38DDEEFF);

        SNOW.render(ctx, w, h, count, 0.015f);
    }
}
