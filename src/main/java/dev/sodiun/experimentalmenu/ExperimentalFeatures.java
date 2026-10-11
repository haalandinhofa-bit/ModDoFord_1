package dev.sodiun.experimentalmenu;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ExperimentalFeatures {
    public static boolean espBox = false;
    public static boolean aimBotArc = false;
    public static boolean aimAtNearestPlayer = false;
    public static boolean puloBlz = false;
    public static boolean killAura = false;
    public static boolean doubleJump = false;
    public static boolean infiniteLife = false;
    public static boolean fly = false;

    private static boolean jumpWasDown = false;
    private static boolean doubleJumpUsed = false;
    private static boolean flyStateCaptured = false;
    private static boolean previousAllowFlying = false;
    private static boolean previousFlying = false;

    private static final RenderLayer ESP_THROUGH_WALLS = RenderLayer.of(
            "experimental_menu_esp_through_walls",
            VertexFormats.LINES,
            VertexFormat.DrawMode.LINES,
            1536,
            false,
            false,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.LINES_PROGRAM)
                    .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .build(false)
    );

    private ExperimentalFeatures() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            jumpWasDown = false;
            doubleJumpUsed = false;
            flyStateCaptured = false;
            return;
        }

        var player = client.player;

        if (aimAtNearestPlayer && isCombatItem(player.getMainHandStack())) {
            PlayerEntity target = findNearestPlayer(client, 24.0);
            if (target != null) {
                Vec3d delta = target.getEyePos().subtract(player.getEyePos());
                double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                float yaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
                float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, horizontal));
                player.setYaw(yaw);
                player.setPitch(pitch);
            }
        }

        if (killAura && client.interactionManager != null
                && player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            PlayerEntity target = findNearestPlayer(client, 3.4);
            if (target != null && player.canSee(target)) {
                client.interactionManager.attackEntity(player, target);
                player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            }
        }

        boolean jumpDown = client.options.jumpKey.isPressed();
        if (player.isOnGround()) {
            doubleJumpUsed = false;
        } else if (doubleJump && jumpDown && !jumpWasDown && !doubleJumpUsed
                && !player.isTouchingWater() && !player.isClimbing()) {
            Vec3d velocity = player.getVelocity();
            player.setVelocity(velocity.x, 0.42, velocity.z);
            player.fallDistance = 0.0f;
            doubleJumpUsed = true;
        }
        jumpWasDown = jumpDown;

        if (fly) {
            if (!flyStateCaptured) {
                previousAllowFlying = player.getAbilities().allowFlying;
                previousFlying = player.getAbilities().flying;
                flyStateCaptured = true;
            }
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
            player.getAbilities().setFlySpeed(0.05f);
        } else if (flyStateCaptured) {
            player.getAbilities().allowFlying = previousAllowFlying;
            player.getAbilities().flying = previousFlying && previousAllowFlying;
            flyStateCaptured = false;
            player.sendAbilitiesUpdate();
        }
    }

    private static PlayerEntity findNearestPlayer(MinecraftClient client, double range) {
        PlayerEntity nearest = null;
        double closestDistanceSquared = range * range;
        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (candidate == client.player || !candidate.isAlive() || candidate.isSpectator()) continue;
            double distanceSquared = client.player.squaredDistanceTo(candidate);
            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static boolean isCombatItem(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem;
    }

    public static void registerRendering() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null
                    || context.matrixStack() == null || context.consumers() == null) return;

            Vec3d camera = context.camera().getPos();
            var matrices = context.matrixStack();
            var vertices = context.consumers().getBuffer(ESP_THROUGH_WALLS);

            if (espBox) {
                for (Entity entity : client.world.getEntities()) {
                    if (entity == client.player || !entity.isAlive()) continue;
                    Box box = entity.getBoundingBox().expand(0.04)
                            .offset(-camera.x, -camera.y, -camera.z);
                    VertexRendering.drawBox(matrices, vertices, box, 1.0f, 0.25f, 0.25f, 0.9f);
                }
            }

            // Aim Bot Arc remains a visual-only trajectory guide.
            if (aimBotArc) {
                Vec3d point = client.player.getEyePos();
                Vec3d velocity = client.player.getRotationVec(1.0f).normalize().multiply(1.35);
                for (int i = 0; i < 28; i++) {
                    point = point.add(velocity);
                    velocity = new Vec3d(velocity.x * 0.99, velocity.y * 0.99 - 0.035,
                            velocity.z * 0.99);
                    Box marker = new Box(point.x - 0.035, point.y - 0.035, point.z - 0.035,
                            point.x + 0.035, point.y + 0.035, point.z + 0.035)
                            .offset(-camera.x, -camera.y, -camera.z);
                    VertexRendering.drawBox(matrices, vertices, marker, 0.25f, 0.95f, 0.75f, 0.95f);
                }
            }
        });
    }
}
