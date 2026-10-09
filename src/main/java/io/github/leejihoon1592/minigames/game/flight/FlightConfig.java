package io.github.leejihoon1592.minigames.game.flight;

/*
 * =========================
 * 헬기게임 설정값 관리
 * =========================
 *
 * 화면 위치, 오브젝트 크기,
 * 스크롤 속도 및 물리값을 관리한다.
 *
 * 실제 게임 진행과 점수 계산은
 * 이 클래스에서 처리하지 않는다.
 *
 * 기존 FlightScreen의 설정값을
 * 변경 없이 분리한 것이다.
 */

public final class FlightConfig {

    /*
     * 설정 전용 클래스이므로
     * 객체를 생성하지 않는다.
     */

    private FlightConfig() {
    }

    /*
     * =========================
     * 화면 및 실 설정
     * =========================
     */

    public static final int PLAYER_X_OFFSET = 120;

    public static final int THREAD_WIDTH = 6;
    public static final int THREAD_HEIGHT = 6;

    /*
     * =========================
     * 비행 영역 설정
     * =========================
     */

    public static final int FLIGHT_AREA_HEIGHT = 210;

    /*
     * =========================
     * 빨간 통로 설정
     * =========================
     */

    public static final int CORRIDOR_HALF_HEIGHT = 38;
    public static final int CORRIDOR_LINE_THICKNESS = 3;

    /*
     * =========================
     * 보너스 포인트 크기
     * =========================
     *
     * 직접 조정한 값을 유지한다.
     */

    public static final int YELLOW_POINT_WIDTH = 4;
    public static final int YELLOW_POINT_HEIGHT = 14;

    public static final int GREEN_POINT_WIDTH = 9;
    public static final int GREEN_POINT_HEIGHT = 9;

    /*
     * =========================
     * 스크롤 및 포인트 간격
     * =========================
     */

    public static final double POINT_SCROLL_SPEED = 5.0;

    public static final double MIN_POINT_GAP = 240.0;

    /*
     * =========================
     * 실의 물리 설정
     * =========================
     */

    public static final double GRAVITY = 0.18;
    public static final double HOLD_FORCE = 0.35;

    public static final double MAX_FALL_SPEED = 4.0;
    public static final double MAX_RISE_SPEED = -4.0;
}