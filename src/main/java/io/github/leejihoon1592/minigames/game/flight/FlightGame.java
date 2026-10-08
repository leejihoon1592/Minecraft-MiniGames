package io.github.leejihoon1592.minigames.game.flight;

public class FlightGame {

    /*
     * =========================
     * 점수 기준
     * =========================
     *
     * 이동거리 기본 클리어 기준: 1000점
     * 최종 평가 기본 만점: 2000점
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
     * 1회: 클리어 기준 900점 / 최종 만점 1900점
     * 2회 이상: 클리어 기준 800점 / 최종 만점 1800점
     * 세 번째 MISS부터는 추가 점수 패널티가 없다.
     * MISS만으로 게임이 종료되지는 않는다.
     *
     * 클리어 기준이 이미 획득한 이동거리보다 낮아지면
     * 이동거리 점수를 새 클리어 기준에 맞춘다.
     * 보너스 점수는 변경하지 않는다.
     */

    public void applyMiss() {
        this.missCount++;

        int missPenalty = Math.min(this.missCount * 100, MAX_MISS_PENALTY);
        this.clearScore = BASE_MAX_SCORE - missPenalty;

        this.distanceScore = Math.min(this.distanceScore, this.clearScore);
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
     * 최종 최대점수 조회 (52단계 확정 규칙)
     * =========================
     *
     * 기본 만점은 2000점이며 보너스로 증가하지 않는다.
     * MISS 0회: 2000점
     * MISS 1회: 1900점
     * MISS 2회 이상: 1800점 유지
     */

    public int getMaxScore() {
        int missPenalty = Math.min(this.missCount * 100, MAX_MISS_PENALTY);
        return MAX_SCORE_LIMIT - missPenalty;
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
