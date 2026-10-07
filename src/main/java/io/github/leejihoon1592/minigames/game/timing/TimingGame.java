package io.github.leejihoon1592.minigames.game.timing;

public class TimingGame {

    private final TimingConfig config;

    private int currentRound;
    private int totalScore;

    private Judgement lastJudgement;

    public TimingGame(TimingConfig config) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "config must not be null"
            );
        }

        this.config = config;

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

        if (distance <= config.getFantasticRange()) {

            judgement = Judgement.FANTASTIC;

        } else if (distance <= config.getPerfectRange()) {

            judgement = Judgement.PERFECT;

        } else if (distance <= config.getGoodRange()) {

            judgement = Judgement.GOOD;

        } else {

            judgement = Judgement.MISS;
        }

        this.lastJudgement = judgement;

        this.totalScore +=
                getScore(judgement);

        this.currentRound++;

        return judgement;
    }

    /*
     * =========================
     * 판정별 점수
     * =========================
     */

    public int getScore(Judgement judgement) {

        return switch (judgement) {

            case FANTASTIC ->
                    config.getFantasticScore();

            case PERFECT ->
                    config.getPerfectScore();

            case GOOD ->
                    config.getGoodScore();

            case MISS ->
                    config.getMissScore();
        };
    }

    /*
     * =========================
     * 게임 상태
     * =========================
     */

    public boolean isFinished() {
        return currentRound >= config.getMaxRounds();
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getMaxRounds() {
        return config.getMaxRounds();
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getMaximumScore() {
        return config.getMaxRounds()
                * config.getFantasticScore();
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

    public TimingConfig getConfig() {
        return config;
    }

    /*
     * =========================
     * 판정 종류
     * =========================
     */

    public enum Judgement {

        FANTASTIC(
                "FANTASTIC!",
                0xFF55AAFF
        ),

        PERFECT(
                "PERFECT!",
                0xFFE0B040
        ),

        GOOD(
                "GOOD!",
                0xFF55FF55
        ),

        MISS(
                "MISS!",
                0xFFFF5555
        );

        private final String displayName;
        private final int color;

        Judgement(
                String displayName,
                int color
        ) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getColor() {
            return color;
        }
    }
}