package ru.visuals.client.features.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

import java.util.HashMap;
import java.util.Map;

/**
 * Клиентское определение "по существу ударили": у LivingEntity есть поле
 * hurtTime, которое сервер выставляет в maxHurtTime при получении урона и
 * оно тикает вниз каждый клиентский тик. Мы ловим момент скачка вверх —
 * значит только что был урон — и спавним частицы в точке сущности.
 * Никакого мixin в LivingEntity#damage не нужно, всё видно из клиентского
 * состояния сущности, поэтому дёшево по производительности.
 */
public class DamageParticlesFeature extends Feature {

    private final Map<Integer, Integer> lastHurtTime = new HashMap<>();

    public DamageParticlesFeature() {
        super("damage_particles", "Частицы урона",
                "Красивый всплеск частиц при попадании по сущности.", FeatureCategory.VISUALS);
    }

    @Override
    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        for (LivingEntity entity : mc.world.getEntitiesByClass(LivingEntity.class,
                mc.player.getBoundingBox().expand(32), e -> true)) {

            int prev = lastHurtTime.getOrDefault(entity.getId(), 0);
            int cur = entity.hurtTime;
            if (cur > prev) {
                spawn(mc, entity);
            }
            lastHurtTime.put(entity.getId(), cur);
        }
    }

    private void spawn(MinecraftClient mc, LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getBodyY(0.6);
        double z = entity.getZ();
        for (int i = 0; i < 6; i++) {
            double ox = (mc.world.random.nextDouble() - 0.5) * 0.6;
            double oy = (mc.world.random.nextDouble() - 0.2) * 0.6;
            double oz = (mc.world.random.nextDouble() - 0.5) * 0.6;
            mc.world.addParticleClient(ParticleTypes.CRIMSON_SPORE, x + ox, y + oy, z + oz, 0, 0.02, 0);
        }
    }
}
