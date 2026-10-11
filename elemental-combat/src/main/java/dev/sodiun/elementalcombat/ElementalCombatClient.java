package dev.sodiun.elementalcombat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ElementalCombatClient implements ClientModInitializer {
    private static final KeyBinding FIRE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.elemental_combat.fire", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.elemental_combat"));
    private static final KeyBinding ICE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.elemental_combat.ice", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.elemental_combat"));
    private static final KeyBinding BLAST = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.elemental_combat.blast", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.elemental_combat"));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) return;
            while (FIRE.wasPressed()) {
                ClientPlayNetworking.send(ElementalCombatMod.FIRE_PACKET, PacketByteBufs.create());
            }
            while (ICE.wasPressed()) {
                ClientPlayNetworking.send(ElementalCombatMod.ICE_PACKET, PacketByteBufs.create());
            }
            while (BLAST.wasPressed()) {
                ClientPlayNetworking.send(ElementalCombatMod.BLAST_PACKET, PacketByteBufs.create());
            }
        });
    }
}
