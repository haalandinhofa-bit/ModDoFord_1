package dev.sodiun.elementalcombat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ElementalCombatMod implements ModInitializer {
    public static final String MOD_ID = "elemental_combat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier FIRE_PACKET = new Identifier(MOD_ID, "cast_fire");
    public static final Identifier ICE_PACKET = new Identifier(MOD_ID, "cast_ice");
    public static final Identifier BLAST_PACKET = new Identifier(MOD_ID, "cast_blast");
    public static final Item BLASTER_ITEM = Registry.register(
            Registries.ITEM, new Identifier(MOD_ID, "blaster"),
            new Item(new Item.Settings().maxCount(1)));

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();
    private static final List<TemporaryIce> TEMPORARY_ICE = new ArrayList<>();

    private record TemporaryIce(ServerWorld world, BlockPos pos, BlockState original, long expiresAt) {}

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(FIRE_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> castFire(player)));
        ServerPlayNetworking.registerGlobalReceiver(ICE_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> castIce(player)));
        ServerPlayNetworking.registerGlobalReceiver(BLAST_PACKET,
                (server, player, handler, buf, responseSender) -> server.execute(() -> castBlast(player)));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<TemporaryIce> iterator = TEMPORARY_ICE.iterator();
            while (iterator.hasNext()) {
                TemporaryIce ice = iterator.next();
                if (ice.world().getTime() >= ice.expiresAt()) {
                    if (ice.world().getBlockState(ice.pos()).isOf(Blocks.ICE)) {
                        ice.world().setBlockState(ice.pos(), ice.original(), 3);
                    }
                    iterator.remove();
                }
            }
        });
        LOGGER.info("Elemental Combat inicializado: Blaster, Fire Blaster, Ice Shard e Earthquake Blast.");
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

    private static void addTemporaryIce(ServerWorld world, BlockPos pos) {
        BlockState original = world.getBlockState(pos);
        if (!original.isAir() && !original.isReplaceable()) return;
        BlockPos immutablePos = pos.toImmutable();
        world.setBlockState(immutablePos, Blocks.ICE.getDefaultState(), 3);
        TEMPORARY_ICE.add(new TemporaryIce(world, immutablePos, original, world.getTime() + 60));
    }

    private static void freezeAround(ServerPlayerEntity player, LivingEntity target) {
        ServerWorld world = player.getServerWorld();
        BlockPos base = target.getBlockPos();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos around = base.offset(direction);
            addTemporaryIce(world, around);
            addTemporaryIce(world, around.up());
        }
        world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.55),
                target.getZ(), 32, 0.65, 0.8, 0.65, 0.025);
    }

    private static void castIce(ServerPlayerEntity player) {
        if (!player.isAlive() || !canCast(player, "ice", 60)) return;
        ServerWorld world = player.getServerWorld();
        Vec3d point = getAimPoint(player, 14.0);
        Box area = Box.of(point, 5.0, 5.0, 5.0);

        for (var entity : world.getOtherEntities(player, area,
                target -> target instanceof LivingEntity && target.isAlive())) {
            if (entity instanceof LivingEntity target && target.squaredDistanceTo(point) <= 5.0 * 5.0) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1), player);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0), player);
                freezeAround(player, target);
            }
        }

        world.spawnParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z,
                35, 0.9, 0.7, 0.9, 0.025);
        world.spawnParticles(ParticleTypes.CLOUD, point.x, point.y, point.z,
                12, 0.5, 0.4, 0.5, 0.01);
    }

    private static void castBlast(ServerPlayerEntity player) {
        if (!player.isAlive() || !canCast(player, "blast", 100)) return;
        ServerWorld world = player.getServerWorld();
        Vec3d point = getAimPoint(player, 12.0);
        // TNT-style destruction is intentionally limited to a small radius.
        world.createExplosion(player, point.x, point.y, point.z,
                2.0F, false, World.ExplosionSourceType.TNT);
        world.spawnParticles(ParticleTypes.POOF, point.x, point.y, point.z,
                30, 0.8, 0.8, 0.8, 0.12);
        world.spawnParticles(ParticleTypes.CLOUD, point.x, point.y, point.z,
                18, 1.1, 0.3, 1.1, 0.08);
    }
}
