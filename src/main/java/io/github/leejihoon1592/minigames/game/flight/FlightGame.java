package io.github.leejihoon1592.minigames.game.flight;

public class FlightGame {

    /*
     * =========================
     * 점수 기준
     * =========================
     */

    private static final int BASE_MAX_SCORE = 1000;
    private static final int MAX_SCORE_LIMIT = 2000;

    /*
     * =========================
     * 점수
     * =========================
     */

    private int currentScore;
    private int maxScore;
    private int missCount;

    public FlightGame() {

        this.currentScore = 0;
        this.maxScore = BASE_MAX_SCORE;
        this.missCount = 0;
    }

    /*
     * =========================
     * 노란 포인트 보너스
     * =========================
     */

    public void collectYellowPoint() {

        this.currentScore += 100;

        this.maxScore =
                Math.min(
                        this.maxScore + 100,
                        MAX_SCORE_LIMIT
                );

        if (this.currentScore > this.maxScore) {

            this.currentScore =
                    this.maxScore;
        }
    }

    /*
     * =========================
     * 초록 포인트 보너스
     * =========================
     */

    public void collectGreenPoint() {

        this.currentScore += 200;

        this.maxScore =
                Math.min(
                        this.maxScore + 200,
                        MAX_SCORE_LIMIT
                );

        if (this.currentScore > this.maxScore) {

            this.currentScore =
                    this.maxScore;
        }
    }

    /*
     * =========================
     * MISS 패널티
     * =========================
     */

    public void applyMiss() {

        this.missCount++;

        this.maxScore -= 100;

        if (this.maxScore < 0) {

            this.maxScore = 0;
        }

        if (this.currentScore > this.maxScore) {

            this.currentScore =
                    this.maxScore;
        }
    }

    /*
     * =========================
     * 점수 조회
     * =========================
     */

    public int getCurrentScore() {

        return this.currentScore;
    }

    public int getMaxScore() {

        return this.maxScore;
    }

    public int getMissCount() {

        return this.missCount;
    }

    /*
     * =========================
     * 기본 진행 점수
     * =========================
     */

    public void addProgressScore(int amount) {

        this.currentScore += amount;

        if (this.currentScore > this.maxScore) {

            this.currentScore =
                    this.maxScore;
        }
    }
}