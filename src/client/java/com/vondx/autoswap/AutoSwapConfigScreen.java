package com.vondx.autoswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class AutoSwapConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private final Screen parent;
    private boolean waitingForKey;
    private Button modeButton;
    private Button intervalButton;
    private Button missingMessageButton;
    private Button toolGuardButton;
    private Button keyButton;

    public AutoSwapConfigScreen(Screen parent) {
        super(Component.translatable("screen.autoswap.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = (width - BUTTON_WIDTH) / 2;
        int y = height / 4 + 8;

        modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> {
            AutoSwapConfig.SETTINGS.immediateSwap = !AutoSwapConfig.SETTINGS.immediateSwap;
            AutoSwapConfig.save();
            modeButton.setMessage(modeLabel());
        }).bounds(left, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.literal("-"), button -> changeInterval(-50))
                .bounds(left, y + 26, 40, BUTTON_HEIGHT).build());
        intervalButton = addRenderableWidget(Button.builder(intervalLabel(), button -> {})
                .bounds(left + 44, y + 26, BUTTON_WIDTH - 88, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> changeInterval(50))
                .bounds(left + BUTTON_WIDTH - 40, y + 26, 40, BUTTON_HEIGHT).build());

        missingMessageButton = addRenderableWidget(Button.builder(missingMessageLabel(), button -> {
            AutoSwapConfig.SETTINGS.showMissingBlockMessage =
                    !AutoSwapConfig.SETTINGS.showMissingBlockMessage;
            AutoSwapConfig.save();
            missingMessageButton.setMessage(missingMessageLabel());
        }).bounds(left, y + 52, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        toolGuardButton = addRenderableWidget(Button.builder(toolGuardLabel(), button -> {
            AutoSwapConfig.SETTINGS.pauseWhileHoldingTool =
                    !AutoSwapConfig.SETTINGS.pauseWhileHoldingTool;
            AutoSwapConfig.save();
            toolGuardButton.setMessage(toolGuardLabel());
        }).bounds(left, y + 78, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        keyButton = addRenderableWidget(Button.builder(keyLabel(), button -> {
            waitingForKey = true;
            keyButton.setMessage(Component.translatable("screen.autoswap.config.key.waiting"));
        }).bounds(left, y + 104, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left, y + 134, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, height / 4 - 12, 0xFFFFFFFF);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!waitingForKey) {
            return super.keyPressed(event);
        }

        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            waitingForKey = false;
            keyButton.setMessage(keyLabel());
            return true;
        }

        if (event.key() > GLFW.GLFW_KEY_UNKNOWN) {
            AutoSwapConfig.SETTINGS.triggerKeyCode = event.key();
            AutoSwapConfig.save();
            LitematicaAutoSwapClient.updateSwapKeyBinding();
            waitingForKey = false;
            keyButton.setMessage(keyLabel());
        }
        return true;
    }

    @Override
    public void onClose() {
        AutoSwapConfig.save();
        minecraft.gui.setScreen(parent);
    }

    private void changeInterval(int delta) {
        AutoSwapConfig.SETTINGS.intervalMs = AutoSwapConfig.clampInterval(
                AutoSwapConfig.SETTINGS.intervalMs + delta
        );
        intervalButton.setMessage(intervalLabel());
        AutoSwapConfig.save();
    }

    private static Component modeLabel() {
        return Component.translatable(
                "screen.autoswap.config.immediate",
                Component.translatable(AutoSwapConfig.SETTINGS.immediateSwap
                        ? "options.on" : "options.off")
        );
    }

    private static Component intervalLabel() {
        return Component.translatable(
                "screen.autoswap.config.interval",
                AutoSwapConfig.SETTINGS.intervalMs
        );
    }

    private static Component missingMessageLabel() {
        return Component.translatable(
                "screen.autoswap.config.missing_message",
                Component.translatable(AutoSwapConfig.SETTINGS.showMissingBlockMessage
                        ? "options.on" : "options.off")
        );
    }

    private static Component toolGuardLabel() {
        return Component.translatable(
                "screen.autoswap.config.tool_guard",
                Component.translatable(AutoSwapConfig.SETTINGS.pauseWhileHoldingTool
                        ? "options.on" : "options.off")
        );
    }

    private static Component keyLabel() {
        return Component.translatable(
                "screen.autoswap.config.key",
                InputConstants.Type.KEYSYM.getOrCreate(AutoSwapConfig.SETTINGS.triggerKeyCode)
                        .getDisplayName()
        );
    }
}
