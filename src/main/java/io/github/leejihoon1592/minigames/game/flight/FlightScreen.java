package io.github.leejihoon1592.minigames.game.flight;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import static io.github.leejihoon1592.minigames.game.flight.FlightConfig.*;

public class FlightScreen extends Screen {

    /*
     * =========================
     * 화면 및 물리 설정
     * =========================
     *
     * 기존 설정값은 FlightConfig에서 관리한다.
     *
     * 이 클래스는 설정값을 직접 선언하지 않고
     * FlightConfig에 정의된 값을 사용한다.
     */

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
     * 빨간 통로 이탈 상태
     * =========================
     *
     * 흰색 실의 일부라도
     * 빨간 통로 경계를 벗어나면 true.
     *
     * 실 전체가 통로 안에 있으면 false.
     *
     * 이후 MISS 판정과
     * 점수 차감 처리에 사용한다.
     */

    private boolean threadOutsideCorridor;

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
     * 실의 이동 경로 관리
     * =========================
     *
     * FlightPath는 빨간 통로 계산과
     * 실이 지나온 위치 기록을 담당한다.
     *
     * 각 게임 화면마다 독립된
     * 이동 경로를 사용한다.
     */

    private final FlightPath flightPath = new FlightPath();

    /*
     * =========================
     * 게임 시작 카운트다운
     * =========================
     *
     * 20Tick = 1초
     * 100Tick = 5초
     *
     * 카운트다운이 끝나기 전에는
     * 게임의 모든 움직임과 점수 계산을 중지한다.
     */

    private static final int START_COUNTDOWN_TICKS = 100;

    private int countdownTicks = START_COUNTDOWN_TICKS;

    /*
     * =========================
     * 게임 클리어 상태
     * =========================
     *
     * 이동거리 점수가 클리어 기준에
     * 도달하면 true로 변경한다.
     *
     * true 상태에서는 게임 진행을
     * 정지하고 결과 화면만 표시한다.
     */

    private boolean gameFinished = false;

    /*
     * =========================
     * 결과 화면 클릭 보호
     * =========================
     *
     * 클리어 직후 20Tick(1초) 동안
     * 마우스 클릭으로 화면을 닫지 못하게 한다.
     *
     * ESC를 이용한 종료는 제한하지 않는다.
     */

    private static final int RESULT_CLICK_PROTECTION_TICKS = 20;

    private int resultClickProtectionTicks = RESULT_CLICK_PROTECTION_TICKS;

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

    /*
     * =========================
     * MISS 이후 포인트 재생성 대기시간
     * =========================
     *
     * MISS가 발생하면 화면에 있는
     * 노란색과 초록색 포인트를 제거한다.
     *
     * pointRespawnCooldownTicks:
     * 새로운 포인트가 나타나기까지
     * 남은 대기시간을 저장한다.
     *
     * MISS 발생 시 20Tick으로 설정한다.
     * 20Tick = 약 1초
     *
     * 대기시간이 0보다 크면
     * 포인트 재생성을 허용하지 않는다.
     *
     * 대기시간이 0이 되면
     * 다시 포인트를 생성할 수 있다.
     */

    private int pointRespawnCooldownTicks = 0;

    public FlightScreen() {
        super(Component.translatable("screen.minigames.flight.title"));
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

        /*
         * =========================
         * 게임 시작 시 잔상 초기화
         * =========================
         *
         * 새로운 게임을 시작할 때
         * 이전 경로 기록이 남지 않도록 한다.
         */

        this.flightPath.clearTrail();

    }

