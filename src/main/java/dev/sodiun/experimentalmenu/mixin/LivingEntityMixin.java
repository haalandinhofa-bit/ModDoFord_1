package dev.sodiun.experimentalmenu.mixin;

import dev.sodiun.experimentalmenu.ExperimentalFeatures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void experimentalmenu$preventLocalPlayerDamage(
            ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (ExperimentalFeatures.infiniteLife
                && (Object) this == MinecraftClient.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
