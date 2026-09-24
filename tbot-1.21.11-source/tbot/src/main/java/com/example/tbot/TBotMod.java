package com.example.tbot;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        // Registrazione del tasto di attivazione
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tbot.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("tbot", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft mc) {
        // Controlli di sicurezza sullo stato del gioco
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }

        // Gestione pressione tasto di attivazione
        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            
            Component statusText = enabled 
                    ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                    : Component.literal("OFF").withStyle(ChatFormatting.RED);

            mc.player.displayClientMessage(
                    Component.literal("TBot: ").append(statusText), true);
        }

        // Se disattivato o c'è un menu aperto, non fare nulla
        if (!enabled || mc.screen != null) {
            return;
        }

        // Controllo del target sotto il mirino
        if (mc.hitResult instanceof EntityHitResult hit) {
            Entity target = hit.getEntity();

            if (target instanceof LivingEntity living 
                    && living.isAlive() 
                    && mc.player.getAttackStrengthScale(0.0f) >= 1.0f) {
                
                // Esegue l'attacco
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
                
                // Resetta il timer dell'attacco sul client
                mc.player.resetAttackStrengthTicker();
            }
        }
    }
}
