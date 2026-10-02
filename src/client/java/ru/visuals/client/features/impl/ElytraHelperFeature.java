package ru.visuals.client.features.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.visuals.client.config.FeatureCategory;
import ru.visuals.client.features.Feature;

/**
 * По нажатию бинда ищет элитру в инвентаре (не только хотбар) и мгновенно
 * меняет её местами с текущим нагрудником через клик по слоту сервера —
 * это обычное действие "поменять слот", доступное и без мода, мы просто
 * делаем его одной кнопкой.
 */
public class ElytraHelperFeature extends Feature {

    public ElytraHelperFeature() {
        super("elytra_helper", "Elytra Helper",
                "Мгновенно надевает элитру из инвентаря по нажатию кнопки.",
                FeatureCategory.COMBAT_QOL);
        this.keyBinding = new KeyBinding("key.visualsmod.elytra_helper",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.visualsmod");
    }

    @Override
    public boolean isTriggerType() {
        return true;
    }

    @Override
    public void onKeyPressed() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        int chestSlot = 6; // индекс слота брони "грудь" в PlayerScreenHandler (armor slots 5..8)
        var inventory = mc.player.getInventory();

        int elytraIndex = -1;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isOf(Items.ELYTRA)) {
                elytraIndex = i;
                break;
            }
        }
        if (elytraIndex == -1) {
            mc.player.sendMessage(Text.literal("§cЭлитра не найдена в инвентаре"), true);
            return;
        }

        // Переводим индекс инвентаря в id слота экрана игрока
        int screenSlotId = toScreenSlotId(elytraIndex);

        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                screenSlotId, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                chestSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                screenSlotId, 0, SlotActionType.PICKUP, mc.player);
    }

    private int toScreenSlotId(int inventoryIndex) {
        // 0..8 хотбар -> слоты 36..44; 9..35 основной инвентарь -> слоты 9..35
        if (inventoryIndex < 9) return inventoryIndex + 36;
        return inventoryIndex;
    }
}
