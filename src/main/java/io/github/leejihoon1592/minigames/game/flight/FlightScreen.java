package io.github.leejihoon1592.minigames.game.flight;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class FlightScreen extends Screen {

    /*
     * =========================
     * 화면 설정
     * =========================
     */

    private static final int PLAYER_X_OFFSET = 120;

    /*
     * 실 크기
     */

    private static final int THREAD_WIDTH = 6;
    private static final int THREAD_HEIGHT = 6;

    /*
     * 비행 통로 높이
     */

    private static final int FLIGHT_AREA_HEIGHT = 210;

    /*
     * 빨간 통로
     */

    private static final int CORRIDOR_HALF_HEIGHT = 38;
    private static final int CORRIDOR_LINE_THICKNESS = 3;

    /*
     * 노란 포인트 크기
     */

    private static final int YELLOW_POINT_WIDTH = 12;
    private static final int YELLOW_POINT_HEIGHT = 30;

    /*
     * 초록 포인트 크기
     */

    private static final int GREEN_POINT_WIDTH = 22;
    private static final int GREEN_POINT_HEIGHT = 22;

    /*
     * 포인트 이동 속도
     */

    private static final double POINT_SCROLL_SPEED = 4.0;

    /*
     * 포인트 최소 간격
     */

    private static final double MIN_POINT_GAP = 160.0;

    /*
     * =========================
     * 임시 물리값
     * =========================
     */

    private static final double GRAVITY = 0.18;
    private static final double HOLD_FORCE = 0.35;

    private static final double MAX_FALL_SPEED = 4.0;
    private static final double MAX_RISE_SPEED = -4.0;

    /*
     * =========================
     * 실 상태
     * =========================
     */

    /* 실 */

    private double threadY;
    private double previousThreadY;
    private double velocityY;

    /* 노란 포인트 */

    private double yellowPointX;
    private double previousYellowPointX;
    private double yellowPointY;
    private boolean yellowPointActive;

    /* 초록 포인트 */

    private double greenPointX;
    private double previousGreenPointX;
    private double greenPointY;
    private boolean greenPointActive;

    /* 빨간 통로 */

    private double corridorScroll;
    private double previousCorridorScroll;

    public FlightScreen() {
        super(Component.literal("Flight Game"));
    }

    @Override
    protected void init() {

        this.threadY =
                this.height / 2.0;

        this.previousThreadY =
                this.threadY;

        this.velocityY =
                0.0;

        /* 노란 포인트 */

        this.yellowPointX =
                this.width + YELLOW_POINT_WIDTH;

        this.previousYellowPointX =
                this.yellowPointX;

        this.yellowPointY =
                getRandomPointY(
                        this.yellowPointX,
                        YELLOW_POINT_HEIGHT
                );

        /* 초록 포인트 */

        this.greenPointX =
                this.yellowPointX
                        + YELLOW_POINT_WIDTH
                        + MIN_POINT_GAP;

        this.previousGreenPointX =
                this.greenPointX;

        this.greenPointY =
                getRandomPointY(
                        this.greenPointX,
                        GREEN_POINT_HEIGHT
                );

        /* 빨간 통로 */

        this.corridorScroll =
                0.0;

        this.previousCorridorScroll =
                0.0;

    }

    @Override
    public void tick() {

        /*
         * 렌더 보간용
         * 현재 위치를 갱신하기 전에
         * 이전 위치를 저장한다.
         */

        previousThreadY =
                threadY;

        /*
         * 좌클릭을 누르고 있는 동안
         * 계속 위로 힘을 준다.
         */

        long window =
                this.minecraft
                        .getWindow()
                        .getWindow();

        boolean holdingLeftMouse =
                GLFW.glfwGetMouseButton(
                        window,
                        GLFW.GLFW_MOUSE_BUTTON_LEFT
                )
                        == GLFW.GLFW_PRESS;

        if (holdingLeftMouse) {

            velocityY -=
                    HOLD_FORCE;
        }

        /*
         * 중력
         */

        velocityY +=
                GRAVITY;

        velocityY =
                Mth.clamp(
                        velocityY,
                        MAX_RISE_SPEED,
                        MAX_FALL_SPEED
                );

        /*
         * Y 이동
         */

        threadY +=
                velocityY;

        /*
         * 화면 위 / 아래 제한
         */

        double minY =
                (this.height - FLIGHT_AREA_HEIGHT)
                        / 2.0;

        double maxY =
                (this.height + FLIGHT_AREA_HEIGHT)
                        / 2.0
                        - THREAD_HEIGHT;

        if (threadY < minY) {

            threadY =
                    minY;

            velocityY =
                    0.0;
        }

        if (threadY > maxY) {

            threadY =
                    maxY;

            velocityY =
                    0.0;

        }
        /*
         * 노란 포인트 스크롤
         */

        /* 노란 포인트 */

        previousYellowPointX =
                yellowPointX;

        yellowPointX -=
                POINT_SCROLL_SPEED;

        if (yellowPointX < -YELLOW_POINT_WIDTH) {

            /*
             * 노란 포인트는
             * 초록 포인트 뒤에서 최소 간격을 확보한다.
             */

            yellowPointX =
                    Math.max(
                            this.width + YELLOW_POINT_WIDTH,
                            greenPointX
                                    + GREEN_POINT_WIDTH
                                    + MIN_POINT_GAP
                    );

            previousYellowPointX =
                    yellowPointX;
            yellowPointY =
                    getRandomPointY(
                            yellowPointX,
                            YELLOW_POINT_HEIGHT
                    );
        }

        /* 초록 포인트 */

        previousGreenPointX =
                greenPointX;

        greenPointX -=
                POINT_SCROLL_SPEED;

        if (greenPointX < -GREEN_POINT_WIDTH) {

            /*
             * 초록 포인트는
             * 노란 포인트 뒤에서 최소 간격을 확보한다.
             */

            greenPointX =
                    Math.max(
                            this.width + GREEN_POINT_WIDTH,
                            yellowPointX
                                    + YELLOW_POINT_WIDTH
                                    + MIN_POINT_GAP
                    );

            previousGreenPointX =
                    greenPointX;

            greenPointY =
                    getRandomPointY(
                            greenPointX,
                            GREEN_POINT_HEIGHT
                    );
        }
        /*
         * =========================
         * 빨간 통로 스크롤
         * =========================
         */

        previousCorridorScroll =
                corridorScroll;

        corridorScroll +=
                POINT_SCROLL_SPEED;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        /*
         * =========================
         * 배경
         * =========================
         */

        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                0xFF202020
        );

        /*
         * =========================
         * 비행 가능 범위
         * =========================
         */

        int top =
                (this.height - FLIGHT_AREA_HEIGHT)
                        / 2;

        int bottom =
                top + FLIGHT_AREA_HEIGHT;

        /*
         * 위쪽 경계선
         */

        graphics.fill(
                0,
                top,
                this.width,
                top + 2,
                0xFF666666
        );

        /*
         * 아래쪽 경계선
         */

        graphics.fill(
                0,
                bottom - 2,
                this.width,
                bottom,
                0xFF666666
        );
        /*
         * 빨간 통로
         */

        drawCorridor(
                graphics,
                partialTick
        );

        /*
         * =========================
         * 노란 포인트
         * =========================
         */

        /* 노란 포인트 */

        double interpolatedYellowPointX =
                Mth.lerp(
                        partialTick,
                        previousYellowPointX,
                        yellowPointX
                );

        int renderYellowPointX =
                (int) Math.round(
                        interpolatedYellowPointX
                );

        int renderYellowPointY =
                (int) Math.round(
                        yellowPointY
                );

        graphics.fill(
                renderYellowPointX,
                renderYellowPointY,
                renderYellowPointX + YELLOW_POINT_WIDTH,
                renderYellowPointY + YELLOW_POINT_HEIGHT,
                0xFFFFFF00
        );

        /* 초록 포인트 */

        double interpolatedGreenPointX =
                Mth.lerp(
                        partialTick,
                        previousGreenPointX,
                        greenPointX
                );

        int renderGreenPointX =
                (int) Math.round(
                        interpolatedGreenPointX
                );

        int renderGreenPointY =
                (int) Math.round(
                        greenPointY
                );

        graphics.fill(
                renderGreenPointX,
                renderGreenPointY,
                renderGreenPointX + GREEN_POINT_WIDTH,
                renderGreenPointY + GREEN_POINT_HEIGHT,
                0xFF00FF00
        );

        /*
         * =========================
         * 실
         * =========================
         */

        int threadX =
                PLAYER_X_OFFSET;

        /*
         * 이전 Tick 위치와
         * 현재 Tick 위치 사이 보간
         */

        double interpolatedY =
                Mth.lerp(
                        partialTick,
                        previousThreadY,
                        threadY
                );

        int renderY =
                (int) Math.round(
                        interpolatedY
                );

        graphics.fill(
                threadX,
                renderY,
                threadX + THREAD_WIDTH,
                renderY + THREAD_HEIGHT,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 테스트 정보
         * =========================
         */

        graphics.drawString(
                this.font,
                "Y : "
                        + String.format(
                        "%.1f",
                        threadY
                ),
                10,
                10,
                0xFFFFFFFF
        );

        graphics.drawString(
                this.font,
                "Velocity : "
                        + String.format(
                        "%.2f",
                        velocityY
                ),
                10,
                22,
                0xFFFFFFFF
        );
    }
        /*
     * =========================
     * 빨간 통로 렌더링
     * =========================
     */

    private void drawCorridor(
            GuiGraphics graphics,
            float partialTick
    ) {

        double renderedScroll =
                Mth.lerp(
                        partialTick,
                        previousCorridorScroll,
                        corridorScroll
                );

        for (int x = 0; x < this.width; x += 2) {

            double worldX =
                    renderedScroll + x;

            double centerY =
                    getCorridorCenterY(
                            worldX
                    );

            int topY =
                    (int) Math.round(
                            centerY
                                    - CORRIDOR_HALF_HEIGHT
                    );

            int bottomY =
                    (int) Math.round(
                            centerY
                                    + CORRIDOR_HALF_HEIGHT
                    );

            /*
             * 위쪽 빨간선
             */

            graphics.fill(
                    x,
                    topY,
                    x + 2,
                    topY + CORRIDOR_LINE_THICKNESS,
                    0xFFFF3030
            );

            /*
             * 아래쪽 빨간선
             */

            graphics.fill(
                    x,
                    bottomY,
                    x + 2,
                    bottomY + CORRIDOR_LINE_THICKNESS,
                    0xFFFF3030
            );
        }
    }

    /*
     * =========================
     * 빨간 통로 중심선
     * =========================
     */

    private double getCorridorCenterY(
            double worldX
    ) {

        /*
         * 반복되는 하나의 통로 패턴
         */

        double patternLength =
                1080.0;

        double localX =
                worldX % patternLength;

        if (localX < 0.0) {
            localX += patternLength;
        }

        double center =
                this.height / 2.0;

        /*
         * 1구간
         * 직선
         */

        if (localX < 240.0) {

            return center;
        }

        /*
         * 2구간
         * 아래쪽으로 경사
         */

        if (localX < 420.0) {

            double progress =
                    (localX - 240.0)
                            / 180.0;

            return center
                    + 55.0 * progress;
        }

        /*
         * 3구간
         * 아래쪽 직선
         */

        if (localX < 660.0) {

            return center + 55.0;
        }

        /*
         * 4구간
         * 다시 중앙으로 경사
         */

        if (localX < 840.0) {

            double progress =
                    (localX - 660.0)
                            / 180.0;

            return center
                    + 55.0
                    - 55.0 * progress;
        }

        /*
         * 5구간
         * 중앙 직선
         */

        return center;
    }

    /*
     * =========================
     * 포인트 랜덤 Y 위치
     * =========================
     */

    private double getRandomPointY(
            double pointScreenX,
            int pointHeight
    ) {

        /*
         * 현재 화면 X 위치가
         * 빨간 통로의 어느 위치에 해당하는지 계산
         */

        double worldX =
                corridorScroll
                        + pointScreenX;

        double corridorCenterY =
                getCorridorCenterY(
                        worldX
                );

        /*
         * 빨간선과 포인트 사이에
         * 약간의 여유를 둔다.
         */

        double margin =
                6.0;

        double minY =
                corridorCenterY
                        - CORRIDOR_HALF_HEIGHT
                        + margin;

        double maxY =
                corridorCenterY
                        + CORRIDOR_HALF_HEIGHT
                        - pointHeight
                        - margin;

        /*
         * 통로 안쪽에서만 랜덤 생성
         */

        return minY
                + Math.random()
                * (maxY - minY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}