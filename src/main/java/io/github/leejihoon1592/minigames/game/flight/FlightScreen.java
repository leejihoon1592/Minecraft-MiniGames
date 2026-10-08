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

    /*
     * =========================
     * Flight 게임 점수 관리
     * =========================
     *
     * FlightGame은 현재점수, 최대점수,
     * 보너스 점수, MISS 횟수를 관리한다.
     *
     * FlightScreen은 실제 게임 화면과
     * 포인트 획득 판정을 담당한다.
     */

    private final FlightGame flightGame = new FlightGame();

    /*
     * =========================
     * 보너스 문구 표시 관리
     * =========================
     *
     * 노란 포인트 획득:
     * 보너스 +10%!
     *
     * 초록 포인트 획득:
     * 보너스 +20%!
     *
     * bonusMessage:
     * 화면에 출력할 문구를 저장한다.
     *
     * bonusMessageTicks:
     * 문구가 화면에 남아 있을 시간을 관리한다.
     * 20Tick = 약 1초
     */

    private String bonusMessage = "";
    private int bonusMessageTicks = 0;

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

        this.yellowPointActive = true;

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

        this.greenPointActive = true;

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

        /*
         * =========================
         * 노란 포인트 충돌 판정
         * =========================
         *
         * 노란 포인트가 활성화되어 있을 때
         * 흰색 실과 겹치는지 확인한다.
         *
         * 겹쳤다면 비활성화하여
         * 화면에서 사라지게 한다.
         */

        if (yellowPointActive
                && PLAYER_X_OFFSET < yellowPointX + YELLOW_POINT_WIDTH
                && PLAYER_X_OFFSET + THREAD_WIDTH > yellowPointX
                && threadY < yellowPointY + YELLOW_POINT_HEIGHT
                && threadY + THREAD_HEIGHT > yellowPointY) {

            /*
             * 노란 포인트 획득 처리
             *
             * 포인트를 비활성화하여
             * 화면에서 사라지게 한다.
             */
            yellowPointActive = false;

            /*
             * 노란 포인트 보너스 적용
             *
             * 현재점수 +100
             * 최대점수 +100
             *
             * 최대점수는 2000을 넘지 않는다.
             *
             * 실제 점수 계산은
             * FlightGame에서 처리한다.
             */
            flightGame.collectYellowPoint();

            /*
             * =========================
             * 노란 포인트 보너스 문구
             * =========================
             *
             * 노란 포인트를 획득하면
             * 보너스 문구를 저장한다.
             *
             * 20Tick 동안 표시하며,
             * 실제 화면 출력은 render()에서 처리한다.
             */

            bonusMessage = "보너스 +10%!";
            bonusMessageTicks = 20;
        }

        /*
         * =========================
         * 노란 포인트 재생성
         * =========================
         *
         * 화면 왼쪽으로 완전히 벗어나면
         * 오른쪽에 새로운 포인트를 생성한다.
         *
         * 새로운 포인트는 다시 활성화된다.
         */

        if (yellowPointX < -YELLOW_POINT_WIDTH) {

            /*
             * 초록 포인트와
             * 최소 간격을 확보한다.
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

            yellowPointActive = true;
        }

        /* 초록 포인트 */

        previousGreenPointX =
                greenPointX;

        greenPointX -=
                POINT_SCROLL_SPEED;

        /*
         * =========================
         * 초록 포인트 충돌 판정
         * =========================
         *
         * 초록 포인트가 활성화되어 있을 때
         * 흰색 실과 겹치는지 확인한다.
         *
         * 겹쳤다면 비활성화하여
         * 화면에서 사라지게 한다.
         */

        if (greenPointActive
                && PLAYER_X_OFFSET < greenPointX + GREEN_POINT_WIDTH
                && PLAYER_X_OFFSET + THREAD_WIDTH > greenPointX
                && threadY < greenPointY + GREEN_POINT_HEIGHT
                && threadY + THREAD_HEIGHT > greenPointY) {

            /*
             * =========================
             * 초록 포인트 획득 처리
             * =========================
             *
             * 흰색 실이 초록 포인트에 닿으면
             * 포인트를 비활성화하여 제거한다.
             */
            greenPointActive = false;

            /*
             * =========================
             * 초록 포인트 보너스 적용
             * =========================
             *
             * 현재점수 +200
             * 최대점수 +200
             *
             * 최대점수는 2000을 넘지 않는다.
             *
             * 점수 계산은 FlightGame에서 처리한다.
             */
            flightGame.collectGreenPoint();

            /*
             * =========================
             * 초록 포인트 보너스 문구
             * =========================
             *
             * 초록 포인트를 획득하면
             * 보너스 문구를 저장한다.
             *
             * 20Tick 동안 표시하며,
             * 실제 화면 출력은 render()에서 처리한다.
             *
             * 다른 보너스 문구가 표시 중이라면
             * 새로운 문구로 교체하고 시간을 초기화한다.
             */

            bonusMessage = "보너스 +20%!";
            bonusMessageTicks = 20;
        }

        /*
         * =========================
         * 초록 포인트 재생성
         * =========================
         *
         * 화면 왼쪽으로 완전히 벗어나면
         * 오른쪽에 새로운 포인트를 생성한다.
         *
         * 새로운 포인트는 다시 활성화된다.
         */

        if (greenPointX < -GREEN_POINT_WIDTH) {

            /*
             * 노란 포인트와
             * 최소 간격을 확보한다.
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

            greenPointActive = true;
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

        /*
         * =========================
         * 보너스 문구 표시 시간 관리
         * =========================
         *
         * 노란색 또는 초록색 포인트를
         * 획득하면 표시 시간이 20Tick으로 설정된다.
         *
         * 매 Tick마다 남은 시간을 1씩 줄인다.
         *
         * 남은 시간이 0이 되면
         * 보너스 문구를 삭제한다.
         *
         * 실제 문구 출력은 render()에서 처리한다.
         */

        if (bonusMessageTicks > 0) {

            bonusMessageTicks--;

            if (bonusMessageTicks == 0) {

                bonusMessage = "";
            }
        }
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

        /*
         * 노란 포인트가 활성화된 경우에만
         * 화면에 표시한다.
         *
         * 획득하여 비활성화되면
         * 화면에서 사라진다.
         */

        if (yellowPointActive) {

            graphics.fill(
                    renderYellowPointX,
                    renderYellowPointY,
                    renderYellowPointX + YELLOW_POINT_WIDTH,
                    renderYellowPointY + YELLOW_POINT_HEIGHT,
                    0xFFFFFF00
            );
        }

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

        /*
         * 초록 포인트가 활성화된 경우에만
         * 화면에 표시한다.
         *
         * 획득하여 비활성화되면
         * 화면에서 사라진다.
         */

        if (greenPointActive) {

            graphics.fill(
                    renderGreenPointX,
                    renderGreenPointY,
                    renderGreenPointX + GREEN_POINT_WIDTH,
                    renderGreenPointY + GREEN_POINT_HEIGHT,
                    0xFF00FF00
            );
        }

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
                "Score : "
                        + flightGame.getCurrentScore()
                        + " / "
                        + flightGame.getMaxScore(),
                10,
                34,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 보너스 문구 화면 출력
         * =========================
         *
         * 포인트를 획득하면 저장된
         * 보너스 문구를 화면에 출력한다.
         *
         * 남은 표시 시간이 0보다 클 때만
         * 문구를 표시한다.
         *
         * 노란 포인트 = 노란색 문구
         * 초록 포인트 = 초록색 문구
         *
         * 문구는 화면 중앙 위쪽에
         * 가로 가운데 정렬하여 표시한다.
         */

        if (bonusMessageTicks > 0) {

            int messageColor =
                    bonusMessage.contains("10%")
                            ? 0xFFFFFF00
                            : 0xFF00FF00;

            int messageX =
                    (this.width - this.font.width(bonusMessage)) / 2;

            int messageY =
                    this.height / 2 - 75;

            graphics.drawString(
                    this.font,
                    bonusMessage,
                    messageX,
                    messageY,
                    messageColor
            );
        }
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