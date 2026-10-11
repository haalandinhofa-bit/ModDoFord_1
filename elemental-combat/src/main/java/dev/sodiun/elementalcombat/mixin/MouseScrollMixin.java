package dev.sodiun.elementalcombat.mixin;

import dev.sodiun.elementalcombat.ElementalCombatClient;
import dev.sodiun.elementalcombat.ElementalCombatMod;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseScrollMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void elementalCombat$selectPower(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (vertical != 0.0 && client.player != null && client.player.isSneaking()
                && (client.player.getMainHandStack().isOf(ElementalCombatMod.BLASTER_ITEM)
                || client.player.getOffHandStack().isOf(ElementalCombatMod.BLASTER_ITEM))) {
            ElementalCombatClient.cyclePower(vertical);
            ci.cancel();
        }
    }
}
