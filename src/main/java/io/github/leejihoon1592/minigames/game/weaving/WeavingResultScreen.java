package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WeavingResultScreen extends Screen {

    /*
     * =========================
     * 결과 데이터
     * =========================
     */
    private final WeavingGame game;

    /*
     * =========================
     * 클릭 보호 시간
     * =========================
     *
     * 20틱 = 1초
     */
    private int clickGuardTicks = 20;

    public WeavingResultScreen(WeavingGame game) {
        super(Component.literal("Weaving Result"));
        this.game = game;
    }

    @Override
    public void tick() {
        if (clickGuardTicks > 0) {
            clickGuardTicks--;
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        /*
         * =========================
         * 1초 클릭 무적
         * =========================
         */
        if (clickGuardTicks > 0) {
            return true;
        }

        this.onClose();
        return true;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        /*
         * ESC는 바로 닫기 허용
         */
        if (keyCode == 256) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        this.renderBackground(graphics);

        String title = "WEAVING CLEAR!";
        String scoreLine =
                "FINAL SCORE : "
                        + game.getTotalScore()
                        + " / "
                        + game.getMaximumScore();

        String fantasticLine =
                "Fantastic : " + game.getFantasticCount();

        String perfectLine =
                "Perfect : " + game.getPerfectCount();

        String goodLine =
                "Good : " + game.getGoodCount();

        String missLine =
                "Miss : " + game.getMissCount();

        String closeLine =
                (clickGuardTicks > 0)
                        ? "PLEASE WAIT..."
                        : "CLICK TO CLOSE";

        int centerX = this.width / 2;
        int y = 40;

        drawCenteredNoShadow(
                graphics,
                title,
                centerX,
                y,
                0xFFFFFF55
        );

        y += 26;

        drawCenteredNoShadow(
                graphics,
                scoreLine,
                centerX,
                y,
                0xFFFFFFFF
        );

        y += 24;

        drawCenteredNoShadow(
                graphics,
                fantasticLine,
                centerX,
                y,
                0xFF55FFFF
        );

        y += 16;

        drawCenteredNoShadow(
                graphics,
                perfectLine,
                centerX,
                y,
                0xFFFFFF55
        );

        y += 16;

        drawCenteredNoShadow(
                graphics,
                goodLine,
                centerX,
                y,
                0xFF55FF55
        );

        y += 16;

        drawCenteredNoShadow(
                graphics,
                missLine,
                centerX,
                y,
                0xFFFF5555
        );

        y += 28;

        drawCenteredNoShadow(
                graphics,
                closeLine,
                centerX,
                y,
                0xFFFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCenteredNoShadow(
            GuiGraphics graphics,
            String text,
            int centerX,
            int y,
            int color
    ) {
        graphics.drawString(
                this.font,
                text,
                centerX - this.font.width(text) / 2,
                y,
                color,
                false
        );
    }
}