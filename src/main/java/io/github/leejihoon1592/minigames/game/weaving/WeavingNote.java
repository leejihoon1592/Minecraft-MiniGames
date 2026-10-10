package io.github.leejihoon1592.minigames.game.weaving;

/**
 * 천 배짜기 게임의 노트 데이터.
 *
 * 노트의 종류, 목표 입력 시간,
 * 소속 라운드, 처리 상태를 관리한다.
 *
 * 화면 출력과 점수 계산은 담당하지 않는다.
 */
public final class WeavingNote {

    /*
     * =========================
     * 노트 종류
     * =========================
     */

    public enum Type {

        // 파란색 - 마우스 좌클릭
        LEFT,

        // 빨간색 - 마우스 우클릭
        RIGHT,

        // 흰색 - 스페이스바
        SPACE
    }

    /*
     * =========================
     * 노트 처리 상태
     * =========================
     */

    public enum State {
        WAITING,
        HIT,
        MISS
    }

    private final Type type;
    private final int targetTick;
    private final int roundNumber;

    private State state;

    public WeavingNote(
            Type type,
            int targetTick,
            int roundNumber
    ) {

        if (type == null
                || targetTick < 0
                || roundNumber < 1) {

            throw new IllegalArgumentException(
                    "Invalid weaving note."
            );
        }

        this.type = type;
        this.targetTick = targetTick;
        this.roundNumber = roundNumber;

        this.state = State.WAITING;
    }

    /*
     * =========================
     * 데이터 확인
     * =========================
     */

    public Type getType() {
        return type;
    }

    public int getTargetTick() {
        return targetTick;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public State getState() {
        return state;
    }

    public boolean isWaiting() {
        return state == State.WAITING;
    }

    /*
     * =========================
     * 입력 처리
     * =========================
     */

    public void markHit() {

        if (isWaiting()) {
            state = State.HIT;
        }
    }

    public void markMiss() {

        if (isWaiting()) {
            state = State.MISS;
        }
    }
}