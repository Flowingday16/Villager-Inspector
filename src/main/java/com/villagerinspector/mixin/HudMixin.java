package com.villagerinspector.mixin;

import com.villagerinspector.VillagerInspectorClient;
import com.villagerinspector.client.render.VillagerHudOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class HudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void villagerinspector$onRender(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        try {
            if (VillagerInspectorClient.isHudEnabled()) {
                VillagerHudOverlay.render(graphics, deltaTracker.getGameTimeDeltaTicks());
            }
        } catch (Throwable ignored) {
        }
    }
}
