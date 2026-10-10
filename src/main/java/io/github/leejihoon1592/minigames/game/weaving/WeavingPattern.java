package io.github.leejihoon1592.minigames.game.weaving;

import java.util.ArrayList;
import java.util.List;

/**
 * 천 배짜기 게임의 노트 패턴 생성.
 *
 * 각 라운드의 마지막 노트는 SPACE이다.
 *
 * 현재는 기능 점검용 고정 패턴을 사용한다.
 * 이후 난이도별 자동 생성 방식으로
 * 교체할 수 있도록 독립시킨다.
 */
public final class WeavingPattern {

    private WeavingPattern() {
    }

    /*
     * =========================
     * 연습용 노트 생성
     * =========================
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
         * =========================
         * 임시 라운드 구성
         * =========================
         *
         * 1라운드
         * 좌 - 좌 - 우 - 스페이스
         *
         * 2라운드
         * 우 - 좌 - 우 - 좌 - 스페이스
         *
         * 3라운드
         * 좌 - 우 - 우 - 스페이스
         *
         * 실제 패턴은 추후 수정한다.
         */

        WeavingNote.Type[][] rounds = {

                {
                        WeavingNote.Type.LEFT,
                        WeavingNote.Type.LEFT,
                        WeavingNote.Type.RIGHT,
                        WeavingNote.Type.SPACE
                },

                {
                        WeavingNote.Type.RIGHT,
                        WeavingNote.Type.LEFT,
                        WeavingNote.Type.RIGHT,
                        WeavingNote.Type.LEFT,
                        WeavingNote.Type.SPACE
                },

                {
                        WeavingNote.Type.LEFT,
                        WeavingNote.Type.RIGHT,
                        WeavingNote.Type.RIGHT,
                        WeavingNote.Type.SPACE
                }
        };

        List<WeavingNote> notes =
                new ArrayList<>();

        /*
         * 첫 번째 노트의 목표 입력 시간.
         */

        int nextTick =
                config.getApproachTicks();

        /*
         * =========================
         * 라운드별 노트 생성
         * =========================
         */

        for (
                int roundIndex = 0;
                roundIndex < rounds.length;
                roundIndex++
        ) {

            for (
                    WeavingNote.Type type : rounds[roundIndex]
            ) {

                notes.add(
                        new WeavingNote(
                                type,
                                nextTick,
                                roundIndex + 1
                        )
                );

                /*
                 * 일반 노트 다음에는
                 * 기본 노트 간격을 사용한다.
                 *
                 * 스페이스바 다음에는
                 * 다음 라운드까지의 간격을 사용한다.
                 */

                if (type == WeavingNote.Type.SPACE) {

                    /*
                     * 스페이스바 노트 이후
                     * 다음 라운드 시작까지 대기한다.
                     *
                     * approachTicks:
                     * 다음 노트가 화면 오른쪽에서
                     * 판정기까지 이동하는 시간.
                     *
                     * roundGapTicks:
                     * 스페이스바 이후
                     * 새로운 노트가 등장하기까지의 대기시간.
                     *
                     * 기본 설정:
                     * 60 + 24 = 84Tick
                     */

                    nextTick +=
                            config.getApproachTicks()
                                    + config.getRoundGapTicks();

                } else {

                    nextTick +=
                            config.getNoteIntervalTicks();
                }
            }
        }

        return List.copyOf(notes);
    }
}