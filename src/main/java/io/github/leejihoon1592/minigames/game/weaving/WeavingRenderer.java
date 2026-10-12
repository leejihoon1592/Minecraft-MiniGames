package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 배짜기 게임의 기본 화면 렌더러.
 *
 * 담당 기능:
 *
 * 1. 배경 출력
 * 2. 천 제작 렌더러 호출
 * 3. 베틀 애니메이션 호출
 * 4. 판정기 출력
 * 5. 일반 노드 및 연타 스택 출력
 * 6. HIT / MISS 정보 출력
 *
 * 실제 노드 판정과 점수는 변경하지 않는다.
 */
public final class WeavingRenderer {

    /*
     * =========================
     * 기본 색상
     * =========================
     */

    private static final int BLUE = 0xFF3549CD;
    private static final int RED = 0xFFE91E29;
    private static final int WHITE = 0xFFFFFFFF;

    /*
     * =========================
     * 노드 외형 설정
     * =========================
     */

    private static final int NOTE_RADIUS = 15;
    private static final int NOTE_OUTLINE_RADIUS = 16;

    /*
     * 연타 스택의 세로 높이 차이.
     *
     * 앞쪽 노드는 원래 판정선 높이에 놓고
     * 뒤쪽 노드일수록 위로 올린다.
     *
     * 가로 좌표는 원래 노트 시간에 맞춰
     * 그대로 이동시킨다.
     */
    private static final int STACK_OFFSET_Y = 7;

    private WeavingRenderer() {
    }

    /*
     * =========================
     * 게임 화면 전체 출력
     * =========================
     */

    public static void render(
            GuiGraphics graphics,
            Font font,
            int width,
            int height,
            WeavingGame game,
            WeavingConfig config,
            double partialTick,
            Component message
    ) {

        /*
         * 전체 배경
         */
        graphics.fill(
                0,
                0,
                width,
                height,
                0xFF000000
        );

        /*
         * =========================
         * 실제 천 제작 출력
         * =========================
         *
         * 기존 렌더러를 그대로 사용한다.
         */
        WeavingClothRenderer.render(
                graphics,
                width,
                height,
                game,
                config,
                partialTick
        );

        /*
         * =========================
         * 베틀 압착 애니메이션
         * =========================
         *
         * 기존 애니메이션을 그대로 사용한다.
         */
        WeavingLoomRenderer.render(
                graphics,
                width,
                height,
                game,
                partialTick
        );

        /*
         * =========================
         * 판정기 위치
         * =========================
         */

        int hitX = Math.max(
                50,
                Math.min(85, width / 5)
        );

        int laneY = (int) (height * 0.76);

        /*
         * 기존 노드 이동 속도를 유지한다.
         *
         * 각 노드는 자신의 판정 시간에 맞춰
         * 판정선으로 이동한다.
         */
        double pixelsPerTick =
                (double) (width - hitX - 14)
                        / config.getApproachTicks();

        /*
         * 판정기 중심선
         */
        graphics.fill(
                hitX - 1,
                laneY - 29,
                hitX + 1,
                laneY + 29,
                0xFFD4B65A
        );

        /*
         * 판정기 원형 테두리
         */
        drawCircleOutline(
                graphics,
                hitX,
                laneY,
                23,
                WHITE
        );

        drawCircleOutline(
                graphics,
                hitX,
                laneY,
                20,
                WHITE
        );

        drawCircleOutline(
                graphics,
                hitX,
                laneY,
                17,
                WHITE
        );

        /*
         * =========================
         * 현재 게임 시간
         * =========================
         */

        double currentTime =
                game.getElapsedTicks() + partialTick;

        /*
         * =========================
         * 노드 출력
         * =========================
         *
         * 일반 노드는 원래 위치에 출력한다.
         *
         * 빠른 연타 노드는
         * 뒤쪽부터 앞쪽으로 그려
         * 쌓인 느낌을 만든다.
         */
        renderStackedNotes(
                graphics,
                game,
                config,
                width,
                hitX,
                laneY,
                currentTime,
                pixelsPerTick
        );

        /*
         * =========================
         * 현재 라운드
         * =========================
         */

        int currentRound = Math.min(
                game.getCompletedRounds() + 1,
                game.getTotalRounds()
        );

        graphics.drawString(
                font,
                "ROUND "
                        + currentRound
                        + " / "
                        + game.getTotalRounds(),
                10,
                10,
                WHITE
        );

        /*
         * =========================
         * HIT / MISS 표시
         * =========================
         */

        graphics.drawString(
                font,
                "HIT "
                        + game.getHitCount()
                        + "   MISS "
                        + game.getMissCount(),
                10,
                height - 17,
                WHITE
        );

        /*
         * READY / COUNTDOWN / FINISHED
         * 화면 메시지
         */
        graphics.drawCenteredString(
                font,
                message,
                width / 2,
                Math.min(height - 35, laneY + 36),
                WHITE
        );
    }

