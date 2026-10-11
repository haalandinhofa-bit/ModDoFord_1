package dev.sodiun.experimentalmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ExperimentalMenuScreen extends Screen {
    public ExperimentalMenuScreen() { super(Text.literal("Experimental Menu")); }

    @Override
    protected void init() {
        int center = this.width / 2;
        int y = this.height / 2 - 92;
        addToggle(center, y, "ESP BOX", 0);
        addToggle(center, y + 24, "AIM BOT ARC", 1);
        addToggle(center, y + 48, "AIM AT NEAREST PLAYER", 2);
        addToggle(center, y + 72, "ANTI-KNOCKBACK", 3);
        addToggle(center, y + 96, "KILL AURA", 4);
        addToggle(center, y + 120, "DOUBLE JUMP", 5);
        addToggle(center, y + 144, "INFINITE LIFE", 6);
        addToggle(center, y + 168, "FLY", 7);
    }

    private void addToggle(int center, int y, String label, int feature) {
        ButtonWidget button = ButtonWidget.builder(toggleText(label, feature), b -> {
            switch (feature) {
                case 0 -> ExperimentalFeatures.espBox = !ExperimentalFeatures.espBox;
                case 1 -> ExperimentalFeatures.aimBotArc = !ExperimentalFeatures.aimBotArc;
                case 2 -> ExperimentalFeatures.aimAtNearestPlayer = !ExperimentalFeatures.aimAtNearestPlayer;
                case 3 -> ExperimentalFeatures.puloBlz = !ExperimentalFeatures.puloBlz;
                case 4 -> ExperimentalFeatures.killAura = !ExperimentalFeatures.killAura;
                case 5 -> ExperimentalFeatures.doubleJump = !ExperimentalFeatures.doubleJump;
                case 6 -> ExperimentalFeatures.infiniteLife = !ExperimentalFeatures.infiniteLife;
                case 7 -> ExperimentalFeatures.fly = !ExperimentalFeatures.fly;
            }
            b.setMessage(toggleText(label, feature));
        }).dimensions(center - 112, y, 224, 20).build();
        addDrawableChild(button);
    }

    private Text toggleText(String label, int feature) {
        boolean enabled = switch (feature) {
            case 0 -> ExperimentalFeatures.espBox;
            case 1 -> ExperimentalFeatures.aimBotArc;
            case 2 -> ExperimentalFeatures.aimAtNearestPlayer;
            case 3 -> ExperimentalFeatures.puloBlz;
            case 4 -> ExperimentalFeatures.killAura;
            case 5 -> ExperimentalFeatures.doubleJump;
            case 6 -> ExperimentalFeatures.infiniteLife;
            default -> ExperimentalFeatures.fly;
        };
        return Text.literal(label + "  [" + (enabled ? "ON" : "OFF") + "]");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.fill(0, 0, width, height, 0xB8101420);
        int center = width / 2;
        context.fill(center - 132, height / 2 - 126, center + 132, height / 2 + 112, 0xEE171D2B);
        context.drawCenteredTextWithShadow(textRenderer, "EXPERIMENTAL MENU", center, height / 2 - 116, 0xFF76E6D2);
        context.drawCenteredTextWithShadow(textRenderer, "SHIFT DIREITO abre • ESC fecha", center, height / 2 + 96, 0xFFB7C2D4);
        super.render(context, mouseX, mouseY, delta);
    }
}
