package dev.sodiun.experimentalmenu;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ExperimentalFeatures {
    public static boolean espBox = false;
    public static boolean aimBotArc = false;
    public static boolean puloBlz = false;
    private static boolean wasAirborne = false;
    private static boolean addedJumpSpeed = false;

    private ExperimentalFeatures() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            wasAirborne = false;
            addedJumpSpeed = false;
            return;
        }

        if (!puloBlz) {
            removeJumpSpeed(client);
            wasAirborne = false;
            return;
        }

        boolean airborne = !client.player.isOnGround()
                && !client.player.isGliding()
                && !client.player.isClimbing()
                && !client.player.isTouchingWater();

        if (airborne && !wasAirborne && !client.player.hasStatusEffect(StatusEffects.SPEED)) {
            // Client-side effect for local testing. Multiplayer servers may reject/overwrite it.
            client.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 20, 3, true, false, false));
            addedJumpSpeed = true;
        }

        if (!airborne) {
            removeJumpSpeed(client);
        }
        wasAirborne = airborne;
    }

    private static void removeJumpSpeed(MinecraftClient client) {
        if (addedJumpSpeed && client.player != null) {
            client.player.removeStatusEffect(StatusEffects.SPEED);
        }
        addedJumpSpeed = false;
    }

    public static void registerRendering() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null
                    || context.matrixStack() == null || context.consumers() == null) return;

            Vec3d camera = context.camera().getPos();
            var matrices = context.matrixStack();
            var vertices = context.consumers().getBuffer(RenderLayer.getLines());

            if (espBox) {
                for (Entity entity : client.world.getEntities()) {
                    if (entity == client.player || !entity.isAlive()) continue;
                    Box box = entity.getBoundingBox().expand(0.04)
                            .offset(-camera.x, -camera.y, -camera.z);
                    VertexRendering.drawBox(matrices, vertices, box, 1.0f, 0.25f, 0.25f, 0.9f);
                }
            }

            // AIM BOT ARC is a visual-only bow/aim training guide. It never moves the player's aim.
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
