package ru.visuals.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.visuals.client.config.ModConfig;
import ru.visuals.client.gui.MainMenuScreen;

/**
 * Подменяет ванильный TitleScreen на наше меню. Один лёгкий хук на setScreen,
 * рендер-код других модов (Sodium, ImmediatelyFast и т.д.) не затрагивается.
 *
 * Аварийный выход: в config/visualsmod.json у записи "menu" поставь
 * "extra": {"enabled": "false"}, и мод оставит ванильное меню.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen visualsmod$replaceTitleScreen(Screen screen) {
        if (screen == null || screen.getClass() != TitleScreen.class) return screen;
        if (MainMenuScreen.bypassOnce) {
            MainMenuScreen.bypassOnce = false;
            return screen;
        }
        if ("false".equals(ModConfig.entryFor("menu").extra.get("enabled"))) return screen;
        return new MainMenuScreen();
    }
}
