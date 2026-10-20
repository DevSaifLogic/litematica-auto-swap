package com.vondx.autoswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import vondx.autoswap.AutoSwap;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AutoSwapConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("autoswap.json");

    public static final AutoSwapConfig SETTINGS = new AutoSwapConfig();

    public boolean immediateSwap;
    public int intervalMs = 100;
    public boolean showMissingBlockMessage = true;
    public boolean pauseWhileHoldingTool = true;
    public int triggerKeyCode = GLFW.GLFW_KEY_X;

    private AutoSwapConfig() {
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            AutoSwapConfig loaded = GSON.fromJson(reader, AutoSwapConfig.class);
            if (loaded == null) {
                throw new JsonParseException("Configuration file is empty");
            }
            SETTINGS.immediateSwap = loaded.immediateSwap;
            SETTINGS.intervalMs = clampInterval(loaded.intervalMs);
            SETTINGS.showMissingBlockMessage = loaded.showMissingBlockMessage;
            SETTINGS.pauseWhileHoldingTool = loaded.pauseWhileHoldingTool;
            SETTINGS.triggerKeyCode = loaded.triggerKeyCode > GLFW.GLFW_KEY_UNKNOWN
                    ? loaded.triggerKeyCode
                    : GLFW.GLFW_KEY_X;
        } catch (IOException | JsonParseException exception) {
            AutoSwap.LOGGER.error("Could not load AutoSwap configuration; using defaults", exception);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(SETTINGS, writer);
            }
        } catch (IOException exception) {
            AutoSwap.LOGGER.error("Could not save AutoSwap configuration", exception);
        }
    }

    public static int clampInterval(int intervalMs) {
        return Math.max(50, Math.min(5000, intervalMs));
    }
}
