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
        super(Component.translatable("screen.minigames.weaving.result.title"));
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

        /*
         * =========================
         * 결과창 언어 파일 연결
         * =========================
         *
         * 현재 마인크래프트의 언어 설정에 따라
         * 영어 또는 한국어 문구를 가져온다.
         *
         * 점수 계산 및 기존 화면 배치는 변경하지 않는다.
         */

        /* 결과창 제목 */
        String title = Component.translatable(
                "screen.minigames.weaving.result.clear"
        ).getString();

        /* 최종 점수 */
        String scoreLine = Component.translatable(
                "screen.minigames.weaving.result.final_score",
                game.getTotalScore(),
                game.getMaximumScore()
        ).getString();

        /* Fantastic 판정 횟수 */
        String fantasticLine = Component.translatable(
                "screen.minigames.weaving.result.fantastic",
                game.getFantasticCount()
        ).getString();

        /* Perfect 판정 횟수 */
        String perfectLine = Component.translatable(
                "screen.minigames.weaving.result.perfect",
                game.getPerfectCount()
        ).getString();

        /* Good 판정 횟수 */
        String goodLine = Component.translatable(
                "screen.minigames.weaving.result.good",
                game.getGoodCount()
        ).getString();

        /* Miss 판정 횟수 */
        String missLine = Component.translatable(
                "screen.minigames.weaving.result.miss",
                game.getMissCount()
        ).getString();

        /*
         * 1초 클릭 보호 시간에는 대기 문구 표시.
         * 보호 시간이 끝나면 클릭 종료 안내 표시.
         */
        String closeLine = Component.translatable(
                clickGuardTicks > 0
                        ? "screen.minigames.weaving.result.wait"
                        : "screen.minigames.weaving.result.close"
        ).getString();

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