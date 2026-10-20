package com.vondx.autoswap;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper.HitType;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class ServerSafeAutoSwapper {
    private static final double TRACE_DISTANCE = 10.0;

    @Nullable
    private static BlockPos lastTargetPos;
    @Nullable
    private static BlockPos lastMissingPos;
    @Nullable
    private static Item lastMissingItem;
    private static long lastSwapTime;

    private ServerSafeAutoSwapper() {
    }

    public static void tick(boolean enabled) {
        Minecraft client = Minecraft.getInstance();
        if (!enabled || client.gui.screen() != null || client.player == null
                || client.level == null || client.gameMode == null) {
            resetTarget();
            return;
        }

        if (AutoSwapConfig.SETTINGS.pauseWhileHoldingTool && isHoldingTool(client)) {
            resetTarget();
            return;
        }

        RayTraceWrapper trace = RayTraceUtils.getGenericTrace(
                client.level,
                EntityUtils.getCameraEntity(),
                TRACE_DISTANCE,
                true,
                Configs.InfoOverlays.INFO_OVERLAYS_TARGET_FLUIDS.getBooleanValue(),
                false
        );
        if (trace == null || trace.getHitType() != HitType.SCHEMATIC_BLOCK) {
            resetTarget();
            return;
        }

        BlockHitResult hitResult = trace.getBlockHitResult();
        if (hitResult == null) {
            resetTarget();
            return;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockPos targetPos = pos.immutable();
        boolean targetChanged = !targetPos.equals(lastTargetPos);
        lastTargetPos = targetPos;

        long now = System.currentTimeMillis();
        if (!targetChanged && now - lastSwapTime < AutoSwapConfig.SETTINGS.intervalMs) {
            return;
        }
        lastSwapTime = now;

        if (SchematicWorldHandler.getSchematicWorld() == null) {
            clearMissingBlock();
            return;
        }

        BlockState schematicState = SchematicWorldHandler.getSchematicWorld().getBlockState(pos);
        if (schematicState.isAir()) {
            clearMissingBlock();
            return;
        }

        Item targetItem = schematicState.getBlock().asItem();
        if (targetItem == null || targetItem == net.minecraft.world.item.Items.AIR) {
            clearMissingBlock();
            return;
        }

        Inventory inventory = client.player.getInventory();
        int matchingSlot = -1;
        for (int slot = 0; slot < 36; slot++) {
            if (inventory.getItem(slot).is(targetItem)) {
                matchingSlot = slot;
                break;
            }
        }

        if (matchingSlot == -1) {
            showMissingBlockMessage(client, targetPos, targetItem);
            return;
        }

        clearMissingBlock();
        int currentHotbarSlot = inventory.getSelectedSlot();
        if (matchingSlot < 9) {
            inventory.setSelectedSlot(matchingSlot);
            return;
        }

        client.gameMode.handleContainerInput(
                client.player.containerMenu.containerId,
                matchingSlot,
                currentHotbarSlot,
                ContainerInput.SWAP,
                client.player
        );
    }

    private static boolean isHoldingTool(Minecraft client) {
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();
        return isVanillaTool(mainHand) || isVanillaTool(offHand)
                || (DataManager.getInstance().hasToolItemComponents()
                && fi.dy.masa.litematica.util.EntityUtils.hasToolItem(client.player));
    }

    private static boolean isVanillaTool(ItemStack stack) {
        return stack.is(ItemTags.AXES)
                || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.SWORDS);
    }

    private static void showMissingBlockMessage(Minecraft client, BlockPos pos, Item item) {
        if (!AutoSwapConfig.SETTINGS.showMissingBlockMessage) {
            return;
        }
        if (!pos.equals(lastMissingPos) || item != lastMissingItem) {
            client.gui.hud.setOverlayMessage(
                    Component.translatable(
                            "message.autoswap.missing",
                            Component.translatable(item.getDescriptionId())
                    ).withStyle(ChatFormatting.RED),
                    false
            );
            lastMissingPos = pos;
            lastMissingItem = item;
        }
    }

    private static void clearMissingBlock() {
        lastMissingPos = null;
        lastMissingItem = null;
    }

    private static void resetTarget() {
        lastTargetPos = null;
        clearMissingBlock();
    }
}