    /*
     * =========================
     * 연타 스택 묶음 출력
     * =========================
     *
     * 같은 라운드에서
     * 같은 색상의 노드가 짧은 시간 간격으로
     * 이어지면 하나의 스택으로 취급한다.
     *
     * 흰색 SPACE 노드는 묶지 않는다.
     *
     * 이미 HIT / MISS된 노드도
     * 그룹 경계 계산에는 포함한다.
     *
     * 따라서 앞쪽 노드가 사라지더라도
     * 남은 노드가 엉뚱한 그룹에 합쳐지지 않는다.
     */
    private static void renderStackedNotes(
            GuiGraphics graphics,
            WeavingGame game,
            WeavingConfig config,
            int width,
            int hitX,
            int laneY,
            double currentTime,
            double pixelsPerTick
    ) {

        List<WeavingNote> notes = game.getNotes();

        /*
         * 현재 기본 정박의 절반 이하인 간격을
         * 연속 입력 후보로 판단한다.
         *
         * 현재 정박 = 12틱
         * 현재 연타 = 3틱
         *
         * 따라서 3틱 연타는 스택으로 처리하고
         * 12틱 일반 노드는 따로 그린다.
         */
        int stackGapTicks = Math.max(
                1,
                config.getNoteIntervalTicks() / 2
        );

        int groupStart = 0;

        /*
         * 전체 노드를 앞에서부터 검사한다.
         */
        while (groupStart < notes.size()) {

            WeavingNote first = notes.get(groupStart);

            int groupEnd = groupStart + 1;

            /*
             * 흰색 노드는 독립 노드다.
             *
             * 파랑 / 빨강에 대해서만
             * 연타 묶음 여부를 확인한다.
             */
            if (first.getType()
                    != WeavingNote.Type.SPACE) {

                while (groupEnd < notes.size()) {

                    WeavingNote previous =
                            notes.get(groupEnd - 1);

                    WeavingNote candidate =
                            notes.get(groupEnd);

                    /*
                     * 색상이 다르면 다른 묶음.
                     */
                    if (candidate.getType()
                            != first.getType()) {
                        break;
                    }

                    /*
                     * 라운드가 다르면 다른 묶음.
                     */
                    if (candidate.getRoundNumber()
                            != first.getRoundNumber()) {
                        break;
                    }

                    /*
                     * 실제 목표 시간 차이.
                     */
                    int gap =
                            candidate.getTargetTick()
                                    - previous.getTargetTick();

                    /*
                     * 간격이 넓으면 일반 노드다.
                     */
                    if (gap <= 0
                            || gap > stackGapTicks) {
                        break;
                    }

                    groupEnd++;
                }
            }

            /*
             * 묶음 하나를 출력한다.
             */
            renderNoteGroup(
                    graphics,
                    notes,
                    groupStart,
                    groupEnd,
                    width,
                    hitX,
                    laneY,
                    currentTime,
                    pixelsPerTick
            );

            /*
             * 다음 묶음으로 이동한다.
             */
            groupStart = groupEnd;
        }
    }

