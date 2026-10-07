package io.github.leejihoon1592.minigames.game.timing;

public class TimingGame {

    private final int maxRounds;

    private int currentRound;
    private int totalScore;

    private Judgement lastJudgement;

    public TimingGame(int maxRounds) {

        if (maxRounds <= 0) {
            throw new IllegalArgumentException(
                    "maxRounds must be greater than 0"
            );
        }

        this.maxRounds = maxRounds;
        this.currentRound = 0;
        this.totalScore = 0;
        this.lastJudgement = null;
    }

    /*
     * =========================
     * 판정 처리
     * =========================
     */

    public Judgement submit(double distanceFromCenter) {

        if (isFinished()) {
            return lastJudgement;
        }

        double distance =
                Math.abs(distanceFromCenter);

        Judgement judgement;

        if (distance <= 0.04) {

            judgement = Judgement.FANTASTIC;

        } else if (distance <= 0.1167) {

            judgement = Judgement.PERFECT;

        } else if (distance <= 0.20) {

            judgement = Judgement.GOOD;

        } else {

            judgement = Judgement.MISS;
        }

        this.lastJudgement = judgement;
        this.totalScore += judgement.getScore();
        this.currentRound++;

        return judgement;
    }

    /*
     * =========================
     * 게임 상태
     * =========================
     */

    public boolean isFinished() {
        return currentRound >= maxRounds;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getMaximumScore() {
        return maxRounds * Judgement.FANTASTIC.getScore();
    }

    public double getAverageScore() {

        if (currentRound == 0) {
            return 0.0;
        }

        return (double) totalScore / currentRound;
    }

    public Judgement getLastJudgement() {
        return lastJudgement;
    }

    /*
     * =========================
     * 판정 종류
     * =========================
     */

    public enum Judgement {

        FANTASTIC(
                "FANTASTIC!",
                100,
                0xFF55AAFF
        ),

        PERFECT(
                "PERFECT!",
                80,
                0xFFE0B040
        ),

        GOOD(
                "GOOD!",
                50,
                0xFF55FF55
        ),

        MISS(
                "MISS!",
                0,
                0xFFFF5555
        );

        private final String displayName;
        private final int score;
        private final int color;

        Judgement(
                String displayName,
                int score,
                int color
        ) {
            this.displayName = displayName;
            this.score = score;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getScore() {
            return score;
        }

        public int getColor() {
            return color;
        }
    }
}