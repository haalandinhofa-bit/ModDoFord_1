package dev.sodiun.experimentalmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

public final class ExperimentalMenuClient implements ClientModInitializer {
    private boolean rightShiftWasDown;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            long handle = client.getWindow().getHandle();
            boolean rightShift = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (rightShift && !rightShiftWasDown && client.currentScreen == null) {
                client.setScreen(new ExperimentalMenuScreen());
            }
            rightShiftWasDown = rightShift;
            ExperimentalFeatures.tick(client);
        });
        ExperimentalFeatures.registerRendering();
    }
}
