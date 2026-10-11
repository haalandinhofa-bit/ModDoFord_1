package dev.sodiun.elementalcombat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import org.lwjgl.glfw.GLFW;

public final class ElementalCombatClient implements ClientModInitializer {
    public enum Power {
        FIRE("Fire Blaster", 0xFFFF7433),
        ICE("Ice Shard", 0xFF65DFFF),
        EARTHQUAKE("Earthquake Blast", 0xFFB5A07A);

        public final String displayName;
        public final int color;

        Power(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }
    }

    private static int selectedPower = 0;

    public static Power getSelectedPower() {
        return Power.values()[selectedPower];
    }

    public static void cyclePower(double scroll) {
        int direction = scroll > 0 ? -1 : 1;
        selectedPower = (selectedPower + direction + Power.values().length) % Power.values().length;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("Blaster: " + getSelectedPower().displayName), true);
        }
    }

    @Override
    public void onInitializeClient() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!player.getStackInHand(hand).isOf(ElementalCombatMod.BLASTER_ITEM)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }
            if (world.isClient && MinecraftClient.getInstance().getNetworkHandler() != null) {
                switch (getSelectedPower()) {
                    case FIRE -> ClientPlayNetworking.send(ElementalCombatMod.FIRE_PACKET, PacketByteBufs.create());
                    case ICE -> ClientPlayNetworking.send(ElementalCombatMod.ICE_PACKET, PacketByteBufs.create());
                    case EARTHQUAKE -> ClientPlayNetworking.send(ElementalCombatMod.BLAST_PACKET, PacketByteBufs.create());
                }
            }
            return TypedActionResult.success(player.getStackInHand(hand), world.isClient);
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.player.getMainHandStack().isOf(ElementalCombatMod.BLASTER_ITEM)) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        Text.literal("Blaster: " + getSelectedPower().displayName),
                        8, 8, getSelectedPower().color);
            }
        });
    }
}
