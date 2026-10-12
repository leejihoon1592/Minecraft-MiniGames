package io.github.leejihoon1592.minigames.game.weaving;

import java.util.Arrays;
import java.util.List;

/**
 * 배짜기 게임의 진행 및 판정 시스템.
 *
 * 담당 기능:
 *
 * 1. 노드의 HIT / MISS 처리
 * 2. Fantastic / Perfect / Good 판정
 * 3. 판정별 임시 점수 계산
 * 4. 라운드 완료 처리
 * 5. 베틀 압착 시간 기록
 *
 * 화면 출력은 담당하지 않는다.
 */
public final class WeavingGame {

    /*
     * =========================
     * 판정 등급
     * =========================
     *
     * NONE:
     * 아직 판정이 발생하지 않은 상태.
     */
    public enum Judgement {
        NONE,
        FANTASTIC,
        PERFECT,
        GOOD,
        MISS
    }

    /*
     * =========================
     * 판정 허용 시간
     * =========================
     *
     * 현재는 테스트용 설정이다.
     *
     * Minecraft 20틱 = 약 1초.
     *
     * Fantastic:
     * 목표 시간에서 앞뒤 2틱 이내.
     *
     * Perfect:
     * Fantastic 범위 밖,
     * 앞뒤 4틱 이내.
     *
     * Good:
     * Perfect 범위 밖,
     * 앞뒤 6틱 이내.
     *
     * 이 값은 나중에 난이도에 따라
     * 조정할 수 있도록 분리했다.
     */
    public static final int FANTASTIC_WINDOW_TICKS = 4;
    public static final int PERFECT_WINDOW_TICKS = 6;
    public static final int GOOD_WINDOW_TICKS = 8;

    /*
     * =========================
     * 판정별 임시 점수
     * =========================
     *
     * 제작 시스템과 연결할 때
     * 점수를 다시 조정할 수 있다.
     *
     * 현재는 테스트용 점수다.
     */
    public static final int FANTASTIC_POINTS = 100;
    public static final int PERFECT_POINTS = 80;
    public static final int GOOD_POINTS = 50;

    /*
     * =========================
     * 게임 데이터
     * =========================
     */
    private final List<WeavingNote> notes;

    private int elapsedTicks = 0;
    private int nextNoteIndex = 0;

    /*
     * 기존 HIT / MISS 기록.
     *
     * HIT에는 세 가지 성공 판정이
     * 모두 포함된다.
     */
    private int hitCount = 0;
    private int missCount = 0;
    private int completedRounds = 0;

    /*
     * =========================
     * 판정별 기록
     * =========================
     */
    private int fantasticCount = 0;
    private int perfectCount = 0;
    private int goodCount = 0;

    /*
     * 현재 획득한 임시 점수.
     */
    private int totalScore = 0;

    /*
     * 가장 최근의 판정 결과.
     *
     * 다음 단계에서 화면에
     * 판정 글씨를 출력할 때 사용한다.
     */
    private Judgement lastJudgement = Judgement.NONE;
    private int lastJudgementTick = -1;

    /*
     * =========================
     * 베틀 압착 시점
     * =========================
     *
     * 배열 번호 = 라운드 번호 - 1.
     *
     * -1이면 아직 베틀을 누르지 않았다.
     */
    private final int[] loomPressTicks;

    /*
     * =========================
     * 게임 생성자
     * =========================
     */
    public WeavingGame(List<WeavingNote> notes) {

        if (notes == null || notes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Weaving notes must not be empty."
            );
        }

        this.notes = List.copyOf(notes);

        this.loomPressTicks =
                new int[getTotalRounds()];

