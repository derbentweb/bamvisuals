package ru.visuals.client.features.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

public class JumpCircleFeature extends Feature {

    private boolean wasOnGround = true;

    public JumpCircleFeature() {
        super("jump_circle", "Круг при прыжке",
                "Рисует лёгкое кольцо частиц в момент прыжка.", FeatureCategory.VISUALS);
    }

    @Override
    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();
        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0.1) {
            spawnRing(mc);
        }
        wasOnGround = onGround;
    }

    private void spawnRing(MinecraftClient mc) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        int points = 12; // немного точек — дёшево по кадру, не бьёт по FPS
        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            double ox = Math.cos(angle) * 0.4;
            double oz = Math.sin(angle) * 0.4;
            mc.world.addParticleClient(ParticleTypes.CLOUD, x + ox, y + 0.05, z + oz, 0, 0.02, 0);
        }
    }
}
