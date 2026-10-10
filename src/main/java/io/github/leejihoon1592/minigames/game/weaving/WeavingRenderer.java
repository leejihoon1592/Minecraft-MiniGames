package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 천 배짜기 게임의 기본 화면 출력.
 *
 * 노트, 정확도 판정기, 임시 천 영역을 그린다.
 * 이후 실과 베틀의 애니메이션을 확장한다.
 */
public final class WeavingRenderer {

    private static final int BLUE = 0xFF3549CD;
    private static final int RED = 0xFFE91E29;
    private static final int WHITE = 0xFFFFFFFF;

    private WeavingRenderer() {
    }

    public static void render(
            GuiGraphics graphics,
            Font font,
            int width,
            int height,
            WeavingGame game,
            WeavingConfig config,
            double partialTick,
            Component message
    ) {

        /*
         * 전체 배경
         */

        graphics.fill(
                0, 0, width, height,
                0xFF000000
        );

        /*
         * =========================
         * 상단 임시 천 영역
         * =========================
         *
         * 지금은 고정된 실을 출력한다.
         * 실제 실의 움직임과 정렬은
         * 다음 개발 단계에서 구현한다.
         */

        int clothTop = 28;
        int clothBottom = Math.min(105, height / 2);

        /*
         * =========================
         * 실제 천 제작 애니메이션
         * =========================
         *
         * 상단 실의 이동과 출력은
         * WeavingClothRenderer가 담당한다.
         *
         * 하단 노트 판정 로직에는
         * 영향을 주지 않는다.
         */

        WeavingClothRenderer.render(
                graphics,
                width,
                height,
                game,
                config,
                partialTick
        );

        /*
         * =========================
         * 판정기와 노트 위치
         * =========================
         */

        int hitX = Math.max(
                50,
                Math.min(85, width / 5)
        );

        int laneY = (int) (height * 0.76);

        /*
         * 첫 노트가 시작 시 화면 오른쪽에
         * 나타나도록 이동 속도를 계산한다.
         */
        double pixelsPerTick =
                (double) (width - hitX - 14)
                        / config.getApproachTicks();

        /*
         * 판정기 중심 안내선
         */
        graphics.fill(
                hitX - 1,
                laneY - 29,
                hitX + 1,
                laneY + 29,
                0xFFD4B65A
        );

        /*
         * 정확도 판정기 바깥 원
         */
        drawCircleOutline(
                graphics, hitX, laneY, 23, WHITE
        );

        drawCircleOutline(
                graphics, hitX, laneY, 20, WHITE
        );

        drawCircleOutline(
                graphics, hitX, laneY, 17, WHITE
        );

        /*
         * =========================
         * 이동 노트
         * =========================
         */

        double currentTime =
                game.getElapsedTicks() + partialTick;

        for (WeavingNote note : game.getNotes()) {

            if (!note.isWaiting()) {
                continue;
            }

            int noteX = (int) Math.round(
                    hitX
                            + (note.getTargetTick() - currentTime)
                            * pixelsPerTick
            );

            /*
             * 화면에 보이는 노트만 출력한다.
             */
            if (noteX < -20 || noteX > width + 20) {
                continue;
            }

            int color = switch (note.getType()) {
                case LEFT -> BLUE;
                case RIGHT -> RED;
                case SPACE -> WHITE;
            };

            drawFilledCircle(
                    graphics,
                    noteX,
                    laneY,
                    15,
                    color
            );
        }

        /*
         * =========================
         * 진행 정보
         * =========================
         */

        int currentRound =
                Math.min(
                        game.getCompletedRounds() + 1,
                        game.getTotalRounds()
                );

        graphics.drawString(
                font,
                "ROUND "
                        + currentRound
                        + " / "
                        + game.getTotalRounds(),
                10,
                10,
                WHITE
        );

        graphics.drawString(
                font,
                "HIT "
                        + game.getHitCount()
                        + "   MISS "
                        + game.getMissCount(),
                10,
                height - 17,
                WHITE
        );

        graphics.drawCenteredString(
                font,
                message,
                width / 2,
                Math.min(height - 35, laneY + 36),
                WHITE
        );
    }

    /*
     * =========================
     * 원형 노트 출력
     * =========================
     */

    private static void drawFilledCircle(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {

        for (int y = -radius; y <= radius; y++) {

            int halfWidth = (int) Math.sqrt(
                    radius * radius - y * y
            );

            graphics.fill(
                    centerX - halfWidth,
                    centerY + y,
                    centerX + halfWidth + 1,
                    centerY + y + 1,
                    color
            );
        }
    }

    /*
     * =========================
     * 정확도 판정기 원형 테두리
     * =========================
     */

    private static void drawCircleOutline(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {

        for (int y = -radius; y <= radius; y++) {

            int halfWidth = (int) Math.sqrt(
                    radius * radius - y * y
            );

            graphics.fill(
                    centerX - halfWidth,
                    centerY + y,
                    centerX - halfWidth + 1,
                    centerY + y + 1,
                    color
            );

            graphics.fill(
                    centerX + halfWidth,
                    centerY + y,
                    centerX + halfWidth + 1,
                    centerY + y + 1,
                    color
            );
        }
    }
}