package io.github.leejihoon1592.minigames.game.weaving;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.leejihoon1592.minigames.game.weaving.client.WeavingKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

/**
 * 천 배짜기 게임의 화면 렌더러.
 *
 * 1. 기존 천/베틀 애니메이션을 그대로 호출한다.
 * 2. 판정기의 색상 원을 실제 판정 시간과 연결한다.
 * 3. 파란색/빨간색 연타 노드를 가로로 쌓아 출력한다.
 * 4. 노드 안에는 현재 마인크래프트 키 설정을 표시한다.
 * 5. 현재 점수와 가장 최근 판정 결과를 표시한다.
 *
 * 실제 HIT/MISS, 점수 계산 및 입력 처리는 WeavingGame이 담당한다.
 */
public final class WeavingRenderer {

    private static final int BLUE = 0xFF3549CD;
    private static final int RED = 0xFFE91E29;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF111111;

    /*
     * 사용자가 정한 3단계 판정 원의 색상.
     * Fantastic은 노드보다 큰 안쪽 원, Perfect와 Good은 바깥쪽 원이다.
     */
    private static final int FANTASTIC_COLOR = 0xFF38BDF8;
    private static final int PERFECT_COLOR = 0xFFF4CE34;
    private static final int GOOD_COLOR = 0xFF29C85C;

    private static final int NOTE_RADIUS = 15;
    private static final int NOTE_OUTLINE_RADIUS = 16;

    /*
     * 연속된 파랑/빨강 노드는 같은 높이에 가로로 겹쳐 놓는다.
     * 가장 먼저 눌러야 하는 노드가 제일 앞에 보인다.
     */
    private static final int STACK_OFFSET_X = 12;

    /*
     * 판정 직후 등급을 잠시 화면에 보여주는 시간 (12틱 = 약 0.6초).
     */
    private static final int FEEDBACK_TICKS = 12;

    private WeavingRenderer() {
    }

    /**
     * 게임 화면 전체 출력.
     * 기존 렌더링 진입점의 인자를 바꾸지 않는다.
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

        /* 기존 천 제작 및 베틀 애니메이션은 변경하지 않는다. */
        WeavingClothRenderer.render(
                graphics, width, height, game, config, partialTick
        );
        WeavingLoomRenderer.render(
                graphics, width, height, game, partialTick
        );

        /*
         * 현재 원래 판정선 X좌표와 노드 이동 속도.
         * 노드의 시각적 판정 위치도 같은 값으로 계산한다.
         */
        int hitX = Math.max(50, Math.min(85, width / 5));
        int laneY = (int) (height * 0.76);
        double pixelsPerTick =
                (double) (width - hitX - 14) / config.getApproachTicks();

        /*
         * 판정 원의 시각적 반지름을
         * 기존보다 각각 1픽셀 줄인다.
         *
         * 판정 시간과 점수는 변경하지 않는다.
         */
        /*
         * 화면에 보이는 판정 원 크기는 기존대로 유지한다.
         *
         * 실제 판정 범위는 WeavingGame에서 별도로 계산한다.
         * 화면 원: 4 / 6 / 8
         * 실제 판정: 2 / 3 / 4
         */
        int fantasticRadius = (int) Math.round(
                4 * pixelsPerTick
        ) - 1;

        int perfectRadius = (int) Math.round(
                6 * pixelsPerTick
        ) - 1;

        int goodRadius = (int) Math.round(
                8 * pixelsPerTick
        ) - 1;

        /*
         * 기존 판정기 중심 세로선은 그대로 둔다.
         */
        graphics.fill(
                hitX - 1, laneY - 29,
                hitX + 1, laneY + 29,
                0xFFD4B65A
        );

        /*
         * 가장 큰 Good부터 순서대로 그린다.
         * 원들은 중심에서 같은 거리인 X축 위치를 판정한다.
         */
        drawCircleOutline(graphics, hitX, laneY, goodRadius, GOOD_COLOR);
        drawCircleOutline(graphics, hitX, laneY, perfectRadius, PERFECT_COLOR);
        drawCircleOutline(graphics, hitX, laneY, fantasticRadius, FANTASTIC_COLOR);

        double currentTime = game.getElapsedTicks() + partialTick;

        /*
         * 기존 가로 일자 스택의 순서와 위치 관계를 유지한다.
         * 여기에 현재 실제 입력키 글자를 추가한다.
         */
        renderStackedNotes(
                graphics, font, game, config, width, hitX, laneY,
                currentTime, pixelsPerTick
        );

