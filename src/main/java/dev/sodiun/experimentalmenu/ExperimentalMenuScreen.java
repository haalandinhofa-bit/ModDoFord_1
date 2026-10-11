package dev.sodiun.experimentalmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ExperimentalMenuScreen extends Screen {
    public ExperimentalMenuScreen() { super(Text.literal("Experimental Menu")); }
    @Override protected void init() {
        int center = this.width / 2;
        int y = this.height / 2 - 42;
        addToggle(center, y, "ESP BOX", 0);
        addToggle(center, y + 30, "AIM BOT ARC", 1);
        addToggle(center, y + 60, "PULO BLZ", 2);
    }
    private void addToggle(int center, int y, String label, int feature) {
        ButtonWidget button = ButtonWidget.builder(toggleText(label, feature), b -> {
            switch (feature) {
                case 0 -> ExperimentalFeatures.espBox = !ExperimentalFeatures.espBox;
                case 1 -> ExperimentalFeatures.aimBotArc = !ExperimentalFeatures.aimBotArc;
                case 2 -> ExperimentalFeatures.puloBlz = !ExperimentalFeatures.puloBlz;
            }
            b.setMessage(toggleText(label, feature));
        }).dimensions(center - 100, y, 200, 22).build();
        addDrawableChild(button);
    }
    private Text toggleText(String label, int feature) {
        boolean enabled = switch (feature) { case 0 -> ExperimentalFeatures.espBox; case 1 -> ExperimentalFeatures.aimBotArc; default -> ExperimentalFeatures.puloBlz; };
        return Text.literal(label + "  [" + (enabled ? "ON" : "OFF") + "]");
    }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0xB8101420);
        int center = width / 2;
        context.fill(center - 120, height / 2 - 76, center + 120, height / 2 + 98, 0xEE171D2B);
        context.drawCenteredTextWithShadow(textRenderer, "EXPERIMENTAL MENU", center, height / 2 - 67, 0xFF76E6D2);
        context.drawCenteredTextWithShadow(textRenderer, "SHIFT abre • ESC fecha", center, height / 2 + 78, 0xFFB7C2D4);
        super.render(context, mouseX, mouseY, delta);
    }
}
