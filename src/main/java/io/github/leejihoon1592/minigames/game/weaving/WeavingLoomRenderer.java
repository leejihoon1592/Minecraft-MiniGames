package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 배짜기 게임의 베틀 누르기 애니메이션.
 *
 * 흰색 노트가 HIT 또는 MISS로 처리되면
 * 베틀이 아래로 내려갔다가 다시 올라온다.
 *
 * 노트 판정과 점수는 담당하지 않는다.
 */
public final class WeavingLoomRenderer {

    /*
     * 전체 애니메이션 시간: 16틱 (0.8초)
     *
     * 하강: 6틱
     * 누르기 유지: 2틱
     * 상승: 8틱
     */
    private static final int DOWN_TICKS = 6;
    private static final int HOLD_TICKS = 2;
    private static final int UP_TICKS = 8;

    private static final int TOTAL_TICKS =
            DOWN_TICKS + HOLD_TICKS + UP_TICKS;

    private static final int FRAME_COLOR = 0xFF777777;
    private static final int BAR_COLOR = 0xFFBBBBBB;
    private static final int HIGHLIGHT_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_COLOR = 0xFF333333;

    private WeavingLoomRenderer() {
    }

    public static void render(
            GuiGraphics graphics,
            int width,
            int height,
            WeavingGame game,
            double partialTick
    ) {

        int clothTop = 28;

        int clothBottom = Math.max(
                clothTop + 18,
                Math.min(
                        105,
                        height / 2 - 16
                )
        );

        /*
         * 베틀 양쪽의 고정 가이드.
         */
        graphics.fill(
                4,
                clothTop - 6,
                7,
                clothBottom + 8,
                FRAME_COLOR
        );

        graphics.fill(
                width - 7,
                clothTop - 6,
                width - 4,
                clothBottom + 8,
                FRAME_COLOR
        );

        double currentTime =
                game.getElapsedTicks() + partialTick;

        /*
         * 현재 재생해야 할 베틀 애니메이션 확인.
         *
         * HIT/MISS 관계없이 저장된 누르기
         * 시간을 기준으로 재생한다.
         */
        double animationAge = -1.0;

        for (
                int round = 1;
                round <= game.getTotalRounds();
                round++
        ) {

            int pressTick =
                    game.getLoomPressTick(round);

            if (pressTick < 0) {
                continue;
            }

            double age =
                    currentTime - pressTick;

            if (age >= 0.0 && age < TOTAL_TICKS) {
                animationAge = age;
            }
        }

        /*
         * 베틀을 누르지 않은 상태라면
         * 움직이는 바를 출력하지 않는다.
         */
        if (animationAge < 0.0) {
            return;
        }

        /*
         * 이동 진행률
         *
         * 0.0 = 베틀 위쪽
         * 1.0 = 베틀 아래쪽
         */
        double progress;

        if (animationAge < DOWN_TICKS) {

            double t =
                    animationAge / DOWN_TICKS;

            /*
             * 하강할 때 점점 감속한다.
             * 아래에 닿을 때 무게감이 생긴다.
             */
            progress =
                    1.0 - Math.pow(1.0 - t, 3.0);

        } else if (
                animationAge
                        < DOWN_TICKS + HOLD_TICKS
        ) {

            /*
             * 실을 누르는 순간
             * 잠깐 아래에서 멈춘다.
             */
            progress = 1.0;

        } else {

            /*
             * 누른 뒤 다시 상승한다.
             */
            double t =
                    (animationAge
                            - DOWN_TICKS
                            - HOLD_TICKS)
                            / UP_TICKS;

            progress = 1.0 - t;
        }

        progress = Math.max(
                0.0,
                Math.min(1.0, progress)
        );

        int upperY = clothTop - 9;
        int lowerY = clothBottom - 9;

        int barY = (int) Math.round(
                upperY
                        + (lowerY - upperY) * progress
        );

        /*
         * 베틀의 그림자.
         */
        graphics.fill(
                8,
                barY + 7,
                width - 8,
                barY + 11,
                SHADOW_COLOR
        );

        /*
         * 베틀 본체.
         */
        graphics.fill(
                8,
                barY,
                width - 8,
                barY + 8,
                BAR_COLOR
        );

        /*
         * 금속 상단 하이라이트.
         */
        graphics.fill(
                8,
                barY,
                width - 8,
                barY + 2,
                HIGHLIGHT_COLOR
        );

        /*
         * 베틀 아래쪽 테두리.
         */
        graphics.fill(
                8,
                barY + 7,
                width - 8,
                barY + 8,
                FRAME_COLOR
        );
    }
}