package dev.sodiun.elementalcombat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ElementalCombatMod implements ModInitializer {
    public static final String MOD_ID = "elemental_combat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier FIRE_PACKET = new Identifier(MOD_ID, "cast_fire");
    public static final Identifier ICE_PACKET = new Identifier(MOD_ID, "cast_ice");
    public static final Identifier BLAST_PACKET = new Identifier(MOD_ID, "cast_blast");

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(FIRE_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> castFire(player)));
        ServerPlayNetworking.registerGlobalReceiver(ICE_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> castIce(player)));
        ServerPlayNetworking.registerGlobalReceiver(BLAST_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> castBlast(player)));
        LOGGER.info("Elemental Combat inicializado: fogo, gelo e explosão.");
    }

    private static boolean canCast(ServerPlayerEntity player, String spell, long cooldownTicks) {
        Map<String, Long> playerCooldowns = COOLDOWNS.computeIfAbsent(player.getUuid(), id -> new HashMap<>());
        long now = player.getWorld().getTime();
        long readyAt = playerCooldowns.getOrDefault(spell, 0L);
        if (now < readyAt) return false;
        playerCooldowns.put(spell, now + cooldownTicks);
        return true;
    }

    private static void castFire(ServerPlayerEntity player) {
        if (!player.isAlive() || !canCast(player, "fire", 20)) return;
        Vec3d look = player.getRotationVec(1.0F);
        SmallFireballEntity fireball = new SmallFireballEntity(
                player.getWorld(), player, look.x, look.y, look.z);
        fireball.setPosition(player.getX(), player.getEyeY() - 0.15, player.getZ());
        player.getWorld().spawnEntity(fireball);
    }

    private static Vec3d getAimPoint(ServerPlayerEntity player, double range) {
        HitResult hit = player.raycast(range, 1.0F, false);
        return hit.getType() == HitResult.Type.MISS
                ? player.getEyePos().add(player.getRotationVec(1.0F).multiply(range))
                : hit.getPos();
    }

    private static void castIce(ServerPlayerEntity player) {
        if (!player.isAlive() || !canCast(player, "ice", 60)) return;
        World world = player.getWorld();
        Vec3d point = getAimPoint(player, 14.0);
        Box area = Box.of(point, 4.5, 4.5, 4.5);

        for (var entity : world.getOtherEntities(player, area,
                target -> target instanceof LivingEntity && target.isAlive())) {
            if (entity instanceof LivingEntity target && target.squaredDistanceTo(point) <= 5.0 * 5.0) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1), player);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0), player);
            }
        }

        world.spawnParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z,
                35, 0.9, 0.7, 0.9, 0.025);
        world.spawnParticles(ParticleTypes.CLOUD, point.x, point.y, point.z,
                12, 0.5, 0.4, 0.5, 0.01);
    }

    private static void castBlast(ServerPlayerEntity player) {
        if (!player.isAlive() || !canCast(player, "blast", 100)) return;
        World world = player.getWorld();
        Vec3d point = getAimPoint(player, 12.0);
        world.createExplosion(player, point.x, point.y, point.z,
                2.5F, false, World.ExplosionSourceType.NONE);
        world.spawnParticles(ParticleTypes.POOF, point.x, point.y, point.z,
                30, 0.8, 0.8, 0.8, 0.12);
    }
}
