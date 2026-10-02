package ru.visuals.client.features.impl;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * Рисует мелкую подпись с названием предмета над каждым ItemEntity в мире
 * (то, что ты называл "показывает что лежит на земле"). Рендерим только
 * предметы в разумном радиусе — не тянем весь мир, чтобы не сажать FPS.
 */
public class GroundLabelFeature extends Feature {

    private static final double RADIUS = 24.0;

    public GroundLabelFeature() {
        super("ground_label", "Подписи предметов на земле",
                "Показывает мелким шрифтом название предмета над ним.", FeatureCategory.VISUALS);
    }

    @Override
    protected void onEnable() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::render);
    }
    // Примечание: полноценная отписка события при onDisable() требует
    // хранить ссылку на регистрируемый listener отдельно — Fabric API
    // на данный момент не даёт unregister из коробки для этого события,
    // поэтому внутри render() дополнительно проверяем isEnabled().

    private void render(WorldRenderContext context) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d camPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        for (ItemEntity entity : mc.world.getEntitiesByClass(ItemEntity.class,
                mc.player.getBoundingBox().expand(RADIUS), e -> true)) {

            double dx = entity.getX() - camPos.x;
            double dy = entity.getY() - camPos.y + 0.4;
            double dz = entity.getZ() - camPos.z;

            matrices.push();
            matrices.translate(dx, dy, dz);
            matrices.multiply(mc.gameRenderer.getCamera().getRotation());
            matrices.scale(-0.025f, -0.025f, 0.025f);

            Text label = entity.getStack().getName();
            var textRenderer = mc.textRenderer;
            float x = -textRenderer.getWidth(label) / 2f;
            textRenderer.draw(label, x, 0, 0xFFFFFF, false,
                    matrices.peek().getPositionMatrix(), consumers,
                    net.minecraft.client.font.TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);

            matrices.pop();
        }
    }
}
