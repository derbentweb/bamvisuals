package ru.visuals.client.features.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3f;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * Простой цветной след частиц позади игрока при движении.
 * Цвет настраивается через RGB в GUI (color).
 */
public class TrailFeature extends Feature {

    public volatile int color = 0x55CCFF; // настраивается из GUI

    public TrailFeature() {
        super("trail", "Trail",
                "Цветной след позади игрока при беге. Цвет настраивается.", FeatureCategory.VISUALS);
    }

    @Override
    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getVelocity().horizontalLength() < 0.08) return; // только в движении

        float r = ColorHelper.getRed(color) / 255f;
        float g = ColorHelper.getGreen(color) / 255f;
        float b = ColorHelper.getBlue(color) / 255f;

        DustParticleEffect effect = new DustParticleEffect(new Vector3f(r, g, b), 1.0f);
        double x = mc.player.getX();
        double y = mc.player.getY() + 0.1;
        double z = mc.player.getZ();
        mc.world.addParticleClient(effect, x, y, z, 0, 0, 0);
    }
}
