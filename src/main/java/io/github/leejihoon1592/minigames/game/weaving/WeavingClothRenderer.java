package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 천 배짜기 게임의 실 이동 렌더러.
 *
 * 담당 기능:
 *
 * 1. 파란색 실 출력
 * 2. 빨간색 실 출력
 * 3. 노트 이동 시간과 실 이동 동기화
 * 4. 이동 완료된 실 유지
 *
 * 이 클래스는 점수와 HIT/MISS 판정을
 * 계산하지 않는다.
 *
 * 베틀 눌림과 실 정렬 애니메이션은
 * 다음 개발 단계에서 추가한다.
 */
public final class WeavingClothRenderer {

    /*
     * =========================
     * 색상 설정
     * =========================
     */

    private static final int BLUE = 0xFF3549CD;
    private static final int RED = 0xFFE91E29;
    private static final int WHITE = 0xFFFFFFFF;

    /*
     * 실 두께
     */
    private static final int THREAD_THICKNESS = 2;

    /*
     * 가로 방향 샘플 간격.
     *
     * 숫자가 작을수록
     * 곡선이 정밀하게 표시된다.
     */
    private static final int DRAW_STEP = 2;

    private WeavingClothRenderer() {
    }

    /*
     * =========================
     * 천 제작 화면 출력
     * =========================
     */

    public static void render(
            GuiGraphics graphics,
            int width,
            int height,
            WeavingGame game,
            WeavingConfig config,
            double partialTick
    ) {

        /*
         * 상단 천 제작 영역.
         *
         * 화면 크기에 따라
         * 아래쪽 영역이 자동으로 제한된다.
         */

        int clothTop = 28;

        int clothBottom = Math.max(
                clothTop + 18,
                Math.min(
                        105,
                        height / 2 - 16
                )
        );

        /*
         * 천 제작 영역 상단의
         * 흰색 기준선.
         */

        graphics.fill(
                0,
                clothTop - 2,
                width,
                clothTop,
                WHITE
        );

        /*
         * =========================
         * 실 개수 확인
         * =========================
         *
         * SPACE는 베틀 누르기이므로
         * 실제 실 개수에서 제외한다.
         */

        int totalThreads = 0;

        for (WeavingNote note : game.getNotes()) {

            if (note.getType()
                    != WeavingNote.Type.SPACE) {

                totalThreads++;
            }
        }

        /*
         * 각 실이 표시될 세로 간격.
         */

        int rowSpacing = Math.max(
                5,
                (clothBottom - clothTop - 8)
                        / Math.max(1, totalThreads)
        );

        /*
         * =========================
         * 현재 게임 시간
         * =========================
         *
         * 하단 노트와 동일한 시간을 사용한다.
         *
         * 따라서 렌더링 FPS가 달라도
         * 실과 노트의 진행 시간이 일치한다.
         */

        double currentTime =
                game.getElapsedTicks() + partialTick;

        /*
         * 현재 처리 중인 실의 순서.
         */

        int threadIndex = 0;

        /*
         * =========================
         * 각 실의 이동 처리
         * =========================
         */

        for (WeavingNote note : game.getNotes()) {

            /*
             * 흰색 노트는 실이 아니다.
             *
             * 이후 베틀 정렬 애니메이션에서
             * 별도로 처리한다.
             */

            if (note.getType()
                    == WeavingNote.Type.SPACE) {

                continue;
            }

            /*
             * 실의 세로 위치.
             *
             * 실이 한 줄씩 아래쪽에
             * 추가되는 방식이다.
             */

            int baseY =
                    clothBottom - 7
                            - threadIndex * rowSpacing;

            threadIndex++;

            /*
             * 현재 화면의 천 영역을
             * 초과한 줄은 그리지 않는다.
             *
             * 나중에 긴 제작 공정을 만들 때
             * 화면 스크롤 방식으로 확장할 수 있다.
             */

            if (baseY < clothTop + 4) {
                continue;
            }

            /*
             * 실의 색상 결정.
             */

            int color =
                    note.getType() == WeavingNote.Type.LEFT
                            ? BLUE
                            : RED;

            /*
             * =========================
             * 실 이동 시작 시간
             * =========================
             *
             * 하단 노트가 화면 오른쪽에서
             * 출발하는 시간과 동일하다.
             */

            double startTime =
                    note.getTargetTick()
                            - config.getApproachTicks();

            /*
             * 아직 노트가 등장하기 전이면
             * 해당 실도 출력하지 않는다.
             */

            if (currentTime < startTime) {
                continue;
            }

            /*
             * =========================
             * 실 이동 진행률
             * =========================
             *
             * 0.0 = 왼쪽 끝
             * 0.5 = 화면 중앙
             * 1.0 = 오른쪽 끝
             *
             * HIT/MISS 여부는 확인하지 않는다.
             */

            double progress =
                    (currentTime - startTime)
                            / config.getApproachTicks();

            progress = Math.max(
                    0.0,
                    Math.min(1.0, progress)
            );

            /*
             * 현재 실이 도달한 X 좌표.
             */

            int headX = (int) Math.round(
                    width * progress
            );

            /*
             * =========================
             * 불규칙한 실 출력
             * =========================
             *
             * 사인 함수를 이용해
             * 실이 약간 울퉁불퉁한
             * 형태가 되도록 한다.
             *
             * 이 곡선은 시각적 표현이며
             * 제작 정확도와 무관하다.
             */

            for (
                    int x = 0;
                    x < headX;
                    x += DRAW_STEP
            ) {

                /*
                 * 동일한 실은 이동 중에도
                 * 같은 모양을 유지한다.
                 *
                 * 실 순서에 따라
                 * 곡선 모양이 달라진다.
                 */

                double wave1 =
                        Math.sin(
                                x * 0.035
                                        + threadIndex * 1.7
                        ) * 1.5;

                double wave2 =
                        Math.sin(
                                x * 0.075
                                        + threadIndex * 0.65
                        ) * 1.0;

                int waveOffset =
                        (int) Math.round(
                                wave1 + wave2
                        );

                int drawY =
                        baseY + waveOffset;

                /*
                 * 실제 실 출력.
                 */

                graphics.fill(
                        x,
                        drawY,
                        Math.min(x + DRAW_STEP, headX),
                        drawY + THREAD_THICKNESS,
                        color
                );
            }

            /*
             * =========================
             * 움직이는 실 끝부분
             * =========================
             *
             * 이동 중인 실의 끝을
             * 조금 두껍게 표시한다.
             *
             * 오른쪽 끝까지 이동하면
             * 끝부분 표시를 제거한다.
             */

            if (progress > 0.0
                    && progress < 1.0) {

                graphics.fill(
                        headX - 2,
                        baseY - 3,
                        headX + 2,
                        baseY + 4,
                        color
                );
            }
        }
    }
}