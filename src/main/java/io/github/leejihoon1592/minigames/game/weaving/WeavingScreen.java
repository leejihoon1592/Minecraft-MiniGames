package io.github.leejihoon1592.minigames.game.weaving;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * 천 배짜기 게임 화면.
 *
 * 입력과 화면 실행 상태만 관리한다.
 * 게임 규칙은 WeavingGame,
 * 출력은 WeavingRenderer가 담당한다.
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
                    Component.literal("Left Click to Start");

            case COUNTDOWN ->
                    Component.literal(
                            "START IN "
                                    + Math.max(
                                    1,
                                    (countdownTicks + 19) / 20
                            )
                    );

            case PLAYING ->
                    Component.literal(
                            "LEFT / RIGHT / SPACE"
                    );

            case FINISHED ->
                    Component.literal(
                            "FINISHED - Click to Close"
                    );
        };

        /*
         * 렌더러는 화면만 그린다.
         *
         * 준비 및 결과 화면에서는
         * 노트 위치를 정지시킨다.
         */
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
     * 마우스 입력
     * =========================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button != 0 && button != 1) {
            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (state == State.READY) {

            countdownTicks = COUNTDOWN_TICKS;
            state = State.COUNTDOWN;

            return true;
        }

        if (state == State.COUNTDOWN) {
            return true;
        }

        if (state == State.FINISHED) {

            this.onClose();
            return true;
        }

        WeavingNote.Type inputType =
                button == 0
                        ? WeavingNote.Type.LEFT
                        : WeavingNote.Type.RIGHT;

        game.submit(inputType);

        if (game.isFinished()) {
            state = State.FINISHED;
        }

        return true;
    }

    /*
     * =========================
     * 스페이스바 입력
     * =========================
     */

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        if (keyCode == GLFW.GLFW_KEY_SPACE) {

            if (state == State.PLAYING) {

                game.submit(
                        WeavingNote.Type.SPACE
                );

                if (game.isFinished()) {
                    state = State.FINISHED;
                }
            }

            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    /*
     * 게임 화면을 열어도
     * 싱글플레이 월드가 일시 정지되지 않는다.
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}