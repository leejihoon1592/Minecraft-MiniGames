package io.github.leejihoon1592.minigames.game.timing;

public class TimingConfig {

    private final int maxRounds;

    private final double travelTimeSeconds;

    private final double fantasticRange;
    private final double perfectRange;
    private final double goodRange;

    private final int fantasticScore;
    private final int perfectScore;
    private final int goodScore;
    private final int missScore;

    public TimingConfig(
            int maxRounds,
            double travelTimeSeconds,
            double fantasticRange,
            double perfectRange,
            double goodRange,
            int fantasticScore,
            int perfectScore,
            int goodScore,
            int missScore
    ) {
        if (maxRounds <= 0) {
            throw new IllegalArgumentException(
                    "maxRounds must be greater than 0"
            );
        }

        if (travelTimeSeconds <= 0.0) {
            throw new IllegalArgumentException(
                    "travelTimeSeconds must be greater than 0"
            );
        }

        if (fantasticRange < 0.0
                || perfectRange < fantasticRange
                || goodRange < perfectRange
                || goodRange > 1.0) {

            throw new IllegalArgumentException(
                    "Judgement ranges must satisfy: "
                            + "0 <= fantastic <= perfect <= good <= 1"
            );
        }

        this.maxRounds = maxRounds;
        this.travelTimeSeconds = travelTimeSeconds;

        this.fantasticRange = fantasticRange;
        this.perfectRange = perfectRange;
        this.goodRange = goodRange;

        this.fantasticScore = fantasticScore;
        this.perfectScore = perfectScore;
        this.goodScore = goodScore;
        this.missScore = missScore;
    }

    /*
     * =========================
     * 현재 테스트용 기본 설정
     * =========================
     */

    public static TimingConfig defaultConfig() {
        return new TimingConfig(
                5,
                1.5,

                0.04,
                0.1167,
                0.20,

                100,
                80,
                50,
                0
        );
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public double getTravelTimeSeconds() {
        return travelTimeSeconds;
    }

    public double getFantasticRange() {
        return fantasticRange;
    }

    public double getPerfectRange() {
        return perfectRange;
    }

    public double getGoodRange() {
        return goodRange;
    }

    public int getFantasticScore() {
        return fantasticScore;
    }

    public int getPerfectScore() {
        return perfectScore;
    }

    public int getGoodScore() {
        return goodScore;
    }

    public int getMissScore() {
        return missScore;
    }
}