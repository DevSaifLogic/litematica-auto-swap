package com.vondx.autoswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class LitematicaAutoSwapClient implements ClientModInitializer {
    static KeyMapping swapKeyBinding;
    private static KeyMapping configKeyBinding;

    @Override
    public void onInitializeClient() {
        AutoSwapConfig.load();
        swapKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoswap.trigger",
                InputConstants.Type.KEYSYM,
                AutoSwapConfig.SETTINGS.triggerKeyCode,
                KeyMapping.Category.GAMEPLAY
        ));
        configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoswap.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKeyBinding.consumeClick() && client.gui.screen() == null) {
                client.gui.setScreen(new AutoSwapConfigScreen(null));
                return;
            }

            boolean autoSwap = AutoSwapConfig.SETTINGS.immediateSwap;
            ServerSafeAutoSwapper.tick(autoSwap || swapKeyBinding.isDown());
        });
    }

    static void updateSwapKeyBinding() {
        if (swapKeyBinding != null) {
            swapKeyBinding.setKey(
                    InputConstants.Type.KEYSYM.getOrCreate(AutoSwapConfig.SETTINGS.triggerKeyCode)
            );
            KeyMapping.resetMapping();
        }
    }
}