        /* 화면 상단 라운드와 임시 점수 */
        int currentRound = Math.min(
                game.getCompletedRounds() + 1,
                game.getTotalRounds()
        );
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.minigames.weaving.round",
                        currentRound,
                        game.getTotalRounds()
                ),
                10, 10, WHITE,
                false
        );

        Component scoreLine = Component.translatable(
                "screen.minigames.weaving.score",
                game.getTotalScore(),
                game.getMaximumScore()
        );
        graphics.drawString(
                font, scoreLine,
                Math.max(10, width - font.width(scoreLine) - 10),
                10, WHITE,
                false
        );

        /* 기존 하단 HIT / MISS 안내도 유지한다. */
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.minigames.weaving.hit_miss",
                        game.getHitCount(),
                        game.getMissCount()
                ),
                10, height - 17, WHITE,
                false
        );

        /*
         * 가장 최근 판정을 판정기 바로 위에 잠시 표시한다.
         * 최근 판정 데이터는 WeavingGame에서 이미 기록 중이다.
         */
        WeavingGame.Judgement judgement = game.getLastJudgement();
        int judgementTick = game.getLastJudgementTick();
        if (judgement != WeavingGame.Judgement.NONE
                && judgementTick >= 0
                && currentTime - judgementTick < FEEDBACK_TICKS) {

            int feedbackColor = switch (judgement) {
                case FANTASTIC -> FANTASTIC_COLOR;
                case PERFECT -> PERFECT_COLOR;
                case GOOD -> GOOD_COLOR;
                case MISS -> RED;
                case NONE -> WHITE;
            };

            Component feedbackText = Component.translatable(
                    "screen.minigames.weaving."
                            + judgement.name().toLowerCase(Locale.ROOT)
            );
            /*
             * 판정 결과를 가운데 정렬해서 출력한다.
             *
             * 마지막 false는 글씨 그림자 제거 옵션이다.
             */
            graphics.drawString(
                    font,
                    feedbackText,
                    hitX - font.width(feedbackText) / 2,
                    Math.max(24, laneY - goodRadius - 13),
                    feedbackColor,
                    false
            );
        }

        /*
         * 게임 안내 메시지.
         *
         * 그림자 없이 가로 중앙에 정렬한다.
         */
        graphics.drawString(
                font,
                message,
                (width - font.width(message)) / 2,
                Math.min(height - 35, laneY + 36),
                WHITE,
                false
        );
    }

    /**
     * 노드 목록을 시간 순서대로 훑어서 같은 색의 빠른 연타를 묶는다.
     * 처리된 노드도 묶음 경계 판별에 포함한다.
     */
    private static void renderStackedNotes(
            GuiGraphics graphics,
            Font font,
            WeavingGame game,
            WeavingConfig config,
            int width,
            int hitX,
            int laneY,
            double currentTime,
            double pixelsPerTick
    ) {
        List<WeavingNote> notes = game.getNotes();
        int stackGapTicks = Math.max(1, config.getNoteIntervalTicks() / 2);
        int groupStart = 0;

        while (groupStart < notes.size()) {
            WeavingNote first = notes.get(groupStart);
            int groupEnd = groupStart + 1;

            if (first.getType() != WeavingNote.Type.SPACE) {
                while (groupEnd < notes.size()) {
                    WeavingNote previous = notes.get(groupEnd - 1);
                    WeavingNote candidate = notes.get(groupEnd);

                    if (candidate.getType() != first.getType()
                            || candidate.getRoundNumber() != first.getRoundNumber()) {
                        break;
                    }

                    int gap = candidate.getTargetTick() - previous.getTargetTick();
                    if (gap <= 0 || gap > stackGapTicks) {
                        break;
                    }
                    groupEnd++;
                }
            }

            renderNoteGroup(
                    graphics, font, notes, groupStart, groupEnd,
                    width, hitX, laneY, currentTime, pixelsPerTick
            );
            groupStart = groupEnd;
        }
    }

    /**
     * 같은 색 연타를 한 줄로 쌓아 출력한다.
     * 가장 먼저 눌러야 하는 노드를 맨 앞(왼쪽)에 그린다.
     */
    private static void renderNoteGroup(
            GuiGraphics graphics,
            Font font,
            List<WeavingNote> notes,
            int groupStart,
            int groupEnd,
            int width,
            int hitX,
            int laneY,
            double currentTime,
            double pixelsPerTick
    ) {
        int frontIndex = -1;
        for (int i = groupStart; i < groupEnd; i++) {
            if (notes.get(i).isWaiting()) {
                frontIndex = i;
                break;
            }
        }
        if (frontIndex < 0) {
            return;
        }

        /* 가장 앞에서 판정할 노드만 실제 시간축의 X좌표에 놓는다. */
        int frontNoteX = (int) Math.round(
                hitX + (notes.get(frontIndex).getTargetTick() - currentTime)
                        * pixelsPerTick
        );

        /* 뒷노드부터 그려야 앞의 노드가 겹쳐서 보인다. */
        for (int i = groupEnd - 1; i >= frontIndex; i--) {
            WeavingNote note = notes.get(i);
            if (!note.isWaiting()) {
                continue;
            }

            int layer = i - frontIndex;
            int noteX = frontNoteX + layer * STACK_OFFSET_X;
            if (noteX < -40 || noteX > width + 40) {
                continue;
            }

            int color = switch (note.getType()) {
                case LEFT -> BLUE;
                case RIGHT -> RED;
                case SPACE -> WHITE;
            };

            drawFilledCircle(graphics, noteX, laneY, NOTE_RADIUS, color);

            /* 흰색 노드에는 테두리를 그리지 않는다. */
            if (note.getType() != WeavingNote.Type.SPACE) {
                drawCircleOutline(
                        graphics, noteX, laneY,
                        NOTE_OUTLINE_RADIUS, WHITE
                );
            }

            /*
             * 각 노드 중앙에 현재 할당된 입력키를 표시한다.
             * 원이 겹쳐진 경우 뒤 노드 글자도 일부 가려지는 것이 정상이다.
             */
            String keyLabel = getInputLabel(note.getType());
            int textColor = note.getType() == WeavingNote.Type.SPACE
                    ? BLACK : WHITE;
            drawNodeLabel(graphics, font, keyLabel, noteX, laneY, textColor);
        }
    }

    /**
     * 현재 마인크래프트 설정에서 파랑/빨강/흰색의 키를 읽어온다.
     * 특정 화살표나 LC/RC를 고정하지 않는다.
     */
    private static String getInputLabel(WeavingNote.Type type) {
        KeyMapping mapping = switch (type) {
            case LEFT -> WeavingKeyMappings.BLUE_INPUT;
            case RIGHT -> WeavingKeyMappings.RED_INPUT;
            case SPACE -> WeavingKeyMappings.WHITE_INPUT;
        };
        InputConstants.Key key = mapping.getKey();
        int value = key.getValue();

        /* 마우스인 경우 실제 지정된 버튼 번호를 표기한다. */
        if (key.getType() == InputConstants.Type.MOUSE) {
            return switch (value) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "LC";
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "RC";
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "MC";
                default -> "M" + (value + 1);
            };
        }

        /* 방향키인 경우 사용자가 보여준 것처럼 실제 화살표를 표기한다. */
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return switch (value) {
                case GLFW.GLFW_KEY_LEFT -> "←";
                case GLFW.GLFW_KEY_RIGHT -> "→";
                case GLFW.GLFW_KEY_UP -> "↑";
                case GLFW.GLFW_KEY_DOWN -> "↓";
                case GLFW.GLFW_KEY_SPACE -> "SPC";
                case GLFW.GLFW_KEY_UNKNOWN -> "?";
                default -> abbreviateKey(key.getDisplayName().getString());
            };
        }

        return abbreviateKey(key.getDisplayName().getString());
    }

    /** 긴 입력키 이름은 노드 안에 표시할 수 있도록 줄인다. */
    private static String abbreviateKey(String keyName) {
        String text = keyName.toUpperCase(Locale.ROOT).trim();
        return text.length() <= 5 ? text : text.substring(0, 5);
    }

    /** 노드 내부의 글자를 크기에 맞게 줄여 중앙에 표시한다. */
    private static void drawNodeLabel(
            GuiGraphics graphics,
            Font font,
            String label,
            int centerX,
            int centerY,
            int color
    ) {
        float scale = Math.min(
                1.0F,
                23.0F / Math.max(1, font.width(label))
        );

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        /*
         * 실제 할당된 키 이름을 노드 중앙에 출력.
         * 그림자는 사용하지 않는다.
         */
        graphics.drawString(
                font,
                label,
                -font.width(label) / 2,
                -4,
                color,
                false
        );
        graphics.pose().popPose();
    }

    /** 기존과 동일한 원형 노드 본체 출력. */
    private static void drawFilledCircle(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(
                    centerX - halfWidth,
                    centerY + y,
                    centerX + halfWidth + 1,
                    centerY + y + 1,
                    color
            );
        }
    }

    /** 기존 판정기에 있던 원형 테두리 출력. */
    private static void drawCircleOutline(
            GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
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
