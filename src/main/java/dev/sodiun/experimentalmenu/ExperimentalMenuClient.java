package dev.sodiun.experimentalmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ExperimentalMenuClient implements ClientModInitializer {
    private boolean shiftWasDown;
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            long handle = client.getWindow().getHandle();
            boolean shift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (shift && !shiftWasDown && client.currentScreen == null) client.setScreen(new ExperimentalMenuScreen());
            shiftWasDown = shift;
            ExperimentalFeatures.tick(client);
        });
        ExperimentalFeatures.registerRendering();
    }
}
