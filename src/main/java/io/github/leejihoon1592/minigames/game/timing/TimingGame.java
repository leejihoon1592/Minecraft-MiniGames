package io.github.leejihoon1592.minigames.game.timing;

public class TimingGame {

    private final TimingConfig config;

    private int currentRound;
    private int totalScore;

    private int fantasticCount;
    private int perfectCount;
    private int goodCount;
    private int missCount;

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

        this.fantasticCount = 0;
        this.perfectCount = 0;
        this.goodCount = 0;
        this.missCount = 0;

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

        switch (judgement) {

            case FANTASTIC ->
                    fantasticCount++;

            case PERFECT ->
                    perfectCount++;

            case GOOD ->
                    goodCount++;

            case MISS ->
                    missCount++;
        }

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
     * 결과 생성
     * =========================
     */

    public TimingResult getResult() {

        return new TimingResult(
                totalScore,
                getMaximumScore(),
                currentRound,
                fantasticCount,
                perfectCount,
                goodCount,
                missCount
        );
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
     *
     * 게임 로직은 판정의 의미만 가진다.
     * 표시 문자열과 색상은 Client가 담당한다.
     * =========================
     */

    public enum Judgement {
        FANTASTIC,
        PERFECT,
        GOOD,
        MISS
    }
}