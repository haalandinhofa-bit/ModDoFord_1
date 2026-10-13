package dev.sodiun.cajadford;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

public class CajadoFordItem extends Item {
    private static final double RANGE = 24.0D;

    public CajadoFordItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(player.getLookAngle().scale(RANGE)).inflate(1.5D);
        net.minecraft.world.phys.EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox,
                entity -> entity instanceof LivingEntity && entity != player && entity.isPickable(),
                RANGE * RANGE);
        Entity hit = entityHit == null ? null : entityHit.getEntity();

        ServerLevel server = (ServerLevel) level;
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS, 0.8F, 1.4F);

        if (hit instanceof LivingEntity target) {
            Vec3 targetPoint = target.getBoundingBox().getCenter();
            Vec3 beam = targetPoint.subtract(eye);
            double distance = beam.length();
            Vec3 direction = beam.normalize();
            for (double d = 0; d < distance; d += 0.35D) {
                Vec3 point = eye.add(direction.scale(d));
                server.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        point.x, point.y, point.z, 1, 0.015, 0.015, 0.015, 0.0);
                server.sendParticles(ParticleTypes.FLAME,
                        point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            target.hurt(level.damageSources().playerAttack(player), 3.0F);
            target.setDeltaMovement(target.getDeltaMovement().x * 0.25D, 2.35D,
                    target.getDeltaMovement().z * 0.25D);
            target.hurtMarked = true;
            target.getPersistentData().putBoolean("cajado_ford_launched", true);
            target.getPersistentData().putInt("cajado_ford_air_ticks", 0);
            target.getPersistentData().putDouble("cajado_ford_peak_y", target.getY());
            target.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                    SoundSource.HOSTILE, 0.8F, 1.7F);
        } else {
            // A visible red-orange pulse still appears when no entity is hit.
            for (int i = 1; i <= 18; i++) {
                Vec3 point = eye.add(player.getLookAngle().scale(i * 0.6D));
                server.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z,
                        2, 0.02, 0.02, 0.02, 0.0);
            }
        }

        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.consume(stack);
    }
}
