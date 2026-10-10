package io.github.leejihoon1592.minigames.game.weaving;

import io.github.leejihoon1592.minigames.game.weaving.client.WeavingKeyMappings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * 천 배짜기 게임 화면.
 *
 * 담당:
 * - 화면 상태
 * - 사용자 입력 전달
 * - 현재 설정된 조작키 표시
 *
 * 판정: WeavingGame
 * 렌더링: WeavingRenderer
 * 키 설정: WeavingKeyMappings
 */
public final class WeavingScreen extends Screen {

    private enum State {
        READY,
        COUNTDOWN,
        PLAYING,
        FINISHED
    }

    private static final int COUNTDOWN_TICKS = 60;

    private final WeavingConfig config;
    private final WeavingGame game;

    private State state = State.READY;

    private int countdownTicks = COUNTDOWN_TICKS;

    public WeavingScreen() {

        super(Component.literal("Weaving Game"));

        this.config =
                WeavingConfig.defaultConfig();

        this.game =
                new WeavingGame(
                        WeavingPattern.createPracticeNotes(config)
                );
    }

    /*
     * =========================
     * 게임 시간 진행
     * =========================
     */

    @Override
    public void tick() {

        if (state == State.COUNTDOWN) {

            countdownTicks--;

            if (countdownTicks <= 0) {
                state = State.PLAYING;
            }

            return;
        }

        /*
         * 게임 종료 이후에도
         * 마지막 베틀 애니메이션을 위해
         * 화면 시간을 계속 진행한다.
         */
        if (state == State.FINISHED) {

            game.tick();
            return;
        }

        if (state != State.PLAYING) {
            return;
        }

        game.tick();

        if (game.isFinished()) {
            state = State.FINISHED;
        }
    }

    /*
     * =========================
     * 화면 출력
     * =========================
     */

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        Component message = switch (state) {

            case READY ->
                    Component.literal(
                            "Click or SPACE to start"
                    );

            case COUNTDOWN ->
                    Component.literal(
                            "START IN "
                                    + Math.max(
                                    1,
                                    (countdownTicks + 19) / 20
                            )
                    );

            /*
             * 현재 실제 키 설정을 읽는다.
             *
             * 마우스 좌클릭 -> D키로 바꾸었다면
             * BLUE 안내에도 D가 표시된다.
             */
            case PLAYING ->
                    Component.literal("BLUE [")
                            .append(
                                    WeavingKeyMappings.BLUE_INPUT
                                            .getTranslatedKeyMessage()
                            )
                            .append("]   RED [")
                            .append(
                                    WeavingKeyMappings.RED_INPUT
                                            .getTranslatedKeyMessage()
                            )
                            .append("]   WHITE [")
                            .append(
                                    WeavingKeyMappings.WHITE_INPUT
                                            .getTranslatedKeyMessage()
                            )
                            .append("]");

            case FINISHED ->
                    Component.literal(
                            "FINISHED - Click to Close"
                    );
        };

        double animationTick =
                (state == State.PLAYING
                        || state == State.FINISHED)
                        ? partialTick
                        : 0.0;

        WeavingRenderer.render(
                graphics,
                this.font,
                this.width,
                this.height,
                game,
                config,
                animationTick,
                message
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    /*
     * =========================
     * 카운트다운 시작
     * =========================
     */

    private void startCountdown() {

        countdownTicks = COUNTDOWN_TICKS;
        state = State.COUNTDOWN;
    }

    /*
     * =========================
     * 노트 입력 전달
     * =========================
     *
     * 입력 방식과 노트 판정을 분리한다.
     *
     * 키보드 D, 마우스 좌클릭 등
     * 무엇으로 입력하든
     * 파란 노트라면 LEFT를 전달한다.
     */

    private void submitInput(WeavingNote.Type type) {

        game.submit(type);

        if (game.isFinished()) {
            state = State.FINISHED;
        }
    }

    /*
     * =========================
     * 마우스 입력
     * =========================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        /*
         * 준비 상태:
         * 클릭하면 게임 시작
         */
        if (state == State.READY) {

            startCountdown();
            return true;
        }

        if (state == State.COUNTDOWN) {
            return true;
        }

        /*
         * 종료 상태:
         * 클릭하면 화면 닫기
         */
        if (state == State.FINISHED) {

            this.onClose();
            return true;
        }

        /*
         * 등록된 파란 노트 조작이
         * 현재 마우스 버튼과 일치하는가?
         */
        if (WeavingKeyMappings.BLUE_INPUT
                .matchesMouse(button)) {

            submitInput(WeavingNote.Type.LEFT);
            return true;
        }

        /*
         * 등록된 빨간 노트 조작이
         * 현재 마우스 버튼과 일치하는가?
         */
        if (WeavingKeyMappings.RED_INPUT
                .matchesMouse(button)) {

            submitInput(WeavingNote.Type.RIGHT);
            return true;
        }

        /*
         * =========================
         * 흰색 노트 - 베틀 누르기
         * =========================
         *
         * 마우스 버튼으로 설정된 경우에도
         * 해당 버튼으로 흰색 노트를 처리한다.
         */

        if (WeavingKeyMappings.WHITE_INPUT
                .matchesMouse(button)) {

            submitInput(WeavingNote.Type.SPACE);
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    /*
     * =========================
     * 키보드 입력
     * =========================
     */

    /*
     * =========================
     * 키보드 입력 처리
     * =========================
     *
     * 게임 시작:
     * 스페이스바 고정
     *
     * 게임 진행:
     * 사용자가 설정한 키 사용
     */

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        /*
         * 현재 설정된 입력키 확인
         */

        boolean bluePressed =
                WeavingKeyMappings.BLUE_INPUT.matches(
                        keyCode,
                        scanCode
                );

        boolean redPressed =
                WeavingKeyMappings.RED_INPUT.matches(
                        keyCode,
                        scanCode
                );

        boolean whitePressed =
                WeavingKeyMappings.WHITE_INPUT.matches(
                        keyCode,
                        scanCode
                );

        /*
         * =========================
         * 게임 시작
         * =========================
         *
         * 흰색 노트의 키 설정과 관계없이
         * 스페이스바로 게임을 시작한다.
         *
         * 마우스 클릭으로 시작하는 기능은
         * mouseClicked()에서 처리한다.
         */

        if (state == State.READY) {

            if (keyCode == GLFW.GLFW_KEY_SPACE) {

                startCountdown();
                return true;
            }
        }

        /*
         * =========================
         * 카운트다운
         * =========================
         *
         * 카운트다운 중에는
         * 노트 입력을 처리하지 않는다.
         */

        if (state == State.COUNTDOWN) {

            if (bluePressed
                    || redPressed
                    || whitePressed
                    || keyCode == GLFW.GLFW_KEY_SPACE) {

                return true;
            }
        }

        /*
         * =========================
         * 실제 게임 진행
         * =========================
         */

        if (state == State.PLAYING) {

            /*
             * 흰색 노트
             *
             * 이제 고정 스페이스바가 아니라
             * 현재 설정된 키로 판정한다.
             */

            if (whitePressed) {

                submitInput(WeavingNote.Type.SPACE);
                return true;
            }

            /*
             * 파란색 노트
             */

            if (bluePressed) {

                submitInput(WeavingNote.Type.LEFT);
                return true;
            }

            /*
             * 빨간색 노트
             */

            if (redPressed) {

                submitInput(WeavingNote.Type.RIGHT);
                return true;
            }
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}