package com.villagerinspector;

import com.mojang.blaze3d.platform.InputConstants;
import com.villagerinspector.client.RaycastHelper;
import com.villagerinspector.client.VillagerClientCache;
import com.villagerinspector.client.gui.VillagerConfigScreen;
import com.villagerinspector.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.Villager;

public class VillagerInspectorClient implements ClientModInitializer {
    public static KeyMapping toggleHudKey;
    public static KeyMapping openConfigKey;
    private static boolean isHudEnabled = true;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.villagerinspector.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            KeyMapping.Category.MISC
        ));

        // Default to UNBOUND (NONE) to avoid shader/mod key conflicts
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.villagerinspector.config",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MISC
        ));

        // Client Tick: Update targeted villager via raycast up to 10 blocks & check keybinds
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                while (toggleHudKey.consumeClick()) {
                    isHudEnabled = !isHudEnabled;
                    if (client.player != null) {
                        String stateText = isHudEnabled ?
                            "§a" + Component.translatable("hud.villagerinspector.toggle.on").getString() :
                            "§c" + Component.translatable("hud.villagerinspector.toggle.off").getString();
                        client.player.displayClientMessage(
                            Component.translatable("hud.villagerinspector.toggle.msg", stateText),
                            true
                        );
                    }
                }

                while (openConfigKey.consumeClick()) {
                    client.setScreen(new VillagerConfigScreen(client.screen));
                }

                if (!isHudEnabled || client.level == null || client.player == null) {
                    return;
                }

                Villager villager = RaycastHelper.getTargetedVillager(client, 10.0);
                if (villager != null) {
                    VillagerClientCache.setTargetedVillager(client, villager);
                }
            } catch (Throwable ignored) {
            }
        });
    }

    public static boolean isHudEnabled() {
        return isHudEnabled;
    }
}