    /*
     * =========================
     * 노드 묶음 하나 그리기
     * =========================
     *
     * 나중에 눌러야 하는 노드부터 먼저 그린다.
     *
     * 맨 앞 노드를 마지막에 그리면
     * 다른 원이 앞쪽 원을 덮지 못하므로
     * 실제 토큰이 쌓인 것처럼 보인다.
     */
    private static void renderNoteGroup(
            GuiGraphics graphics,
            List<WeavingNote> notes,
            int groupStart,
            int groupEnd,
            int width,
            int hitX,
            int laneY,
            double currentTime,
            double pixelsPerTick
    ) {

        /*
         * 아직 처리되지 않은 노드 중
         * 가장 먼저 눌러야 하는 노드를 찾는다.
         */
        int frontIndex = -1;

        for (
                int i = groupStart;
                i < groupEnd;
                i++
        ) {

            if (notes.get(i).isWaiting()) {
                frontIndex = i;
                break;
            }
        }

        /*
         * 묶음 전체가 처리됐다면
         * 화면에 그릴 노드가 없다.
         */
        if (frontIndex < 0) {
            return;
        }

        /*
         * 뒤쪽 노드부터 앞쪽 노드 순서로 출력.
         */
        for (
                int i = groupEnd - 1;
                i >= frontIndex;
                i--
        ) {

            WeavingNote note = notes.get(i);

            if (!note.isWaiting()) {
                continue;
            }

            /*
             * 노드의 X좌표는 원래 판정 시간을 따른다.
             *
             * 스택을 만들려고 X좌표를 강제로 줄이면
             * 화면상 노드 위치와 판정 시간이
             * 어긋날 수 있으므로 변경하지 않는다.
             */
            int noteX = (int) Math.round(
                    hitX
                            + (note.getTargetTick() - currentTime)
                            * pixelsPerTick
            );

            /*
             * 화면 바깥 노드는 출력하지 않는다.
             */
            if (noteX < -20 || noteX > width + 20) {
                continue;
            }

            /*
             * 가장 먼저 입력할 노드는
             * 원래 판정선 높이를 유지한다.
             *
             * 그 뒤에 있는 노드는
             * 하나씩 위로 올라간다.
             *
             * 앞의 노드가 처리되면
             * 다음 노드가 새로운 맨 앞이 된다.
             */
            int layer = i - frontIndex;

            int drawY =
                    laneY - layer * STACK_OFFSET_Y;

            /*
             * 노드 종류에 따른 색상.
             */
            int color = switch (note.getType()) {
                case LEFT -> BLUE;
                case RIGHT -> RED;
                case SPACE -> WHITE;
            };

            /*
             * =========================
             * 노드 본체
             * =========================
             */
            drawFilledCircle(
                    graphics,
                    noteX,
                    drawY,
                    NOTE_RADIUS,
                    color
            );

            /*
             * =========================
             * 흰색 외곽선
             * =========================
             *
             * 노드마다 개별적으로 그린다.
             *
             * 뒤에 있는 노드를 먼저 그리고
             * 앞에 있는 노드를 나중에 그리므로
             * 외곽선도 자연스럽게 겹쳐진다.
             */
            drawCircleOutline(
                    graphics,
                    noteX,
                    drawY,
                    NOTE_OUTLINE_RADIUS,
                    WHITE
            );
        }
    }

    /*
     * =========================
     * 원형 노드 본체 출력
     * =========================
     *
     * 기존 원형 출력 방식을 그대로 사용한다.
     */
    private static void drawFilledCircle(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {

        for (int y = -radius; y <= radius; y++) {

            int halfWidth = (int) Math.sqrt(
                    radius * radius - y * y
            );

            graphics.fill(
                    centerX - halfWidth,
                    centerY + y,
                    centerX + halfWidth + 1,
                    centerY + y + 1,
                    color
            );
        }
    }

    /*
     * =========================
     * 원형 흰색 테두리 출력
     * =========================
     *
     * 기존 판정기에서도 사용한다.
     */
    private static void drawCircleOutline(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {

        for (int y = -radius; y <= radius; y++) {

            int halfWidth = (int) Math.sqrt(
                    radius * radius - y * y
            );

            graphics.fill(
                    centerX - halfWidth,
                    centerY + y,
                    centerX - halfWidth + 1,
                    centerY + y + 1,
                    color
            );

            graphics.fill(
                    centerX + halfWidth,
                    centerY + y,
                    centerX + halfWidth + 1,
                    centerY + y + 1,
                    color
            );
        }
    }
}