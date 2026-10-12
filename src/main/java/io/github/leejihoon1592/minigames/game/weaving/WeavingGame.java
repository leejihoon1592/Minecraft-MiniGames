package io.github.leejihoon1592.minigames.game.weaving;

import java.util.Arrays;
import java.util.List;


/**
 * 천 배짜기 게임의 진행과 입력 처리.
 *
 * 노트의 시간, HIT, MISS, 라운드 진행을 담당한다.
 * 화면 출력과 제작 점수는 담당하지 않는다.
 */
public final class WeavingGame {

    /*
     * 임시 입력 허용 범위.
     *
     * 목표 입력 시간의 앞뒤 5Tick 이내에
     * 올바른 입력을 하면 HIT로 처리한다.
     *
     * 점수별 세부 판정은 추후 구현한다.
     */
    private static final int HIT_WINDOW_TICKS = 5;

    private final List<WeavingNote> notes;

    private int elapsedTicks = 0;
    private int nextNoteIndex = 0;

    private int hitCount = 0;
    private int missCount = 0;
    private int completedRounds = 0;

    /*
     * 각 라운드에서 베틀이 눌린 시점.
     *
     * 배열의 인덱스는 라운드 번호 - 1이다.
     * -1은 아직 눌리지 않았다는 뜻이다.
     */
    private final int[] loomPressTicks;

    /*
     * =========================
     * 노드별 판정 허용 시간
     * =========================
     *
     * 일반 노드는 기존 ±5틱을 유지한다.
     *
     * 노드 간격이 짧아질수록
     * 판정 허용 범위도 자동으로 좁아진다.
     *
     * 연속 노드가 서로의 판정 시간을
     * 침범하지 않도록 하기 위한 기능이다.
     */
    private int getHitWindowTicks(int noteIndex) {

        /*
         * 기본 판정 범위는 유지한다.
         */
        int nearestGap = Integer.MAX_VALUE;

        int targetTick =
                notes.get(noteIndex).getTargetTick();

        /*
         * 이전 노드와의 간격 확인
         */
        if (noteIndex > 0) {

            int previousTick =
                    notes.get(noteIndex - 1).getTargetTick();

            nearestGap = Math.min(
                    nearestGap,
                    targetTick - previousTick
            );
        }

        /*
         * 다음 노드와의 간격 확인
         */
        if (noteIndex + 1 < notes.size()) {

            int nextTick =
                    notes.get(noteIndex + 1).getTargetTick();

            nearestGap = Math.min(
                    nearestGap,
                    nextTick - targetTick
            );
        }

        /*
         * 연타 구간은 판정을 너무 좁게 만들지 않는다.
         *
         * 기존 방식은 3틱 연타에서
         * 사실상 1틱 판정까지 줄어들 수 있어서
         * 연타 중 하나가 MISS 나는 현상이 있었다.
         */
        if (nearestGap <= 3) {
            return 2;
        }

        if (nearestGap <= 6) {
            return 3;
        }

        return HIT_WINDOW_TICKS;
    }

    public WeavingGame(List<WeavingNote> notes) {

        if (notes == null || notes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Weaving notes must not be empty."
            );
        }

        this.notes = List.copyOf(notes);

        /*
         * 라운드별 베틀 누르기 시간을 준비한다.
         */
        this.loomPressTicks =
                new int[getTotalRounds()];

        Arrays.fill(loomPressTicks, -1);
    }

    /*
     * =========================
     * 게임 시간 진행
     * =========================
     */

    public void tick() {

        /*
         * 게임이 종료되어도 시간은 계속 흐른다.
         *
         * 마지막 베틀 누르기 애니메이션이
         * 도중에 멈추지 않도록 하기 위함이다.
         */
        elapsedTicks++;

        if (isFinished()) {
            return;
        }

        /*
         * 입력 가능 시간을 지나친 노트는
         * 자동으로 MISS 처리한다.
         */
        while (!isFinished()) {

            WeavingNote note =
                    notes.get(nextNoteIndex);

            if (elapsedTicks
                    <= note.getTargetTick()
                    + getHitWindowTicks(nextNoteIndex)) {
                break;
            }


            note.markMiss();
            missCount++;

            advanceNote(note);
        }
    }

    /*
     * =========================
     * 플레이어 입력
     * =========================
     */

    public boolean submit(WeavingNote.Type inputType) {

        if (isFinished() || inputType == null) {
            return false;
        }

        WeavingNote note =
                notes.get(nextNoteIndex);

        /*
         * 현재 처리해야 할 노트와
         * 입력 종류가 다르면 인정하지 않는다.
         */
        if (note.getType() != inputType) {
            return false;
        }

        int timingDifference =
                Math.abs(
                        elapsedTicks - note.getTargetTick()
                );

        if (timingDifference > getHitWindowTicks(nextNoteIndex)) {
            return false;
        }

        note.markHit();
        hitCount++;

        advanceNote(note);

        return true;
    }

    /*
     * =========================
     * 다음 노트로 진행
     * =========================
     */

    private void advanceNote(WeavingNote note) {

        /*
         * SPACE 노트는 라운드의 마지막이다.
         *
         * HIT와 MISS 모두 라운드를 종료한다.
         */
        if (note.getType() == WeavingNote.Type.SPACE) {
            completedRounds++;

            /*
             * HIT와 MISS를 구분하지 않고
             * 실제로 라운드가 완료된 시간을 저장한다.
             *
             * 이후 렌더러가 이 시간을 기준으로
             * 베틀 애니메이션을 실행한다.
             */
            loomPressTicks[note.getRoundNumber() - 1] =
                    elapsedTicks;
        }


        nextNoteIndex++;
    }

    public boolean isFinished() {
        return nextNoteIndex >= notes.size();
    }

    public List<WeavingNote> getNotes() {
        return notes;
    }

    public int getElapsedTicks() {
        return elapsedTicks;
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getMissCount() {
        return missCount;
    }

    public int getCompletedRounds() {
        return completedRounds;
    }

    public int getTotalRounds() {
        return notes.get(notes.size() - 1)
                .getRoundNumber();
    }

    public int getTotalNotes() {
        return notes.size();
    }

    public int getProcessedNotes() {
        return nextNoteIndex;
    }
    /*
     * =========================
     * 베틀 누르기 시점 조회
     * =========================
     *
     * 반환값:
     * -1 = 아직 해당 라운드를 누르지 않음
     * 0 이상 = 베틀을 누른 게임 시간
     */
    public int getLoomPressTick(int roundNumber) {

        if (roundNumber < 1
                || roundNumber > loomPressTicks.length) {
            return -1;
        }

        return loomPressTicks[roundNumber - 1];
    }
}