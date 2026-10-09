package io.github.leejihoon1592.minigames.game.flight;

public class FlightGame {

    /*
     * =========================
     * 점수 기준
     * =========================
     *
     * 이동거리 기본 클리어 기준: 1000점
     * 게임 시작 최대점수: 1000점
     * 보너스로 증가하는 최대점수 상한: 2000점
     * MISS 패널티: 1회당 100점, 최대 200점
     */

    private static final int BASE_MAX_SCORE = 1000;
    private static final int MAX_SCORE_LIMIT = 2000;
    private static final int MAX_MISS_PENALTY = 200;

    /*
     * =========================
     * 점수 및 MISS 기록
     * =========================
     *
     * distanceScore: 정상 비행으로 획득한 이동거리 점수
     * bonusScore: 노란색·초록색 포인트로 획득한 보너스 점수
     * clearScore: 클리어에 필요한 이동거리 점수
     * missCount: 실제 MISS 발생 횟수 (횟수 제한 없음)
     */

    /*
     * =========================
     * 실제 이동거리
     * =========================
     *
     * traveledDistance:
     * 게임 시작부터 실제로 이동한 거리.
     *
     * MISS 상태에서도 이동거리는 증가한다.
     * 정상 비행 점수와 별도로 기록한다.
     *
     * 이 거리가 클리어 기준에 도달하면
     * 게임을 종료할 예정이다.
     */

    private int traveledDistance;

    /*
     * =========================
     * 점수 관리
     * =========================
     *
     * distanceScore: 정상 비행 획득 점수
     * bonusScore: 오브젝트 보너스 점수
     * clearScore: 클리어 목표 이동거리
     * missCount: 실제 MISS 횟수
     */

    private int distanceScore;
    private int bonusScore;
    private int clearScore;
    private int missCount;

    /*
     * =========================
     * 게임 시작 시 초기화
     * =========================
     *
     * 이동거리·보너스·MISS는 0부터 시작한다.
     * 클리어 기준은 1000점에서 시작한다.
     */

    public FlightGame() {

            /*
             * 실제 이동거리 초기화
             */
            this.traveledDistance = 0;

            /*
             * 기존 점수 초기화
             */
            this.distanceScore = 0;
            this.bonusScore = 0;
            this.clearScore = BASE_MAX_SCORE;
            this.missCount = 0;

    }

    /*
     * =========================
     * 노란색 포인트 획득
     * =========================
     *
     * 보너스 점수만 100점 증가한다.
     * 이동거리 점수와 클리어 기준은 변경하지 않는다.
     */

    public void collectYellowPoint() {
        this.bonusScore += 100;
    }

    /*
     * =========================
     * 초록색 포인트 획득
     * =========================
     *
     * 보너스 점수만 200점 증가한다.
     * 이동거리 점수와 클리어 기준은 변경하지 않는다.
     */

    public void collectGreenPoint() {
        this.bonusScore += 200;
    }

    /*
     * =========================
     * MISS 발생 처리
     * =========================
     *
     * MISS는 횟수 제한 없이 기록한다.
     *
     * MISS 0회:
     * 목표 이동거리 1000
     *
     * MISS 1회:
     * 목표 이동거리 900
     *
     * MISS 2회 이상:
     * 목표 이동거리 800
     *
     * MISS가 발생해도 실제 이동거리와
     * 이미 획득한 점수는 감소하지 않는다.
     *
     * MISS 상태에서 정상 비행 점수를
     * 획득하지 못하는 처리는
     * FlightScreen에서 담당한다.
     *
     * MISS 발생 시 변경되는 것은
     * 클리어 목표 이동거리와
     * 최종 최대점수 계산에 사용되는
     * MISS 횟수뿐이다.
     *
     * 보너스 점수 역시 감소하지 않는다.
     */

    public void applyMiss() {

        /*
         * =========================
         * MISS 횟수 증가
         * =========================
         */

        this.missCount++;

        /*
         * =========================
         * MISS 패널티 계산
         * =========================
         *
         * 1회당 100점의 패널티를 적용한다.
         * 최대 패널티는 200점이다.
         *
         * MISS가 3회 이상 발생하더라도
         * 추가 패널티는 적용하지 않는다.
         */

        int missPenalty = Math.min(
                this.missCount * 100,
                MAX_MISS_PENALTY
        );

        /*
         * =========================
         * 클리어 목표 이동거리 변경
         * =========================
         *
         * MISS 0회: 1000
         * MISS 1회: 900
         * MISS 2회 이상: 800
         *
         * 이미 이동한 거리는 유지한다.
         * 기존 정상 비행 점수도 유지한다.
         */

        this.clearScore =
                BASE_MAX_SCORE - missPenalty;
    }

    /*
     * =========================
     * 현재점수 조회
     * =========================
     *
     * 이동거리 점수와 보너스 점수를 합산한다.
     * 최종 최대점수를 초과하면 최대점수까지만 표시한다.
     */

    public int getCurrentScore() {
        return Math.min(this.distanceScore + this.bonusScore, this.getMaxScore());
    }

    /*
     * =========================
     * 게임 진행 중 최대점수 조회
     * =========================
     *
     * 시작 최대점수는 1000점이다.
     * 노란색 포인트 +100점, 초록색 포인트 +200점으로 증가한다.
     * 보너스로 증가한 최대점수는 2000점을 넘지 않는다.
     * MISS 패널티는 1회당 -100점, 최대 -200점이다.
     * 예: 시작 1000 → 노랑 획득 1100 → MISS 1000
     * 예: 보너스로 2000 도달 → MISS 2회 1800
     */

    public int getMaxScore() {
        int missPenalty = Math.min(this.missCount * 100, MAX_MISS_PENALTY);
        int bonusLimit = Math.min(this.bonusScore, MAX_SCORE_LIMIT - BASE_MAX_SCORE);
        return BASE_MAX_SCORE + bonusLimit - missPenalty;
    }

    /*
     * =========================
     * 개별 점수 조회
     * =========================
     *
     * 화면 표시 및 향후 클리어 판정에서 사용한다.
     */

    public int getMissCount() {
        return this.missCount;
    }

    public int getDistanceScore() {
        return this.distanceScore;
    }

    /*
     * =========================
     * 실제 이동거리 조회
     * =========================
     *
     * MISS 여부와 관계없이
     * 누적된 실제 이동거리를 반환한다.
     */

    public int getTraveledDistance() {
        return this.traveledDistance;
    }

    /*
     * =========================
     * 실제 이동거리 증가
     * =========================
     *
     * FlightScreen에서 매 Tick마다
     * 이동한 거리만큼 추가한다.
     *
     * 이동거리는 클리어 기준을
     * 초과하지 않는다.
     *
     * 정상 비행 점수와 보너스 점수는
     * 여기서 변경하지 않는다.
     */

    public void addTraveledDistance(int amount) {

        if (amount <= 0) {
            return;
        }

        this.traveledDistance = Math.min(
                this.traveledDistance + amount,
                this.clearScore
        );
    }

    public int getBonusScore() {
        return this.bonusScore;
    }

    public int getClearScore() {
        return this.clearScore;
    }

    /*
     * =========================
     * 정상 비행 점수 추가
     * =========================
     *
     * FlightScreen에서 통로 안에 있을 때
     * 1Tick당 1점씩 추가한다.
     * 이동거리 점수는 현재 클리어 기준을 넘지 않는다.
     */

    public void addProgressScore(int amount) {
        this.distanceScore = Math.min(this.distanceScore + amount, this.clearScore);
    }
}
