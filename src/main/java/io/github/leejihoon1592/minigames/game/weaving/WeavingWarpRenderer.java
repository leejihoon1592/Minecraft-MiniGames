package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 배짜기 게임의 세로 날실 렌더러.
 *
 * 담당 기능:
 *
 * 1. 고정된 세로 날실 출력
 * 2. 압착된 가로 씨실과 교차 무늬 출력
 * 3. 날실 위치 정보 제공
 *
 * 노트 판정과 점수는 담당하지 않는다.
 */
public final class WeavingWarpRenderer {

    /*
     * =========================
     * 날실 기본 설정
     * =========================
     */

    private static final int BRIGHT_WARP_COLOR = 0xFFE4C94B;
    private static final int DARK_WARP_COLOR = 0xFFC2A12E;

    private static final int WARP_START_X = 12;
    private static final int WARP_SPACING = 12;
    private static final int WARP_THICKNESS = 3;

    private WeavingWarpRenderer() {
    }

    /*
     * =========================
     * 외부 참조용 설정값
     * =========================
     */

    public static int getWarpStartX() {
        return WARP_START_X;
    }

    public static int getWarpSpacing() {
        return WARP_SPACING;
    }

    public static int getWarpThickness() {
        return WARP_THICKNESS;
    }

    /*
     * =========================
     * 날실 색상 선택
     * =========================
     *
     * 짝수 날실 = 밝은 노랑
     * 홀수 날실 = 짙은 노랑
     */
    public static int getWarpColor(int warpIndex) {

        if (warpIndex % 2 == 0) {
            return BRIGHT_WARP_COLOR;
        }

        return DARK_WARP_COLOR;
    }

    /*
     * =========================
     * 특정 X좌표가
     * 씨실 틈 위치인지 확인
     * =========================
     *
     * 날실 중심부가 지나가는 자리에서
     * 씨실을 1픽셀 비워 직조 느낌을 만든다.
     */
    public static boolean shouldLeaveWeftGap(int x) {

        if (x < WARP_START_X) {
            return false;
        }

        int offset = x - WARP_START_X;
        return offset % WARP_SPACING == 0;
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

        int warpIndex = 0;

        for (
                int x = WARP_START_X;
                x < width - WARP_START_X;
                x += WARP_SPACING
        ) {

            graphics.fill(
                    x,
                    clothTop + 3,
                    x + WARP_THICKNESS,
                    clothBottom,
                    getWarpColor(warpIndex)
            );

            warpIndex++;
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

        if (headX <= 0 || rowThickness <= 0) {
            return;
        }

        int warpIndex = 0;

        for (
                int x = WARP_START_X;
                x < width - WARP_START_X && x < headX;
                x += WARP_SPACING
        ) {

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
                        getWarpColor(warpIndex)
                );
            }

            warpIndex++;
        }
    }
}