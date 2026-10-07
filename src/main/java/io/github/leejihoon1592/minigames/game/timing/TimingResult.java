package io.github.leejihoon1592.minigames.game.timing;

public class TimingResult {

    private final int totalScore;
    private final int maximumScore;
    private final int roundsPlayed;

    private final int fantasticCount;
    private final int perfectCount;
    private final int goodCount;
    private final int missCount;

    public TimingResult(
            int totalScore,
            int maximumScore,
            int roundsPlayed,
            int fantasticCount,
            int perfectCount,
            int goodCount,
            int missCount
    ) {
        this.totalScore = totalScore;
        this.maximumScore = maximumScore;
        this.roundsPlayed = roundsPlayed;

        this.fantasticCount = fantasticCount;
        this.perfectCount = perfectCount;
        this.goodCount = goodCount;
        this.missCount = missCount;
    }

    /*
     * =========================
     * 기본 결과
     * =========================
     */

    public int getTotalScore() {
        return totalScore;
    }

    public int getMaximumScore() {
        return maximumScore;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    /*
     * =========================
     * 판정별 횟수
     * =========================
     */

    public int getFantasticCount() {
        return fantasticCount;
    }

    public int getPerfectCount() {
        return perfectCount;
    }

    public int getGoodCount() {
        return goodCount;
    }

    public int getMissCount() {
        return missCount;
    }

    /*
     * =========================
     * 계산된 결과
     * =========================
     */

    public double getAverageScore() {

        if (roundsPlayed == 0) {
            return 0.0;
        }

        return (double) totalScore / roundsPlayed;
    }

    public double getScoreRatio() {

        if (maximumScore == 0) {
            return 0.0;
        }

        return (double) totalScore / maximumScore;
    }

    public double getScorePercent() {
        return getScoreRatio() * 100.0;
    }
}