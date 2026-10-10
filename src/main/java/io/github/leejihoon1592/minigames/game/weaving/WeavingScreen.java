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
                            "Click or configured key to start"
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
                            .append("]   WHITE [SPACE]");

            case FINISHED ->
                    Component.literal(
                            "FINISHED - Click to Close"
                    );
        };

        double animationTick =
                state == State.PLAYING
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

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

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

        /*
         * 준비 상태에서는 설정된 키나
         * 스페이스바로 시작할 수 있다.
         */
        if (state == State.READY) {

            if (bluePressed
                    || redPressed
                    || keyCode == GLFW.GLFW_KEY_SPACE) {

                startCountdown();
                return true;
            }
        }

        /*
         * 카운트다운 도중 입력은 무시한다.
         */
        if (state == State.COUNTDOWN) {

            if (bluePressed
                    || redPressed
                    || keyCode == GLFW.GLFW_KEY_SPACE) {

                return true;
            }
        }

        /*
         * 실제 게임 플레이
         */
        if (state == State.PLAYING) {

            /*
             * 흰 노트는 기존대로
             * 스페이스바를 사용한다.
             */
            if (keyCode == GLFW.GLFW_KEY_SPACE) {

                submitInput(WeavingNote.Type.SPACE);
                return true;
            }

            /*
             * 파란 노트:
             * 설정된 키보드 입력
             */
            if (bluePressed) {

                submitInput(WeavingNote.Type.LEFT);
                return true;
            }

            /*
             * 빨간 노트:
             * 설정된 키보드 입력
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