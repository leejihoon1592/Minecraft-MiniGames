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

    /*
     * =========================
     * 압착 후 줄 간격 / 두께
     * =========================
     *
     * 베틀이 실을 누른 뒤에는
     * 줄 간격을 좁혀서
     * 더 촘촘한 천처럼 보이게 한다.
     */

    private static final int PACKED_ROW_SPACING = 4;
    private static final int PACKED_THREAD_THICKNESS = 3;

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
         * 세로 날실 출력
         * =========================
         *
         * 렌더링 프레임마다 단 한 번 실행한다.
         *
         * 가로 실의 반복문이 시작되기 전에
         * 날실을 먼저 그려 성능 저하를 방지한다.
         */
        WeavingWarpRenderer.render(
                graphics,
                width,
                clothTop,
                clothBottom
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
                            - threadIndex * rowSpacing;/*
             * 베틀로 눌렀을 때의 목표 위치.
             *
             * baseY는 원래 줄 위치,
             * packedY는 압착 후 더 촘촘한 위치다.
             */
            int packedY =
                    clothBottom - 7
                            - threadIndex * PACKED_ROW_SPACING;

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
             * 베틀에 의한 실 정렬
             * =========================
             *
             * 각 실은 자신이 속한 라운드의
             * 베틀 누르기 시간을 확인한다.
             *
             * 아직 누르지 않았다면
             * 구불구불한 모양을 유지한다.
             *
             * 베틀이 바닥에 닿으면
             * 2틱 동안 실이 평평해진다.
             *
             * HIT와 MISS는 구분하지 않는다.
             */

            int pressTick =
                    game.getLoomPressTick(
                            note.getRoundNumber()
                    );


            /*
             * 베틀 하강과 실 압착 동기화.
             *
             * 베틀이 내려오는 진행률을
             * 실의 위치, 두께, 굴곡에도 똑같이 적용한다.
             *
             * 하강 완료 후에는 압착 상태를 유지한다.
             */
            double flattenProgress = 0.0;

            if (pressTick >= 0) {

                double pressAge =
                        currentTime - pressTick;

                flattenProgress =
                        WeavingLoomRenderer.getCompressionProgress(
                                pressAge
                        );
            }

            /*
             * 실의 압착 위치와 두께.
             * 실 출력 반복문과 같은 범위에서 계산한다.
             */
            int renderBaseY = (int) Math.round(
                    baseY + (packedY - baseY) * flattenProgress
            );

            int renderThickness = (int) Math.round(
                    THREAD_THICKNESS
                            + (PACKED_THREAD_THICKNESS - THREAD_THICKNESS)
                            * flattenProgress
            );

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

                /*
                 * 정렬이 진행될수록
                 * 실의 굴곡을 줄인다.
                 *
                 * flattenProgress = 0.0
                 * → 원래 구불구불한 실
                 *
                 * flattenProgress = 1.0
                 * → 완전히 평평한 실
                 */
                int waveOffset =
                        (int) Math.round(
                                (wave1 + wave2)
                                        * (1.0 - flattenProgress)
                        );

                int drawY =
                        renderBaseY + waveOffset;



                /*
                 * 실제 실 출력.
                 */

                /*
                 * =========================
                 * 실제 실 출력
                 * =========================
                 *
                 * 날실이 지나가는 위치에서는
                 * 씨실을 1픽셀 비워
                 * 직조 느낌을 만든다.
                 */
                /*
                 * 빨강·파랑 가로실을 끊김 없이 출력한다.
                 *
                 * 날실과의 교차 무늬는
                 * 아래 renderCrossings에서 따로 처리한다.
                 */
                graphics.fill(
                        x,
                        drawY,
                        Math.min(x + DRAW_STEP, headX),
                        drawY + renderThickness,
                        color
                );

            } // 가로실 출력 반복문 종료

                /*
                 * =========================
                 * 직물 교차 무늬 출력
                 * =========================
                 *
                 * 베틀 압착이 완료된 가로 실에만
                 * 날실과 씨실의 교차 무늬를 그린다.
                 *
                 * 가로 실 출력 반복문 바깥에서
                 * 딱 한 번 호출한다.
                 *
                 * threadIndex는 이미 1 증가했으므로
                 * 실제 가로 실 번호는 threadIndex - 1이다.
                 */
                if (flattenProgress >= 1.0) {

                    WeavingWarpRenderer.renderCrossings(
                            graphics,
                            width,
                            headX,
                            renderBaseY,
                            renderThickness,
                            threadIndex - 1
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