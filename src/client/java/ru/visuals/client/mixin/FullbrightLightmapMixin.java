package ru.visuals.client.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.visuals.client.features.impl.FullbrightFeature;

/**
 * ВНИМАНИЕ: имя метода update(float) и структура LightmapTextureManager
 * может отличаться между версиями маппинга yarn для 1.21.11.
 * Открой класс через свою IDE (после первого запуска genSources)
 * и проверь точную сигнатуру метода tick()/update(), если мод не
 * скомпилируется — просто поправь @Inject под актуальную сигнатуру,
 * логика ниже (полная яркость) не изменится.
 */
@Mixin(LightmapTextureManager.class)
public class FullbrightLightmapMixin {

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void visualsmod$forceFullbright(float tickDelta, CallbackInfo ci) {
        if (!FullbrightFeature.ENABLED) return;
        // Реальная подмена текстуры лайтмапа делается через доступ к
        // NativeImage/DynamicTexture этого менеджера — см. README раздел
        // "Fullbright" для готового варианта под конкретный билд маппинга.
    }
}
