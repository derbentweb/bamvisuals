package ru.visuals.client.gui;

import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/**
 * Дешёвый снег: хлопья живут в нормализованных координатах (0..1), на кадр
 * это просто N маленьких прямоугольников через DrawContext.fill. Никаких
 * частиц мира, чанков, текстур и аллокаций в цикле.
 */
public final class SnowRenderer {

    private final float[] x, y, speed, phase, depth;
    private final Random rnd = new Random();
    private long last = System.nanoTime();

    public SnowRenderer(int max) {
        x = new float[max];
        y = new float[max];
        speed = new float[max];
        phase = new float[max];
        depth = new float[max];
        for (int i = 0; i < max; i++) {
            respawn(i, true);
        }
    }

    private void respawn(int i, boolean anywhere) {
        x[i] = rnd.nextFloat();
        y[i] = anywhere ? rnd.nextFloat() : -0.02f;
        depth[i] = rnd.nextFloat();
        speed[i] = 0.05f + depth[i] * 0.10f;
        phase[i] = rnd.nextFloat() * 6.2831f;
    }

    public void render(DrawContext ctx, int w, int h, int count, float wind) {
        long now = System.nanoTime();
        float dt = Math.min((now - last) / 1_000_000_000f, 0.05f);
        last = now;
        float time = now / 1_000_000_000f;

        int n = Math.min(count, x.length);
        for (int i = 0; i < n; i++) {
            y[i] += speed[i] * dt;
            x[i] += (wind + (float) Math.sin(time * 0.8f + phase[i]) * 0.012f) * dt;
            if (y[i] > 1.02f) respawn(i, false);
            if (x[i] > 1f) x[i] -= 1f;
            if (x[i] < 0f) x[i] += 1f;

            int size = depth[i] < 0.5f ? 1 : (depth[i] < 0.85f ? 2 : 3);
            int alpha = 90 + (int) (depth[i] * 150);
            int px = (int) (x[i] * w);
            int py = (int) (y[i] * h);
            ctx.fill(px, py, px + size, py + size, (alpha << 24) | 0xFFFFFF);
        }
    }
}