        Arrays.fill(loomPressTicks, -1);
    }

    /*
     * =========================
     * 입력 시간에 따른 판정 등급
     * =========================
     *
     * 노드 중심과 판정기 중심 사이의
     * 시간 차이를 계산한다.
     *
     * 다음 단계에서 화면에 그릴
     * 색상별 판정 원도 이 수치를 사용한다.
     *
     * 따라서 판정 원과 실제 판정이
     * 서로 다른 값을 사용하지 않는다.
     */
    private Judgement calculateJudgement(
            int timingDifference
    ) {

        if (timingDifference <= FANTASTIC_WINDOW_TICKS) {
            return Judgement.FANTASTIC;
        }

        if (timingDifference <= PERFECT_WINDOW_TICKS) {
            return Judgement.PERFECT;
        }

        if (timingDifference <= GOOD_WINDOW_TICKS) {
            return Judgement.GOOD;
        }

        return Judgement.MISS;
    }

    /*
     * =========================
     * 판정 등급별 점수
     * =========================
     *
     * NONE / MISS는 0점.
     */
    public static int getPointsFor(
            Judgement judgement
    ) {

        return switch (judgement) {
            case FANTASTIC -> FANTASTIC_POINTS;
            case PERFECT -> PERFECT_POINTS;
            case GOOD -> GOOD_POINTS;
            case MISS, NONE -> 0;
        };
    }

    /*
     * =========================
     * 게임 시간 진행
     * =========================
     */
    public void tick() {

        /*
         * 게임 종료 후에도 시간을 증가시킨다.
         *
         * 마지막 베틀 애니메이션이
         * 멈추지 않도록 하는 기존 기능이다.
         */
        elapsedTicks++;

        if (isFinished()) {
            return;
        }

        /*
         * Good 판정의 허용 시간을 지나면
         * 해당 노드를 MISS 처리한다.
         *
         * 연속 노드가 여러 개 지나간 경우에도
         * 하나씩 빠짐없이 처리한다.
         */
        while (!isFinished()) {

            WeavingNote note =
                    notes.get(nextNoteIndex);

            if (elapsedTicks
                    <= note.getTargetTick()
                    + GOOD_WINDOW_TICKS) {

                break;
            }

            registerMiss(note);
        }
    }

    /*
     * =========================
     * 플레이어 입력 처리
     * =========================
     *
     * 노드 하나에 입력 한 번만 적용한다.
     *
     * 꾹 누르기나 연속 자동 입력은 없다.
     *
     * 입력 성공 여부와 판정 등급은
     * 서로 구분해 저장한다.
     */
    public boolean submit(
            WeavingNote.Type inputType
    ) {

        if (isFinished() || inputType == null) {
            return false;
        }

        WeavingNote note =
                notes.get(nextNoteIndex);

        /*
         * 현재 노드와 입력 종류가 다르면
         * 기존 방식대로 인정하지 않는다.
         */
        if (note.getType() != inputType) {
            return false;
        }

        /*
         * 목표 시간과 실제 입력 시간 차이.
         */
        int timingDifference = Math.abs(
                elapsedTicks - note.getTargetTick()
        );

        /*
         * 해당 노드의 판정 등급 계산.
         */
        Judgement judgement =
                calculateJudgement(timingDifference);

        /*
         * Good 판정 범위 밖에서
         * 현재 노드에 해당하는 키를 누르면
         * 즉시 MISS 처리하고 다음 노드로 진행한다.
         *
         * 아무것도 누르지 않고 지나간 경우에는
         * 기존 tick()이 자동으로 MISS 처리한다.
         */
        if (judgement == Judgement.MISS) {
            registerMiss(note);
            return false;
        }

        /*
         * =========================
         * HIT 처리
         * =========================
         */
        note.markHit();
        hitCount++;

        /*
         * 판정 등급 기록.
         */
        switch (judgement) {

            case FANTASTIC -> fantasticCount++;

            case PERFECT -> perfectCount++;

            case GOOD -> goodCount++;

            default -> {
                // 성공 판정 외에는 실행하지 않는다.
            }
        }

        /*
         * 해당 판정의 점수 합산.
         *
         * 기본 임시 설정:
         *
         * Fantastic = 100점
         * Perfect   =  80점
         * Good      =  50점
         */
        totalScore += getPointsFor(judgement);

        /*
         * 가장 최근 판정 저장.
         */
        lastJudgement = judgement;
        lastJudgementTick = elapsedTicks;

        /*
         * 다음 노드로 진행.
         */
        advanceNote(note);

        return true;
    }

    /*
     * =========================
     * MISS 처리
     * =========================
     *
     * 실제 입력이 없어서
     * 노드가 판정 범위를 지나간 경우.
     */
    private void registerMiss(
            WeavingNote note
    ) {

        note.markMiss();
        missCount++;

        lastJudgement = Judgement.MISS;
        lastJudgementTick = elapsedTicks;

        /*
         * MISS 점수는 현재 0점이다.
         *
         * 제작 시스템의 추가 페널티는
         * 나중에 별도로 적용한다.
         */

        advanceNote(note);
    }

    /*
     * =========================
     * 다음 노드 진행
     * =========================
     *
     * 기존 베틀 압착 로직을 유지한다.
     */
    private void advanceNote(
            WeavingNote note
    ) {

        /*
         * 흰색 노드는 라운드의 마지막.
         *
         * 흰색 노드가 HIT 또는 MISS가 되면
         * 해당 라운드를 완료한다.
         */
        if (note.getType()
                == WeavingNote.Type.SPACE) {

            completedRounds++;

            /*
             * 베틀이 눌린 시간을 저장한다.
             *
             * 기존 애니메이션과 연결되는 값이다.
             */
            loomPressTicks[
                    note.getRoundNumber() - 1
                    ] = elapsedTicks;
        }

        /*
         * 다음 노드로 이동.
         */
        nextNoteIndex++;
    }

    /*
     * =========================
     * 기존 진행 정보 조회
     * =========================
     */

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
     * 판정별 기록 조회
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

    /*
     * =========================
     * 점수 조회
     * =========================
     */

    public int getTotalScore() {
        return totalScore;
    }

    /*
     * 현재 패턴의 이론상 최대 점수.
     *
     * 모든 노드를 Fantastic으로
     * 판정했을 때의 점수다.
     */
    public int getMaximumScore() {
        return notes.size() * FANTASTIC_POINTS;
    }

    /*
     * =========================
     * 최근 판정 조회
     * =========================
     */

    public Judgement getLastJudgement() {
        return lastJudgement;
    }

    public int getLastJudgementTick() {
        return lastJudgementTick;
    }

    /*
     * =========================
     * 베틀 누르기 시점 조회
     * =========================
     *
     * -1 = 아직 해당 라운드를 누르지 않음
     */
    public int getLoomPressTick(
            int roundNumber
    ) {

        if (roundNumber < 1
                || roundNumber > loomPressTicks.length) {

            return -1;
        }

        return loomPressTicks[roundNumber - 1];
    }
}