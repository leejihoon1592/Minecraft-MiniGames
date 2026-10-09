package io.github.leejihoon1592.minigames.game.flight;

import java.util.ArrayList;
import java.util.List;


/*
 * =========================
 * 헬기게임 경로 관리
 * =========================
 *
 * 빨간 통로의 중심 좌표를 계산한다.
 *
 * 화면에 통로를 그리거나
 * 실이 통로를 벗어났는지 판정할 때
 * 동일한 계산 결과를 사용한다.
 *
 * 기존 FlightScreen의 통로 계산식을
 * 변경 없이 옮겼다.
 */

public final class FlightPath {

    /*
     * =========================
     * 통로 패턴 설정
     * =========================
     *
     * 하나의 패턴은 1080 길이로 반복된다.
     */

    private static final double PATTERN_LENGTH = 1080.0;

    private FlightPath() {
    }

    /*
     * =========================
     * 빨간 통로 중심선 계산
     * =========================
     *
     * worldX:
     * 스크롤 이동까지 반영된 X 좌표
     *
     * screenHeight:
     * 현재 미니게임 화면의 높이
     *
     * 반환값:
     * 해당 X 좌표에서 빨간 통로의 중심 Y 좌표
     */

    public static double getCorridorCenterY(
            double worldX,
            int screenHeight
    ) {

        /*
         * 반복되는 통로 패턴 안에서
         * 현재 위치를 계산한다.
         */

        double localX =
                worldX % PATTERN_LENGTH;

        if (localX < 0.0) {
            localX += PATTERN_LENGTH;
        }

        double center =
                screenHeight / 2.0;

        /*
         * 1구간
         * 중앙 직선
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
                    (localX - 240.0) / 180.0;

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
                    (localX - 660.0) / 180.0;

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
     * 실의 이동 경로 기록
     * =========================
     *
     * 실은 화면에서 X 좌표가 고정되어 있지만
     * 게임판은 계속 왼쪽으로 이동한다.
     *
     * 따라서 화면 좌표가 아닌
     * 스크롤이 반영된 월드 X 좌표와
     * 실의 Y 좌표를 함께 기록한다.
     *
     * 기록된 경로는 화면 밖으로 벗어날 때까지
     * 남아 있도록 관리한다.
     */

    private final List<TrailPoint> trailPoints =
            new ArrayList<>();

    /*
     * =========================
     * 잔상 좌표 자료형
     * =========================
     *
     * worldX: 실이 지나간 월드 X 좌표
     * y: 당시 실의 중앙 Y 좌표
     */

    public static final class TrailPoint {

        private final double worldX;
        private final double y;

        private TrailPoint(
                double worldX,
                double y
        ) {
            this.worldX = worldX;
            this.y = y;
        }

        public double getWorldX() {
            return worldX;
        }

        public double getY() {
            return y;
        }
    }

    /*
     * =========================
     * 실의 현재 이동 경로 기록
     * =========================
     *
     * 게임이 실제로 진행되는 Tick마다
     * FlightScreen에서 호출한다.
     *
     * 카운트다운 중에는 호출하지 않는다.
     */

    public void recordTrailPoint(
            double worldX,
            double y
    ) {

        trailPoints.add(
                new TrailPoint(
                        worldX,
                        y
                )
        );

        /*
         * =========================
         * 오래된 잔상 기록 정리
         * =========================
         *
         * 실이 왼쪽으로 지나간 기록 중
         * 화면에 표시할 필요가 없는
         * 오래된 기록을 제거한다.
         *
         * 최대 256개의 좌표를 유지해
         * 게임을 오래 진행하더라도
         * 기록이 무제한 증가하지 않게 한다.
         */

        while (trailPoints.size() > 256) {
            trailPoints.remove(0);
        }
    }

    /*
     * =========================
     * 잔상 좌표 목록 조회
     * =========================
     *
     * FlightScreen에서 잔상을 그릴 때
     * 기록된 좌표를 읽을 수 있도록 한다.
     *
     * 실제 좌표 수정은 이 클래스에서만 담당한다.
     */

    public List<TrailPoint> getTrailPoints() {
        return java.util.Collections.unmodifiableList(trailPoints);
    }

    /*
     * =========================
     * 실의 이동 경로 초기화
     * =========================
     *
     * 새로운 미니게임을 시작하거나
     * 경로를 초기화해야 할 경우 사용한다.
     */

    public void clearTrail() {
        trailPoints.clear();
    }
}