    @Override
    public void tick() {

        /*
         * =========================
         * 클리어 후 게임 진행 정지
         * =========================
         *
         * 클리어 상태에서는 물리 계산,
         * 점수 증가, 포인트 이동,
         * MISS 판정을 더 이상 실행하지 않는다.
         */

        if (gameFinished) {

            /*
             * =========================
             * 결과 화면 클릭 보호 시간 감소
             * =========================
             *
             * 게임 물리 계산과 점수 계산은
             * 계속 정지된 상태를 유지한다.
             *
             * 결과 화면 보호 시간만
             * Tick마다 감소시킨다.
             */

            if (resultClickProtectionTicks > 0) {
                resultClickProtectionTicks--;
            }

            return;
        }

        /*
         * =========================
         * 게임 시작 전 카운트다운
         * =========================
         *
         * 카운트다운 중에는 Tick마다
         * 남은 시간을 1씩 감소시킨다.
         *
         * 남은 시간이 0보다 크면
         * 나머지 게임 로직을 실행하지 않는다.
         *
         * 0이 되면 자동으로 게임을 시작한다.
         */

        if (countdownTicks > 0) {

            countdownTicks--;

            return;
        }

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

            bonusMessage = "screen.minigames.flight.bonus_yellow";
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

        /*
         * =========================
         * 노란 포인트 재생성 제한
         * =========================
         *
         * 노란 포인트가 화면 왼쪽으로
         * 완전히 벗어났는지 확인한다.
         *
         * 동시에 MISS 이후 재생성
         * 대기시간이 0인지 확인한다.
         *
         * 대기시간이 남아 있다면
         * 노란 포인트를 재생성하지 않는다.
         *
         * 대기시간이 끝나면
         * 기존 재생성 방식을 사용한다.
         */

        if (yellowPointX < -YELLOW_POINT_WIDTH
                && pointRespawnCooldownTicks == 0) {

            /*
             * =========================
             * 노란 포인트 재생성 위치
             * =========================
             *
             * 화면 오른쪽에 생성하며
             * 초록 포인트와 최소 간격을 확보한다.
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

            /*
             * 빨간 통로 내부에서
             * 새로운 Y 좌표를 결정한다.
             */

            yellowPointY =
                    getRandomPointY(
                            yellowPointX,
                            YELLOW_POINT_HEIGHT
                    );

            /*
             * 새로운 노란 포인트를
             * 활성화하여 화면에 표시한다.
             */

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

            bonusMessage = "screen.minigames.flight.bonus_green";
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

        /*
         * =========================
         * 초록 포인트 재생성 제한
         * =========================
         *
         * 초록 포인트가 화면 왼쪽으로
         * 완전히 벗어났는지 확인한다.
         *
         * MISS 이후 재생성 대기시간이
         * 0일 때만 새로운 포인트를 생성한다.
         *
         * 대기시간이 남아 있다면
         * 초록 포인트를 재생성하지 않는다.
         */

        if (greenPointX < -GREEN_POINT_WIDTH
                && pointRespawnCooldownTicks == 0) {

            /*
             * =========================
             * 초록 포인트 재생성 위치
             * =========================
             *
             * 화면 오른쪽에 생성하며
             * 노란 포인트와 최소 간격을 확보한다.
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

            /*
             * 빨간 통로 내부에서
             * 새로운 Y 좌표를 결정한다.
             */

            greenPointY =
                    getRandomPointY(
                            greenPointX,
                            GREEN_POINT_HEIGHT
                    );

            /*
             * 새로운 초록 포인트를
             * 활성화하여 화면에 표시한다.
             */

            greenPointActive = true;
        }

        /*
         * =========================
         * 빨간 통로 스크롤
         * =========================
         */

        previousCorridorScroll =
                corridorScroll;

        previousCorridorScroll =
                corridorScroll;

        corridorScroll +=
                POINT_SCROLL_SPEED;

        /*
         * =========================
         * 실의 이동 경로 기록
         * =========================
         *
         * 화면에서 실의 X 좌표는 고정이지만
         * 배경이 이동하므로 월드 좌표를 기록한다.
         *
         * 실의 중심 Y 좌표를 사용해
         * 실제 지나온 경로를 저장한다.
         *
         * 카운트다운 중에는 이 코드까지
         * 도달하지 않으므로 기록하지 않는다.
         */

        flightPath.recordTrailPoint(
                corridorScroll + PLAYER_X_OFFSET
                        + THREAD_WIDTH / 2.0,
                threadY + THREAD_HEIGHT / 2.0
        );

        /*
         * =========================
         * 빨간 통로 이탈 판정
         * =========================
         *
         * 흰색 실의 X 좌표는 고정되어 있다.
         *
         * 따라서 현재 실이 위치한 X 좌표에서
         * 빨간 통로의 중심 Y 좌표를 계산한다.
         *
         * 통로가 움직이기 때문에
         * 현재 스크롤 위치를 함께 사용한다.
         */

        double corridorCenterAtThread =
                getCorridorCenterY(
                        corridorScroll + PLAYER_X_OFFSET
                );

        /*
         * 현재 실 위치에서의
         * 빨간 통로 위쪽과 아래쪽 경계
         */

        double corridorTop =
                corridorCenterAtThread
                        - CORRIDOR_HALF_HEIGHT;

        double corridorBottom =
                corridorCenterAtThread
                        + CORRIDOR_HALF_HEIGHT;

        /*
         * =========================
         * 실의 통로 이탈 여부 확인
         * =========================
         *
         * 실의 위쪽이 통로 위쪽을 벗어나거나
         * 실의 아래쪽이 통로 아래쪽을 벗어나면
         * 이탈 상태를 true로 설정한다.
         *
         * 실 전체가 통로 내부에 있으면
         * false로 설정한다.
         *
         * 현재는 이탈 상태만 기록하며
         * MISS 패널티는 적용하지 않는다.
         */

        /*
         * =========================
         * 이전 이탈 상태 저장
         * =========================
         *
         * 현재 판정을 갱신하기 전에
         * 직전 Tick의 이탈 상태를 저장한다.
         *
         * 이를 통해 통로 밖으로 나가는
         * 순간을 한 번만 감지할 수 있다.
         */

        boolean wasOutsideCorridor =
                threadOutsideCorridor;

        /*
         * =========================
         * 현재 이탈 상태 갱신
         * =========================
         *
         * 실의 일부라도 빨간 통로를
         * 벗어나면 true로 설정한다.
         */

        threadOutsideCorridor =
                threadY < corridorTop
                        || threadY + THREAD_HEIGHT > corridorBottom;

        /*
         * =========================
         * 실제 이동거리 누적
         * =========================
         *
         * 게임이 진행되는 동안
         * 매 Tick마다 실제 이동거리를 1씩 증가시킨다.
         *
         * MISS 상태에서도 이동거리는 증가한다.
         *
         * 실제 이동거리는 게임 클리어 조건에
         * 사용되며 점수와 별도로 관리한다.
         *
         * MISS 0회: 목표 이동거리 1000
         * MISS 1회: 목표 이동거리 900
         * MISS 2회 이상: 목표 이동거리 800
         */

        flightGame.addTraveledDistance(1);

        /*
         * =========================
         * 정상 비행 점수 누적
         * =========================
         *
         * 흰색 실 전체가 빨간 통로 안에 있을 때만
         * 매 Tick마다 정상 비행 점수를 1점 획득한다.
         *
         * 통로 밖으로 벗어난 동안에는
         * 실제 이동거리만 증가하고
         * 정상 비행 점수는 증가하지 않는다.
         *
         * 노란색과 초록색 보너스 점수는
         * FlightGame에서 별도로 관리한다.
         */

        if (!threadOutsideCorridor) {

            flightGame.addProgressScore(1);
        }

        /*
         * =========================
         * MISS 발생 및 점수 차감
         * =========================
         *
         * 직전 Tick에는 통로 안에 있었고
         * 현재 Tick에는 통로 밖에 있다면
         * 새로운 MISS로 판정한다.
         *
         * MISS 1회당 최대점수 -100
         * MISS 횟수 +1
         *
         * 통로 밖에 계속 머무르는 동안에는
         * 중복으로 MISS를 적용하지 않는다.
         *
         * 다시 통로 내부로 복귀한 뒤
         * 재차 벗어나면 새로운 MISS가 발생한다.
         */

        if (threadOutsideCorridor
                && !wasOutsideCorridor) {

            /*
             * =========================
             * MISS 점수 패널티
             * =========================
             *
             * MISS 횟수를 1 증가시키고
             * 최대점수를 100 감소시킨다.
             *
             * 현재점수가 최대점수보다 높으면
             * 현재점수도 최대점수에 맞춘다.
             */

            flightGame.applyMiss();

            /*
             * =========================
             * MISS 발생 시 포인트 제거
             * =========================
             *
             * MISS가 발생하면 화면의
             * 노란색과 초록색 포인트를
             * 모두 비활성화한다.
             *
             * 비활성화된 포인트는
             * 화면에 표시되지 않으며
             * 획득 판정도 발생하지 않는다.
             */

            /*
             * =========================
             * 노랑·초록 포인트 비활성화
             * =========================
             *
             * MISS 발생 순간 두 포인트를
             * 화면에서 제거한다.
             *
             * 비활성화된 포인트는
             * 획득 판정에서도 제외된다.
             */

            yellowPointActive = false;
            greenPointActive = false;

            /*
             * =========================
             * 포인트 재생성 대기시간 설정
             * =========================
             *
             * MISS가 발생하면 20Tick 동안
             * 새로운 포인트 생성을 금지한다.
             *
             * 이 시간은 이후 Tick 처리에서
             * 매번 1씩 감소시킬 예정이다.
             */

            pointRespawnCooldownTicks = 20;
        }

        /*
         * =========================
         * MISS 이후 포인트 재생성 대기시간 감소
         * =========================
         *
         * MISS 발생 시 설정된 20Tick을
         * 매 Tick마다 1씩 감소시킨다.
         *
         * 대기시간이 0이 되면
         * 더 이상 감소시키지 않는다.
         *
         * 실제 포인트 재생성 제한은
         * 이후 재생성 처리에서 연결한다.
         */

        if (pointRespawnCooldownTicks > 0) {

            pointRespawnCooldownTicks--;
        }

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

        /*
         * =========================
         * 실제 이동거리 클리어 판정
         * =========================
         *
         * 정상 비행 점수가 아닌
         * 실제 이동거리를 기준으로 클리어한다.
         *
         * MISS 0회: 이동거리 1000
         * MISS 1회: 이동거리 900
         * MISS 2회 이상: 이동거리 800
         *
         * MISS 상태에서 이동한 거리도
         * 클리어 목표에 포함한다.
         *
         * 정상 비행 점수와 보너스 점수는
         * 게임 종료 시 그대로 유지한다.
         *
         * 목표 이동거리에 도달하면
         * 게임을 종료 상태로 변경한다.
         *
         * 다음 Tick부터는 게임 진행이 정지된다.
         */

        if (flightGame.getTraveledDistance()
                >= flightGame.getClearScore()) {

            gameFinished = true;
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
         * 마인크래프트 월드 배경 표시
         * =========================
         *
         * 기존의 불투명한 단색 배경을 제거한다.
         *
         * 마인크래프트의 기본 화면 배경 처리를
         * 사용하여 월드가 뒤에 보이도록 한다.
         *
         * 미로게임 및 타이밍게임과
         * 동일한 배경 처리 방식을 사용한다.
         *
         * 실제 월드 게임 진행은 일시정지하지 않는다.
         */

        this.renderBackground(graphics);

        /*
         * =========================
         * 헬기게임 결과 화면
         * =========================
         *
         * 클리어 상태에서는
         * 기존 게임 요소를 그리지 않고
         * 최종 점수만 표시한다.
         */

        if (gameFinished) {

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            /*
             * =========================
             * 게임 클리어 제목
             * =========================
             *
             * 마인크래프트 언어 설정에 따라
             * 한국어 또는 영어로 표시한다.
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.clear"
                    ),
                    centerX,
                    centerY - 60,
                    0xFFFFD700
            );

            /*
             * =========================
             * 실제 이동거리 출력
             * =========================
             *
             * MISS 상태에서 이동한 거리까지
             * 포함한 실제 이동거리를 표시한다.
             *
             * 클리어 기준도 함께 표시한다.
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.distance",
                            flightGame.getTraveledDistance(),
                            flightGame.getClearScore()
                    ),
                    centerX,
                    centerY - 30,
                    0xFFFFFFFF
            );

            /*
             * =========================
             * 정상 비행 획득 점수 출력
             * =========================
             *
             * 통로 내부에서 비행하여
             * 획득한 점수만 표시한다.
             *
             * 보너스 점수는 포함하지 않는다.
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.flight_score",
                            flightGame.getDistanceScore()
                    ),
                    centerX,
                    centerY - 45,
                    0xFFFFFFFF
            );

            /*
             * =========================
             * 보너스 획득 점수 출력
             * =========================
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.bonus",
                            flightGame.getBonusScore()
                    ),
                    centerX,
                    centerY - 15,
                    0xFF55FF55
            );

            /*
             * =========================
             * MISS 발생 횟수 출력
             * =========================
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.miss",
                            flightGame.getMissCount()
                    ),
                    centerX,
                    centerY,
                    0xFFFF5555
            );

            /*
             * =========================
             * 최종 점수 및 최대점수 출력
             * =========================
             *
             * 기존 점수 계산값을 그대로 사용한다.
             * 표시 문구만 번역 파일에서 가져온다.
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.final_score",
                            flightGame.getCurrentScore(),
                            flightGame.getMaxScore()
                    ),
                    centerX,
                    centerY + 30,
                    0xFFFFFF55
            );

            return;
        }

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
         * 실의 이동 잔상 표시
         * =========================
         *
         * 빨간 통로 위에 실이 지나온
         * 이동 경로를 표시한다.
         *
         * 현재 실보다 먼저 그리므로
         * 흰색 실은 항상 잔상 위에 표시된다.
         */

        drawThreadTrail(
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

        /*
         * =========================
         * 실의 현재 Y 좌표
         * =========================
         *
         * 개발 및 게임 테스트를 위한 좌표다.
         * 숫자는 기존과 같이 소수점 첫째 자리까지 표시한다.
         */

        graphics.drawString(
                this.font,
                Component.translatable(
                        "screen.minigames.flight.y_position",
                        String.format(
                                java.util.Locale.ROOT,
                                "%.1f",
                                threadY
                        )
                ),
                10,
                10,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 현재 점수 표시
         * =========================
         *
         * 현재 획득 점수와 최대점수를
         * 마인크래프트 언어 설정에 맞춰 표시한다.
         */

        graphics.drawString(
                this.font,
                Component.translatable(
                        "screen.minigames.flight.score",
                        flightGame.getCurrentScore(),
                        flightGame.getMaxScore()
                ),
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

        /*
         * =========================
         * 보너스 문구 화면 출력
         * =========================
         *
         * 포인트 획득 시 저장된 문구를
         * 화면 중앙 위쪽에 출력한다.
         */

        if (bonusMessageTicks > 0) {

            /*
             * =========================
             * 보너스 번역 문구 생성
             * =========================
             *
             * 저장된 언어 키를 이용해
             * 현재 마인크래프트 언어 설정에 맞는
             * 문구를 생성한다.
             */

            Component translatedBonusMessage =
                    Component.translatable(bonusMessage);

            /*
             * =========================
             * 보너스별 출력 색상 지정
             * =========================
             *
             * 노란색 포인트: 노란색 문구
             * 초록색 포인트: 초록색 문구
             *
             * 화면에 출력되는 실제 문구가 아닌
             * 번역 키로 색상을 구분한다.
             */

            int messageColor =
                    bonusMessage.equals(
                            "screen.minigames.flight.bonus_yellow"
                    )
                            ? 0xFFFFFF00
                            : 0xFF00FF00;

            /*
             * =========================
             * 번역 문구 중앙 정렬
             * =========================
             *
             * 한국어와 영어는 글자 폭이 다르므로
             * 실제 번역된 문구의 길이를 사용한다.
             */

            int messageX =
                    (this.width
                            - this.font.width(translatedBonusMessage)) / 2;

            int messageY =
                    this.height / 2 - 75;

            graphics.drawString(
                    this.font,
                    translatedBonusMessage,
                    messageX,
                    messageY,
                    messageColor
            );
        }

        /*
         * =========================
         * 시작 전 게임판 미리보기
         * =========================
         *
         * 게임 시작 전 5초 동안
         * 빨간 통로, 실, 보너스 포인트를
         * 모두 화면에 표시한다.
         *
         * 실제 움직임과 점수 계산은
         * tick()의 카운트다운 처리에서
         * 계속 정지된 상태로 유지한다.
         *
         * 화면 상단 중앙에 남은 시간을
         * 출력하여 게임 시작을 안내한다.
         */

        if (countdownTicks > 0) {

            int countdownSeconds =
                    (countdownTicks + 19) / 20;

            /*
             * =========================
             * 카운트다운 시작 안내 문구
             * =========================
             *
             * 남은 시간 숫자는 기존대로 유지한다.
             * 시작 안내 문구만 언어 파일에서 가져온다.
             */

            graphics.drawCenteredString(
                    this.font,
                    Component.translatable(
                            "screen.minigames.flight.start_in"
                    ),
                    this.width / 2,
                    40,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    String.valueOf(countdownSeconds),
                    this.width / 2,
                    60,
                    0xFFFFFF55
            );
        }
    }

    /*
     * =========================
     * 빨간 통로 렌더링
     * =========================
     */

    /*
     * =========================
     * 실의 이동 잔상 렌더링
     * =========================
     *
     * FlightPath에 기록된 월드 좌표를
     * 현재 스크롤 위치에 맞춰 화면에 출력한다.
     *
     * 실이 고정된 X 좌표에서 움직이더라도
     * 지나온 경로는 왼쪽으로 흘러간다.
     *
     * 점 사이의 공간을 보간하여
     * 스크롤 속도가 빨라도 잔상이
     * 끊어지지 않도록 그린다.
     */

    private void drawThreadTrail(
            GuiGraphics graphics,
            float partialTick
    ) {

        /*
         * =========================
         * 현재 스크롤 위치 보간
         * =========================
         */

        double renderedScroll =
                Mth.lerp(
                        partialTick,
                        previousCorridorScroll,
                        corridorScroll
                );

        /*
         * =========================
         * 이전 잔상 좌표 관리
         * =========================
         */

        FlightPath.TrailPoint previousPoint = null;

        /*
         * =========================
         * 저장된 이동 경로 순회
         * =========================
         */

        for (FlightPath.TrailPoint point
                : flightPath.getTrailPoints()) {

            /*
             * 현재 기록의 화면 좌표 계산
             */

            double currentX =
                    point.getWorldX() - renderedScroll;

            double currentY =
                    point.getY();

            /*
             * 첫 번째 기록은 연결할
             * 이전 좌표가 없으므로 건너뛴다.
             */

            if (previousPoint == null) {

                previousPoint = point;
                continue;
            }

            /*
             * 이전 기록의 화면 좌표 계산
             */

            double previousX =
                    previousPoint.getWorldX() - renderedScroll;

            double previousY =
                    previousPoint.getY();

            /*
             * 두 기록 사이의 이동 거리
             */

            double differenceX =
                    currentX - previousX;

            double differenceY =
                    currentY - previousY;

            /*
             * =========================
             * 좌표 사이 보간
             * =========================
             *
             * X 또는 Y 이동량 중
             * 더 큰 값을 기준으로
             * 1픽셀 간격의 점을 생성한다.
             *
             * 빠른 이동 중에도
             * 경로가 끊어지지 않게 한다.
             */

            int steps =
                    Math.max(
                            1,
                            (int) Math.ceil(
                                    Math.max(
                                            Math.abs(differenceX),
                                            Math.abs(differenceY)
                                    )
                            )
                    );

            for (int step = 0; step <= steps; step++) {

                double progress =
                        (double) step / steps;

                double x =
                        previousX
                                + differenceX * progress;

                double y =
                        previousY
                                + differenceY * progress;

                int renderX =
                        (int) Math.round(x);

                int renderY =
                        (int) Math.round(y);

                /*
                 * 화면 밖에 있는 기록은
                 * 실제 렌더링을 생략한다.
                 */

                if (renderX < 0
                        || renderX >= this.width
                        || renderY < 0
                        || renderY >= this.height) {
                    continue;
                }

                /*
                 * =========================
                 * 잔상 픽셀 출력
                 * =========================
                 *
                 * 현재 실과 구분되도록
                 * 옅은 회백색을 사용한다.
                 *
                 * 잔상 두께는 2픽셀이다.
                 */

                graphics.fill(
                        renderX,
                        renderY,
                        Math.min(renderX + 4, this.width),
                        Math.min(renderY + 4, this.height),
                        0xFFAAAAAA
                );
            }

            /*
             * 다음 연결 구간을 위해
             * 현재 좌표를 저장한다.
             */

            previousPoint = point;
        }
    }

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
     * 빨간 통로 중심선 조회
     * =========================
     *
     * 실제 통로 패턴 계산은
     * FlightPath에서 담당한다.
     *
     * FlightScreen은 현재 화면 높이와
     * 월드 X 좌표만 전달한다.
     *
     * 기존 호출부는 그대로 유지하여
     * 통로 출력 및 충돌 판정에
     * 영향을 주지 않도록 한다.
     */

    private double getCorridorCenterY(
            double worldX
    ) {

        return FlightPath.getCorridorCenterY(
                worldX,
                this.height
        );
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
    /*
     * =========================
     * 게임 종료 후 마우스 클릭 처리
     * =========================
     *
     * 헬기게임이 진행 중일 때는
     * 기존 마우스 조작을 유지한다.
     *
     * 게임이 클리어되어 결과 화면이
     * 표시되고 있을 때만
     * 클릭 한 번으로 화면을 닫는다.
     *
     * 좌클릭과 우클릭 모두 허용한다.
     *
     * 화면을 닫으면 기존에 플레이하던
     * 마인크래프트 월드로 돌아간다.
     *
     * 점수 및 게임 진행 상태는
     * 이곳에서 변경하지 않는다.
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        /*
         * =========================
         * 결과 화면 클릭 종료
         * =========================
         *
         * gameFinished가 true이면
         * 이미 게임이 클리어된 상태이다.
         *
         * 마우스 버튼을 누르면
         * 현재 화면을 닫고 월드로 복귀한다.
         */

        if (gameFinished) {

            /*
             * =========================
             * 결과 화면 클릭 보호
             * =========================
             *
             * 클리어 후 1초가 지나지 않았다면
             * 마우스 클릭을 소비하고
             * 결과 화면을 그대로 유지한다.
             */

            if (resultClickProtectionTicks > 0) {
                return true;
            }

            /*
             * =========================
             * 결과 화면 종료
             * =========================
             *
             * 보호 시간이 끝난 이후에는
             * 마우스를 클릭하면
             * 마인크래프트 월드로 복귀한다.
             */

            this.minecraft.setScreen(null);

            return true;
        }

        /*
         * =========================
         * 기존 클릭 처리 유지
         * =========================
         *
         * 게임 진행 중에는
         * 기존 Screen의 클릭 처리를 사용한다.
         *
         * 헬기 조작에 사용되는
         * 마우스 버튼 상태 판정은
         * tick()에서 계속 처리한다.
         */

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}