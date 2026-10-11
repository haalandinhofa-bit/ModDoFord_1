package dev.sodiun.experimentalmenu;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class ExperimentalFeatures {
    public static boolean espBox = false;
    public static boolean aimBotArc = false;
    public static boolean puloBlz = false;
    private static boolean wasAirborne = false;
    private ExperimentalFeatures() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) { wasAirborne = false; return; }
        if (!puloBlz) { wasAirborne = false; return; }
        boolean airborne = !client.player.isOnGround() && !client.player.isFallFlying() && !client.player.isClimbing() && !client.player.isTouchingWater();
        if (airborne && !wasAirborne) {
            // Client-side experimental boost; not a server-authoritative potion effect.
            var v = client.player.getVelocity();
            client.player.setVelocity(v.x * 1.12, v.y, v.z * 1.12);
        }
        wasAirborne = airborne;
    }

    public static void registerRendering() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!espBox || client.world == null || client.player == null || context.matrixStack() == null || context.consumers() == null) return;
            Vec3d camera = context.camera().getPos();
            var matrices = context.matrixStack();
            var vertices = context.consumers().getBuffer(RenderLayer.getLines());
            for (Entity entity : client.world.getEntities()) {
                if (entity == client.player || !entity.isAlive()) continue;
                Box box = entity.getBoundingBox().expand(0.04).offset(-camera.x, -camera.y, -camera.z);
                VertexRendering.drawBox(matrices, vertices, box, 1.0f, 0.25f, 0.25f, 0.9f);
            }
        });
    }
}
