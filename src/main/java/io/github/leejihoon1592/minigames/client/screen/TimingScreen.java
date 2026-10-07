package io.github.leejihoon1592.minigames.client.screen;

import io.github.leejihoon1592.minigames.game.timing.TimingGame;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TimingScreen extends Screen {

    private static final int BAR_WIDTH = 600;
    private static final int BAR_HEIGHT = 20;

    private static final int GOOD_HALF_WIDTH = 60;
    private static final int PERFECT_HALF_WIDTH = 35;
    private static final int FANTASTIC_HALF_WIDTH = 12;

    private static final int POINTER_WIDTH = 4;
    private static final int POINTER_HEIGHT = 32;

    private static final double TRAVEL_TIME_SECONDS = 1.5;

    private static final int MAX_ROUNDS = 5;

    private long startTime;

    private TimingGame game;

    public TimingScreen() {
        super(Component.literal("Timing Game"));
    }

    @Override
    protected void init() {
        super.init();

        this.startTime = System.nanoTime();
        this.game = new TimingGame(MAX_ROUNDS);
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int barWidth = Math.min(BAR_WIDTH, this.width - 40);

        int barX = centerX - barWidth / 2;
        int barY = centerY - BAR_HEIGHT / 2;

        /*
         * =========================
         * 제목 / 라운드
         * =========================
         */

        graphics.drawCenteredString(
                this.font,
                "Timing Game",
                centerX,
                barY - 55,
                0xFFFFFFFF
        );

        if (!game.isFinished()) {

            graphics.drawCenteredString(
                    this.font,
                    "Round "
                            + (game.getCurrentRound() + 1)
                            + " / "
                            + game.getMaxRounds(),
                    centerX,
                    barY - 38,
                    0xFFFFFFFF
            );

        } else {

            graphics.drawCenteredString(
                    this.font,
                    "FINISHED",
                    centerX,
                    barY - 38,
                    0xFFFFD700
            );
        }

        /*
         * =========================
         * 기본 바 - MISS
         * =========================
         */

        graphics.fill(
                barX,
                barY,
                barX + barWidth,
                barY + BAR_HEIGHT,
                0xFF555555
        );

        /*
         * =========================
         * GOOD
         * =========================
         */

        graphics.fill(
                centerX - GOOD_HALF_WIDTH,
                barY,
                centerX + GOOD_HALF_WIDTH,
                barY + BAR_HEIGHT,
                0xFF55AA55
        );

        /*
         * =========================
         * PERFECT
         * =========================
         */

        graphics.fill(
                centerX - PERFECT_HALF_WIDTH,
                barY,
                centerX + PERFECT_HALF_WIDTH,
                barY + BAR_HEIGHT,
                0xFFE0B040
        );

        /*
         * =========================
         * FANTASTIC
         * =========================
         */

        graphics.fill(
                centerX - FANTASTIC_HALF_WIDTH,
                barY,
                centerX + FANTASTIC_HALF_WIDTH,
                barY + BAR_HEIGHT,
                0xFF55AAFF
        );

        /*
         * =========================
         * 포인터
         * =========================
         */

        if (!game.isFinished()) {

            int pointerCenterX =
                    calculatePointerX(barX, barWidth);

            int pointerX =
                    pointerCenterX - POINTER_WIDTH / 2;

            graphics.fill(
                    pointerX,
                    barY - (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                    pointerX + POINTER_WIDTH,
                    barY + BAR_HEIGHT
                            + (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                    0xFFFFFFFF
            );
        }

        /*
         * =========================
         * 마지막 판정
         * =========================
         */

        TimingGame.Judgement lastJudgement =
                game.getLastJudgement();

        if (lastJudgement != null) {

            graphics.drawCenteredString(
                    this.font,
                    lastJudgement.getDisplayName(),
                    centerX,
                    barY + 40,
                    lastJudgement.getColor()
            );
        }

        /*
         * =========================
         * 진행 중 점수
         * =========================
         */

        if (!game.isFinished()) {

            graphics.drawCenteredString(
                    this.font,
                    "Total: " + game.getTotalScore(),
                    centerX,
                    barY + 58,
                    0xFFFFFFFF
            );
        }

        /*
         * =========================
         * 최종 결과
         * =========================
         */

        if (game.isFinished()) {

            graphics.drawCenteredString(
                    this.font,
                    "Total Score: "
                            + game.getTotalScore()
                            + " / "
                            + game.getMaximumScore(),
                    centerX,
                    barY + 58,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    "Average: "
                            + String.format(
                            "%.1f",
                            game.getAverageScore()
                    ),
                    centerX,
                    barY + 75,
                    0xFFFFFFFF
            );
        }

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    /*
     * =========================
     * 좌클릭
     * =========================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button == 0 && !game.isFinished()) {

            submitTiming();

            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    /*
     * =========================
     * 클릭 판정 전달
     * =========================
     */

    private void submitTiming() {

        int centerX = this.width / 2;

        int barWidth =
                Math.min(
                        BAR_WIDTH,
                        this.width - 40
                );

        int barX =
                centerX - barWidth / 2;

        int pointerCenterX =
                calculatePointerX(
                        barX,
                        barWidth
                );

        /*
         * 중앙에서 얼마나 떨어졌는지를
         * 바 절반 길이를 기준으로 0.0 ~ 1.0으로 변환
         *
         * 중앙 = 0.0
         * 끝   = 1.0
         */

        double halfBarWidth =
                barWidth / 2.0;

        double distanceFromCenter =
                Math.abs(pointerCenterX - centerX)
                        / halfBarWidth;

        game.submit(distanceFromCenter);
    }

    /*
     * =========================
     * 포인터 위치 계산
     * =========================
     */

    private int calculatePointerX(
            int barX,
            int barWidth
    ) {

        double elapsedSeconds =
                (System.nanoTime() - this.startTime)
                        / 1_000_000_000.0;

        double cycle =
                elapsedSeconds
                        / TRAVEL_TIME_SECONDS;

        double cyclePosition =
                cycle % 2.0;

        double progress;

        if (cyclePosition <= 1.0) {

            progress = cyclePosition;

        } else {

            progress = 2.0 - cyclePosition;
        }

        progress =
                Mth.clamp(
                        progress,
                        0.0,
                        1.0
                );

        return barX
                + (int) Math.round(
                progress * barWidth
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}