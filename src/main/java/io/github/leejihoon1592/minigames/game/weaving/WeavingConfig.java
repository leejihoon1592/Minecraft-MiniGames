package io.github.leejihoon1592.minigames.game.weaving;

/**
 * 천 배짜기 리듬게임의 임시 연습용 시간 설정.
 *
 * 점수, 판정 등급, 제작 난이도는 여기서 결정하지 않는다.
 * 이후 제작 모드의 난이도를 전달받을 수 있도록
 * 숫자를 객체에 모아서 관리한다.
 */
public final class WeavingConfig {

    private final int approachTicks;
    private final int noteIntervalTicks;
    private final int roundGapTicks;

    public WeavingConfig(
            int approachTicks,
            int noteIntervalTicks,
            int roundGapTicks
    ) {

        if (approachTicks <= 0
                || noteIntervalTicks <= 0
                || roundGapTicks <= 0) {

            throw new IllegalArgumentException(
                    "All weaving timing values must be positive."
            );
        }

        this.approachTicks = approachTicks;
        this.noteIntervalTicks = noteIntervalTicks;
        this.roundGapTicks = roundGapTicks;
    }

    public static WeavingConfig defaultConfig() {

        /*
         * 임시 테스트 값.
         *
         * 실제 속도와 난이도는
         * 게임 플레이 후 조정한다.
         */

        return new WeavingConfig(
                60,
                12,
                24
        );
    }

    public int getApproachTicks() {
        return approachTicks;
    }

    public int getNoteIntervalTicks() {
        return noteIntervalTicks;
    }

    public int getRoundGapTicks() {
        return roundGapTicks;
    }
}