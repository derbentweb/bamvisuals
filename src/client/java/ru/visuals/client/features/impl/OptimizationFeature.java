package ru.visuals.client.features.impl;

import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * ВАЖНО: реальный, серьёзный прирост FPS дают именно Sodium/Lithium/Krypton
 * и т.д. — они переписывают рендер-пайплайн и логику тика на низком уровне,
 * это тысячи строк узкоспециализированного кода. Дублировать их в рамках
 * этого мода бессмысленно и как раз может конфликтовать с ними.
 *
 * Что реально стоит сделать здесь (и не конфликтует с Sodium/Lithium):
 *  - лёгкие клиентские настройки, которые сами по себе не трогают рендер-код
 *    этих модов: снижение дальности частиц, отключение лишних GUI-анимаций
 *    самого VisualsMod (у остальных наших фич), троттлинг наших же тиков.
 *  - каждая фича в этом моде и так написана с оглядкой на дешевизну
 *    (см. комментарии в JumpCircle/DamageParticles/GroundLabel) — то есть
 *    "оптимизация" в первую очередь означает "наши функции не должны
 *    давать -20/-50 FPS", а не замену Sodium.
 *
 * Этот тумблер сейчас управляет внутренними лимитами наших же фич
 * (радиусы, частота спавна частиц) — см. поля ниже.
 */
public class OptimizationFeature extends Feature {

    public static volatile boolean PERFORMANCE_MODE = false;

    public OptimizationFeature() {
        super("optimization", "Оптимизация визуалов",
                "Снижает нагрузку от остальных функций этого мода (радиусы/частота частиц). Не заменяет Sodium/Lithium — используй их вместе с этим модом.",
                FeatureCategory.PERFORMANCE);
    }

    @Override
    protected void onEnable() {
        PERFORMANCE_MODE = true;
    }

    @Override
    protected void onDisable() {
        PERFORMANCE_MODE = false;
    }
}
