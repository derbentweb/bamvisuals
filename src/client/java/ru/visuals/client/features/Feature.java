package ru.visuals.client.features;

import net.minecraft.client.option.KeyBinding;
import ru.visuals.client.config.FeatureCategory;

/**
 * Базовый класс для любой функции мода.
 * Чтобы добавить новую функцию:
 *  1. Создай класс в features.impl, унаследуй Feature.
 *  2. Заполни id/ruName/ruDescription/category в конструкторе.
 *  3. Если функции нужен свой бинд (например "нажал — сработало один раз",
 *     как автосвап или элитра-хелпер) — верни true в hasOwnKeybind() и
 *     переопредели onKeyPressed().
 *  4. Если функция — обычный тумблер (вкл/выкл, типа fullbright) — не
 *     переопределяй onKeyPressed(), в GUI у неё будет чекбокс "включено".
 *  5. Зарегистрируй фичу в FeatureManager.registerAll().
 */
public abstract class Feature {

    private final String id;
    private final String ruName;
    private final String ruDescription;
    private final FeatureCategory category;

    private boolean enabled = false;
    protected KeyBinding keyBinding;

    protected Feature(String id, String ruName, String ruDescription, FeatureCategory category) {
        this.id = id;
        this.ruName = ruName;
        this.ruDescription = ruDescription;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getRuName() {
        return ruName;
    }

    public String getRuDescription() {
        return ruDescription;
    }

    public FeatureCategory getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /** true если функция активируется однократным нажатием (свап предмета и т.п.),
     *  а не является простым тумблером. */
    public boolean isTriggerType() {
        return false;
    }

    public KeyBinding getKeyBinding() {
        return keyBinding;
    }

    // === хуки для наследников ===
    protected void onEnable() {}
    protected void onDisable() {}
    public void onKeyPressed() {}
    public void tick() {}
}
