package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 배짜기 게임의 세로 날실 렌더러.
 *
 * 날실은 고정되어 있으며,
 * 가로 씨실이 그 위로 이동한다.
 *
 * 교차 무늬는 다음 단계에서 처리한다.
 */
public final class WeavingWarpRenderer {

    private static final int WARP_COLOR = 0xFFE4C94B;

    /*
     * 날실 간격 및 두께.
     */
    private static final int WARP_SPACING = 12;
    private static final int WARP_THICKNESS = 2;

    private WeavingWarpRenderer() {
    }

    public static void render(
            GuiGraphics graphics,
            int width,
            int clothTop,
            int clothBottom
    ) {

        /*
         * 화면 좌우에 여백을 남겨
         * 세로 날실을 일정하게 배치한다.
         */

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
}