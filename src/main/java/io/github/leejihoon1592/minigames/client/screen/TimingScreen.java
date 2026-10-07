package io.github.leejihoon1592.minigames.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TimingScreen extends Screen {

    private static final int BAR_WIDTH = 600;
    private static final int BAR_HEIGHT = 20;

    // 중앙 기준 판정 범위
    private static final int GOOD_HALF_WIDTH = 60;
    private static final int PERFECT_HALF_WIDTH = 35;
    private static final int FANTASTIC_HALF_WIDTH = 12;

    private static final int POINTER_WIDTH = 4;
    private static final int POINTER_HEIGHT = 32;

    // 포인터가 한쪽 끝에서 반대쪽 끝까지 이동하는 시간
    private static final double TRAVEL_TIME_SECONDS = 1.5;

    private long startTime;

    // 마지막 판정 결과
    private Judgement lastJudgement = null;

    public TimingScreen() {
        super(Component.literal("Timing Game"));
    }

    @Override
    protected void init() {
        super.init();

        // 화면이 열린 순간을 시작 시각으로 저장
        this.startTime = System.nanoTime();
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

        // 작은 해상도에서도 화면 밖으로 나가지 않도록 제한
        int barWidth = Math.min(BAR_WIDTH, this.width - 40);

        int barX = centerX - barWidth / 2;
        int barY = centerY - BAR_HEIGHT / 2;

        /*
         * =========================
         * 기본 바 - MISS 영역
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
         * GOOD 영역
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
         * PERFECT 영역
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
         * FANTASTIC 영역
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

        int pointerCenterX = calculatePointerX(barX, barWidth);

        int pointerX =
                pointerCenterX - POINTER_WIDTH / 2;

        graphics.fill(
                pointerX,
                barY - (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                pointerX + POINTER_WIDTH,
                barY + BAR_HEIGHT + (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 제목
         * =========================
         */

        graphics.drawCenteredString(
                this.font,
                "Timing Game",
                centerX,
                barY - 40,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 판정 결과
         * =========================
         */

        if (lastJudgement != null) {

            graphics.drawCenteredString(
                    this.font,
                    lastJudgement.displayName,
                    centerX,
                    barY + 40,
                    lastJudgement.color
            );

            graphics.drawCenteredString(
                    this.font,
                    "Score: " + lastJudgement.score,
                    centerX,
                    barY + 55,
                    0xFFFFFFFF
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /*
     * =========================
     * 좌클릭 입력
     * =========================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        // 마우스 왼쪽 버튼
        if (button == 0) {
            judgeTiming();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /*
     * =========================
     * 현재 포인터 위치 계산
     * =========================
     */

    private int calculatePointerX(
            int barX,
            int barWidth
    ) {

        double elapsedSeconds =
                (System.nanoTime() - this.startTime)
                        / 1_000_000_000.0;

        // 0 → 1 → 0 → 1 왕복
        double cycle =
                elapsedSeconds / TRAVEL_TIME_SECONDS;

        double cyclePosition =
                cycle % 2.0;

        double progress;

        if (cyclePosition <= 1.0) {
            progress = cyclePosition;
        } else {
            progress = 2.0 - cyclePosition;
        }

        progress =
                Mth.clamp(progress, 0.0, 1.0);

        return barX
                + (int) Math.round(progress * barWidth);
    }

    /*
     * =========================
     * 판정
     * =========================
     */

    private void judgeTiming() {

        int centerX = this.width / 2;

        int barWidth =
                Math.min(BAR_WIDTH, this.width - 40);

        int barX =
                centerX - barWidth / 2;

        // 클릭한 바로 그 순간의 위치를 계산
        int pointerCenterX =
                calculatePointerX(barX, barWidth);

        int distance =
                Math.abs(pointerCenterX - centerX);

        if (distance <= FANTASTIC_HALF_WIDTH) {

            lastJudgement = Judgement.FANTASTIC;

        } else if (distance <= PERFECT_HALF_WIDTH) {

            lastJudgement = Judgement.PERFECT;

        } else if (distance <= GOOD_HALF_WIDTH) {

            lastJudgement = Judgement.GOOD;

        } else {

            lastJudgement = Judgement.MISS;
        }
    }

    /*
     * =========================
     * 판정 종류
     * =========================
     */

    private enum Judgement {

        FANTASTIC(
                "FANTASTIC!",
                100,
                0xFF55AAFF
        ),

        PERFECT(
                "PERFECT!",
                80,
                0xFFE0B040
        ),

        GOOD(
                "GOOD!",
                50,
                0xFF55FF55
        ),

        MISS(
                "MISS!",
                0,
                0xFFFF5555
        );

        private final String displayName;
        private final int score;
        private final int color;

        Judgement(
                String displayName,
                int score,
                int color
        ) {
            this.displayName = displayName;
            this.score = score;
            this.color = color;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}