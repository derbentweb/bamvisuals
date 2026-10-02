package ru.visuals.client.features.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import org.lwjgl.glfw.GLFW;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * Ручной свап по кнопке (НЕ автоматический реактивный автосвап в бою):
 * игрок настраивает в меню "предмет A" и "предмет Б" (например тотем/сфера),
 * жмёт бинд — мод ищет ближайший подходящий предмет из хотбара и берёт его
 * в руку. Это обычный quick-switch, ничего не происходит без нажатия кнопки.
 */
public class AutoSwapFeature extends Feature {

    // Настраиваются из GUI, храним id предметов как строки (namespace:path)
    public volatile String itemAId = "minecraft:totem_of_undying";
    public volatile String itemBId = "minecraft:ender_pearl"; // пример "сферы"/др. предмета

    public AutoSwapFeature() {
        super("autoswap", "Автосвап (ручной)",
                "По кнопке берёт в руку предмет A, повторное нажатие — предмет Б. Настрой предметы ниже.",
                FeatureCategory.COMBAT_QOL);
        this.keyBinding = new KeyBinding("key.visualsmod.autoswap",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.visualsmod");
    }

    @Override
    public boolean isTriggerType() {
        return true;
    }

    private boolean nextIsA = true;

    @Override
    public void onKeyPressed() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String targetId = nextIsA ? itemAId : itemBId;
        var registryEntry = net.minecraft.registry.Registries.ITEM
                .getOrEmpty(net.minecraft.util.Identifier.tryParse(targetId));
        if (registryEntry.isEmpty()) return;
        Item wanted = registryEntry.get();

        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(wanted)) {
                inv.setSelectedSlot(i);
                nextIsA = !nextIsA;
                return;
            }
        }
        mc.player.sendMessage(net.minecraft.text.Text.literal(
                "§cПредмет не найден в хотбаре: " + targetId), true);
    }
}
