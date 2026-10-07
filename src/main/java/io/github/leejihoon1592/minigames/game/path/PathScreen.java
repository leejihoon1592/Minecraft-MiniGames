package io.github.leejihoon1592.minigames.game.path;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PathScreen extends Screen {

    /*
     * =========================
     * 테스트 경로 설정
     * =========================
     */

    private static final int PATH_THICKNESS = 30;

    private static final int PATH_COLOR = 0xFFAAAAAA;
    private static final int START_COLOR = 0xFFFF5555;
    private static final int END_COLOR = 0xFFFF5555;

    /*
     * =========================
     * 게임 상태
     * =========================
     */

    private GameState state = GameState.READY;

    private boolean currentlyInsidePath = false;
    private int pathExitCount = 0;

    public PathScreen() {
        super(
                Component.literal("Path Game")
        );
    }

    /*
     * =========================
     * 화면 렌더링
     * =========================
     */

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

        /*
         * 경로
         */

        drawTestPath(
                graphics,
                centerX,
                centerY
        );

        /*
         * 현재 마우스 위치 판정
         */

        boolean insidePath = isInsidePath(
                mouseX,
                mouseY,
                centerX,
                centerY
        );

        boolean insideStart = isInsideStart(
                mouseX,
                mouseY,
                centerX,
                centerY
        );

        boolean insideEnd = isInsideEnd(
                mouseX,
                mouseY,
                centerX,
                centerY
        );

        /*
         * 게임 상태 갱신
         */

        updateGameState(
                insidePath,
                insideStart,
                insideEnd
        );

        /*
         * 상태 표시
         */

        drawStatus(
                graphics,
                centerX,
                insidePath
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    /*
     * =========================
     * 게임 상태 갱신
     * =========================
     */

    private void updateGameState(
            boolean insidePath,
            boolean insideStart,
            boolean insideEnd
    ) {

        /*
         * READY
         *
         * START 영역에 마우스를 올리면 시작
         */

        if (state == GameState.READY) {

            if (insideStart) {
                state = GameState.PLAYING;
                currentlyInsidePath = true;
            }

            return;
        }

        /*
         * PLAYING
         */

        if (state == GameState.PLAYING) {

            /*
             * END에 도착하면 완료
             */

            if (insideEnd) {
                state = GameState.FINISHED;
                return;
            }

            /*
             * 경로 이탈 감지
             *
             * 계속 밖에 있는 동안 숫자가 증가하는 것이 아니라,
             * 안 → 밖으로 넘어가는 순간에만 1회 증가
             */

            if (currentlyInsidePath && !insidePath) {
                pathExitCount++;
            }

            currentlyInsidePath = insidePath;
        }
    }

    /*
     * =========================
     * 상태 표시
     * =========================
     */

    private void drawStatus(
            GuiGraphics graphics,
            int centerX,
            boolean insidePath
    ) {

        String mainText;
        int mainColor;

        switch (state) {

            case READY -> {
                mainText = "MOVE TO START";
                mainColor = 0xFFFFFFFF;
            }

            case PLAYING -> {

                if (insidePath) {
                    mainText = "PATH : INSIDE";
                    mainColor = 0xFF55FF55;
                } else {
                    mainText = "PATH : OUTSIDE";
                    mainColor = 0xFFFF5555;
                }
            }

            case FINISHED -> {
                mainText = "FINISHED";
                mainColor = 0xFFFFD700;
            }

            default -> {
                mainText = "";
                mainColor = 0xFFFFFFFF;
            }
        }

        graphics.drawCenteredString(
                this.font,
                mainText,
                centerX,
                30,
                mainColor
        );

        /*
         * 플레이 중 / 완료 후
         * 이탈 횟수 표시
         */

        if (state == GameState.PLAYING
                || state == GameState.FINISHED) {

            graphics.drawCenteredString(
                    this.font,
                    "Path Exits : " + pathExitCount,
                    centerX,
                    45,
                    0xFFFFFFFF
            );
        }
    }

    /*
     * =========================
     * 테스트 경로 그리기
     * =========================
     */

    private void drawTestPath(
            GuiGraphics graphics,
            int centerX,
            int centerY
    ) {

        int startX = centerX - 300;
        int startY = centerY - 50;

        /*
         * 1번 구간
         */

        drawHorizontalPath(
                graphics,
                startX,
                startY,
                220
        );

        /*
         * 2번 구간
         */

        drawVerticalPath(
                graphics,
                startX + 220 - PATH_THICKNESS,
                startY,
                100
        );

        /*
         * 3번 구간
         */

        drawHorizontalPath(
                graphics,
                startX + 100,
                startY + 100 - PATH_THICKNESS,
                120
        );

        /*
         * 4번 구간
         */

        drawVerticalPath(
                graphics,
                startX + 100,
                startY + 100 - PATH_THICKNESS,
                80
        );

        /*
         * 5번 구간
         */

        drawHorizontalPath(
                graphics,
                startX + 100,
                startY + 150,
                400
        );

        /*
         * START
         */

        graphics.fill(
                startX,
                startY,
                startX + 5,
                startY + PATH_THICKNESS,
                START_COLOR
        );

        /*
         * END
         */

        graphics.fill(
                startX + 495,
                startY + 150,
                startX + 500,
                startY + 150 + PATH_THICKNESS,
                END_COLOR
        );
    }

    /*
     * =========================
     * 전체 경로 내부 판정
     * =========================
     */

    private boolean isInsidePath(
            double mouseX,
            double mouseY,
            int centerX,
            int centerY
    ) {

        int startX = centerX - 300;
        int startY = centerY - 50;

        /*
         * 1번
         */

        if (isInsideRectangle(
                mouseX,
                mouseY,
                startX,
                startY,
                220,
                PATH_THICKNESS
        )) {
            return true;
        }

        /*
         * 2번
         */

        if (isInsideRectangle(
                mouseX,
                mouseY,
                startX + 220 - PATH_THICKNESS,
                startY,
                PATH_THICKNESS,
                100
        )) {
            return true;
        }

        /*
         * 3번
         */

        if (isInsideRectangle(
                mouseX,
                mouseY,
                startX + 100,
                startY + 100 - PATH_THICKNESS,
                120,
                PATH_THICKNESS
        )) {
            return true;
        }

        /*
         * 4번
         */

        if (isInsideRectangle(
                mouseX,
                mouseY,
                startX + 100,
                startY + 100 - PATH_THICKNESS,
                PATH_THICKNESS,
                80
        )) {
            return true;
        }

        /*
         * 5번
         */

        return isInsideRectangle(
                mouseX,
                mouseY,
                startX + 100,
                startY + 150,
                400,
                PATH_THICKNESS
        );
    }

    /*
     * =========================
     * START 판정
     * =========================
     */

    private boolean isInsideStart(
            double mouseX,
            double mouseY,
            int centerX,
            int centerY
    ) {

        int startX = centerX - 300;
        int startY = centerY - 50;

        return isInsideRectangle(
                mouseX,
                mouseY,
                startX,
                startY,
                10,
                PATH_THICKNESS
        );
    }

    /*
     * =========================
     * END 판정
     * =========================
     */

    private boolean isInsideEnd(
            double mouseX,
            double mouseY,
            int centerX,
            int centerY
    ) {

        int startX = centerX - 300;
        int startY = centerY - 50;

        return isInsideRectangle(
                mouseX,
                mouseY,
                startX + 490,
                startY + 150,
                10,
                PATH_THICKNESS
        );
    }

    /*
     * =========================
     * 사각형 내부 판정
     * =========================
     */

    private boolean isInsideRectangle(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    /*
     * =========================
     * 가로 경로
     * =========================
     */

    private void drawHorizontalPath(
            GuiGraphics graphics,
            int x,
            int y,
            int length
    ) {

        graphics.fill(
                x,
                y,
                x + length,
                y + PATH_THICKNESS,
                PATH_COLOR
        );
    }

    /*
     * =========================
     * 세로 경로
     * =========================
     */

    private void drawVerticalPath(
            GuiGraphics graphics,
            int x,
            int y,
            int length
    ) {

        graphics.fill(
                x,
                y,
                x + PATH_THICKNESS,
                y + length,
                PATH_COLOR
        );
    }

    /*
     * =========================
     * 게임 상태
     * =========================
     */

    private enum GameState {
        READY,
        PLAYING,
        FINISHED
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}