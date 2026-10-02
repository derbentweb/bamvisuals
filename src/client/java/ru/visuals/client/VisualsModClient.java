package ru.visuals.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.visuals.client.features.FeatureManager;
import ru.visuals.client.features.impl.WinterFeature;
import ru.visuals.client.music.MusicPlayer;
import ru.visuals.client.gui.VisualsScreen;

public class VisualsModClient implements ClientModInitializer {

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        FeatureManager.registerAll();
        MusicPlayer.init();

        HudRenderCallback.EVENT.register((context, tickCounter) -> WinterFeature.renderHud(context));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> MusicPlayer.shutdown());

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualsmod.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.visualsmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MusicPlayer.tick();
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new VisualsScreen());
                }
            }
        });
    }
}
