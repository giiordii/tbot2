package com.example.tbot;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class TBotMod implements ClientModInitializer {

    private static boolean enabled = false;
    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("tbot", "main"));

        // Tasto N (modificabile da Opzioni > Controlli)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tbot.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            mc.player.displayClientMessage(
                    Component.literal("TBot: " + (enabled ? "\u00a7aON" : "\u00a7cOFF")), true);
        }

        if (!enabled || mc.screen != null) return;

        if (mc.hitResult instanceof EntityHitResult hit) {
            Entity target = hit.getEntity();
            if (target instanceof LivingEntity living
                    && living.isAlive()
                    && mc.player.getAttackStrengthScale(0.5f) >= 1.0f) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }
}
