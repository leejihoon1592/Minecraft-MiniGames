package io.github.leejihoon1592.minigames.game.weaving;

import java.util.ArrayList;
import java.util.List;

/**
 * 배짜기 게임의 리듬 패턴 생성기.
 *
 * 담당 기능:
 * 1. 파란색 / 빨간색 노드 묶음 관리
 * 2. 묶음 안의 개별 노드 생성
 * 3. 노드별 판정 시간 지정
 * 4. 라운드 마지막에 흰색 노드 생성
 *
 * 판정, 점수, 화면 출력은 담당하지 않는다.
 */
public final class WeavingPattern {

    private WeavingPattern() {
    }

    /*
     * =========================
     * 리듬 묶음 데이터
     * =========================
     *
     * type:
     * BLUE 또는 RED에 해당하는 노드 종류.
     *
     * count:
     * 해당 색상 노드의 개수.
     *
     * repeatTicks:
     * 같은 묶음 안에서 다음 노드까지의 간격.
     *
     * afterTicks:
     * 묶음이 끝나고 다음 노드까지의 간격.
     *
     * 노드 하나마다 별도의 판정 시간이 생긴다.
     */
    private record RhythmGroup(
            WeavingNote.Type type,
            int count,
            int repeatTicks,
            int afterTicks
    ) {

        private RhythmGroup {

            if (type == null
                    || type == WeavingNote.Type.SPACE
                    || count <= 0
                    || repeatTicks <= 0
                    || afterTicks <= 0) {

                throw new IllegalArgumentException(
                        "Invalid weaving rhythm group."
                );
            }
        }
    }

    /*
     * =========================
     * 연습용 리듬 생성
     * =========================
     *
     * 이번 단계에서는 기존 정박 간격을 유지한다.
     *
     * 다음 단계에서 repeatTicks를 줄여
     * 겹치는 노드를 만들 수 있다.
     */
    public static List<WeavingNote> createPracticeNotes(
            WeavingConfig config
    ) {

        if (config == null) {
            throw new IllegalArgumentException(
                    "config must not be null"
            );
        }

        /*
         * 현재 정박의 기본 간격.
         *
         * 기존 설정값은 12틱이다.
         */
        int beatTicks = config.getNoteIntervalTicks();

        /*
         * =========================
         * 연속 입력 테스트 간격
         * =========================
         *
         * 기본 정박 12틱의 1/4 간격이다.
         *
         * 현재 설정에서는 3틱이므로
         * 같은 색 노드를 0.15초 간격으로 입력한다.
         *
         * 추후 난이도 설정에서 조정한다.
         */
        int rapidTicks = Math.max(
                3,
                beatTicks / 4
        );

        /*
         * =========================
         * 연습용 라운드 구성
         * =========================
         *
         * 1라운드:
         * 파랑 2개 → 빨강 1개 → 흰색
         *
         * 2라운드:
         * 파랑 → 빨강 → 파랑 → 빨강 → 흰색
         *
         * 3라운드:
         * 파랑 1개 → 빨강 2개 → 흰색
         *
         * 현재는 안전한 정박 테스트용이다.
         * 빠른 연타와 엇박은 다음 단계에서 추가한다.
         */
        RhythmGroup[][] rounds = {

                {
                        /*
                         * 3라운드 연타 테스트
                         *
                         * 파랑 3연타
                         * → 빨강 3연타
                         * → 흰색 베틀 누르기
                         *
                         * 각 노드는 독립 판정이다.
                         */
                        new RhythmGroup(
                                WeavingNote.Type.LEFT,
                                3,
                                rapidTicks,
                                beatTicks
                        ),
                        new RhythmGroup(
                                WeavingNote.Type.RIGHT,
                                3,
                                rapidTicks,
                                beatTicks
                        )

                },

                {
                        new RhythmGroup(
                                WeavingNote.Type.LEFT,
                                1,
                                beatTicks,
                                beatTicks
                        ),
                        new RhythmGroup(
                                WeavingNote.Type.RIGHT,
                                1,
                                beatTicks,
                                beatTicks
                        ),
                        new RhythmGroup(
                                WeavingNote.Type.LEFT,
                                1,
                                beatTicks,
                                beatTicks
                        ),
                        new RhythmGroup(
                                WeavingNote.Type.RIGHT,
                                1,
                                beatTicks,
                                beatTicks
                        )
                },

                {
                        new RhythmGroup(
                                WeavingNote.Type.LEFT,
                                1,
                                beatTicks,
                                beatTicks
                        ),
                        new RhythmGroup(
                                WeavingNote.Type.RIGHT,
                                2,
                                beatTicks,
                                beatTicks
                        )
                }
        };

        List<WeavingNote> notes = new ArrayList<>();

        /*
         * 첫 노드의 판정 시간.
         */
        int nextTick = config.getApproachTicks();

        /*
         * =========================
         * 라운드별 노드 생성
         * =========================
         */
        for (
                int roundIndex = 0;
                roundIndex < rounds.length;
                roundIndex++
        ) {

            int roundNumber = roundIndex + 1;

            /*
             * 해당 라운드의 묶음을 순서대로 처리.
             */
            for (RhythmGroup group : rounds[roundIndex]) {

                /*
                 * 묶음의 노드도 각각 독립적으로 생성.
                 *
                 * 예: 파란색 3개라면
                 * LEFT 노드 3개가 만들어진다.
                 */
                for (
                        int i = 0;
                        i < group.count();
                        i++
                ) {

                    notes.add(
                            new WeavingNote(
                                    group.type(),
                                    nextTick,
                                    roundNumber
                            )
                    );

                    /*
                     * 묶음 내부에서는 연속 입력 간격,
                     * 마지막 노드에서는 다음 묶음 간격 사용.
                     */
                    if (i < group.count() - 1) {

                        nextTick += group.repeatTicks();

                    } else {

                        nextTick += group.afterTicks();
                    }
                }
            }

            /*
             * =========================
             * 라운드 마지막 흰색 노드
             * =========================
             *
             * 흰색은 베틀 압착 입력이다.
             */
            notes.add(
                    new WeavingNote(
                            WeavingNote.Type.SPACE,
                            nextTick,
                            roundNumber
                    )
            );

            /*
             * 다음 라운드 시작까지 대기.
             *
             * 향후 고난도 일시정지 기능은
             * 이 부분과 별도로 연결한다.
             */
            nextTick +=
                    config.getApproachTicks()
                            + config.getRoundGapTicks();
        }

        return List.copyOf(notes);
    }
}