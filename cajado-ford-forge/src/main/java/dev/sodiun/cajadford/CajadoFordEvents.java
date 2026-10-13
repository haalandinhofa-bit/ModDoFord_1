package dev.sodiun.cajadford;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CajadoFordMod.MOD_ID)
public final class CajadoFordEvents {
    private CajadoFordEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !entity.getPersistentData().getBoolean("cajado_ford_launched")) {
            return;
        }

        int airTicks = entity.getPersistentData().getInt("cajado_ford_air_ticks");
        if (!entity.onGround()) {
            entity.getPersistentData().putInt("cajado_ford_air_ticks", airTicks + 1);
            entity.getPersistentData().putDouble("cajado_ford_peak_y",
                    Math.max(entity.getPersistentData().getDouble("cajado_ford_peak_y"), entity.getY()));
            return;
        }

        if (airTicks > 2) {
            makeImpact((ServerLevel) entity.level(), entity);
        }
        entity.getPersistentData().remove("cajado_ford_launched");
        entity.getPersistentData().remove("cajado_ford_air_ticks");
        entity.getPersistentData().remove("cajado_ford_peak_y");
    }

    private static void makeImpact(ServerLevel level, LivingEntity entity) {
        BlockPos center = entity.blockPosition();
        Random random = new Random(entity.getUUID().getLeastSignificantBits() ^ level.getGameTime());

        level.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY() + 0.1,
                entity.getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.2,
                entity.getZ(), 28, 0.9, 0.15, 0.9, 0.12);
        level.playSound(null, center, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.2F, 0.8F);

        // Break only a few ordinary, non-block-entity blocks; never dig below the impact.
        int attempts = 0;
        for (int i = 0; i < 12 && attempts < 4; i++) {
            int dx = random.nextInt(5) - 2;
            int dz = random.nextInt(5) - 2;
            BlockPos pos = center.offset(dx, 0, dz);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getDestroySpeed(level, pos) < 0
                    || !state.getFluidState().isEmpty() || level.getBlockEntity(pos) != null) {
                continue;
            }
            if (level.destroyBlock(pos, true, entity)) {
                attempts++;
                level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 5, 0.25, 0.25, 0.25, 0.08);
            }
        }

        // Small knockback to nearby living entities makes the landing feel powerful.
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class,
                entity.getBoundingBox().inflate(3.0D), e -> e != entity)) {
            double dx = nearby.getX() - entity.getX();
            double dz = nearby.getZ() - entity.getZ();
            double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            nearby.push(dx / length * 0.8D, 0.45D, dz / length * 0.8D);
            nearby.hurtMarked = true;
        }
    }
}
