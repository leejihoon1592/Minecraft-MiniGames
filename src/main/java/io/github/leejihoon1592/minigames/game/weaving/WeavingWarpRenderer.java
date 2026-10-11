package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 배짜기 게임의 세로 날실 렌더러.
 *
 * 담당 기능:
 *
 * 1. 고정된 세로 날실 출력
 * 2. 압착된 가로 씨실과 교차 무늬 출력
 *
 * 노트 판정과 점수는 담당하지 않는다.
 */
public final class WeavingWarpRenderer {

    /*
     * =========================
     * 날실 기본 설정
     * =========================
     */

    private static final int WARP_COLOR = 0xFFE4C94B;

    private static final int WARP_SPACING = 12;
    private static final int WARP_THICKNESS = 2;

    private WeavingWarpRenderer() {
    }

    /*
     * =========================
     * 고정된 세로 날실 출력
     * =========================
     *
     * 프레임마다 단 한 번 호출한다.
     *
     * 가로 실을 출력하기 전에
     * 배경 날실부터 그린다.
     */
    public static void render(
            GuiGraphics graphics,
            int width,
            int clothTop,
            int clothBottom
    ) {

        for (
                int x = 12;
                x < width - 12;
                x += WARP_SPACING
        ) {

            graphics.fill(
                    x,
                    clothTop + 3,
                    x + WARP_THICKNESS,
                    clothBottom,
                    WARP_COLOR
            );
        }
    }

    /*
     * =========================
     * 가로 씨실과 날실의 교차 표현
     * =========================
     *
     * 압착 완료된 가로 실 하나에 대해
     * 필요한 교차 지점만 계산한다.
     *
     * 가로 실 전체를 다시 그리지 않고
     * 날실이 위로 올라오는 부분만 그린다.
     */
    public static void renderCrossings(
            GuiGraphics graphics,
            int width,
            int headX,
            int rowY,
            int rowThickness,
            int rowIndex
    ) {

        /*
         * 아직 가로 실이 없는 경우
         * 교차 무늬를 그리지 않는다.
         */
        if (headX <= 0 || rowThickness <= 0) {
            return;
        }

        /*
         * 날실 번호.
         */
        int warpIndex = 0;

        /*
         * =========================
         * 교차 지점 확인
         * =========================
         *
         * 홀수 / 짝수 위치를 이용해
         * 날실이 씨실 위로 나오는 부분을
         * 번갈아 선택한다.
         */
        for (
                int x = 12;
                x < width - 12 && x < headX;
                x += WARP_SPACING
        ) {

            /*
             * 가로 실 번호와 날실 번호의
             * 합이 짝수일 때 날실을 위에 그린다.
             *
             * 다음 가로 실은 자동으로
             * 반대 교차 패턴을 가진다.
             */
            boolean warpOnTop =
                    (rowIndex + warpIndex) % 2 == 0;

            if (warpOnTop) {

                graphics.fill(
                        x,
                        rowY,
                        Math.min(
                                x + WARP_THICKNESS,
                                headX
                        ),
                        rowY + rowThickness,
                        WARP_COLOR
                );
            }

            warpIndex++;
        }
    }
}