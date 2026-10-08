package io.github.leejihoon1592.minigames.game.flight;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class FlightScreen extends Screen {

    /*
     * =========================
     * 화면 설정
     * =========================
     */

    private static final int PLAYER_X_OFFSET = 120;

    private static final int THREAD_WIDTH = 12;
    private static final int THREAD_HEIGHT = 12;

    /*
     * =========================
     * 임시 물리값
     * =========================
     */

    private static final double GRAVITY = 0.18;
    private static final double HOLD_FORCE = 0.35;

    private static final double MAX_FALL_SPEED = 4.0;
    private static final double MAX_RISE_SPEED = -4.0;

    /*
     * =========================
     * 실 상태
     * =========================
     */

    private double threadY;
    private double previousThreadY;
    private double velocityY;

    public FlightScreen() {
        super(Component.literal("Flight Game"));
    }

    @Override
    protected void init() {

        this.threadY =
                this.height / 2.0;

        this.previousThreadY =
                this.threadY;

        this.velocityY =
                0.0;
    }

    @Override
    public void tick() {

        /*
         * 렌더 보간용
         * 현재 위치를 갱신하기 전에
         * 이전 위치를 저장한다.
         */

        previousThreadY =
                threadY;

        /*
         * 좌클릭을 누르고 있는 동안
         * 계속 위로 힘을 준다.
         */

        long window =
                this.minecraft
                        .getWindow()
                        .getWindow();

        boolean holdingLeftMouse =
                GLFW.glfwGetMouseButton(
                        window,
                        GLFW.GLFW_MOUSE_BUTTON_LEFT
                )
                        == GLFW.GLFW_PRESS;

        if (holdingLeftMouse) {

            velocityY -=
                    HOLD_FORCE;
        }

        /*
         * 중력
         */

        velocityY +=
                GRAVITY;

        velocityY =
                Mth.clamp(
                        velocityY,
                        MAX_RISE_SPEED,
                        MAX_FALL_SPEED
                );

        /*
         * Y 이동
         */

        threadY +=
                velocityY;

        /*
         * 화면 위 / 아래 제한
         */

        double minY =
                20.0;

        double maxY =
                this.height
                        - THREAD_HEIGHT
                        - 20.0;

        if (threadY < minY) {

            threadY =
                    minY;

            velocityY =
                    0.0;
        }

        if (threadY > maxY) {

            threadY =
                    maxY;

            velocityY =
                    0.0;
        }
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        /*
         * 배경
         */

        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                0xFF202020
        );

        /*
         * 비행 가능 범위 표시
         */

        int top =
                20;

        int bottom =
                this.height - 20;

        graphics.fill(
                0,
                top,
                this.width,
                top + 2,
                0xFF666666
        );

        graphics.fill(
                0,
                bottom - 2,
                this.width,
                bottom,
                0xFF666666
        );

        /*
         * 실
         *
         * 현재는 테스트용 흰색 사각형.
         */

        int threadX =
                PLAYER_X_OFFSET;

        /*
         * 이전 Tick 위치와
         * 현재 Tick 위치 사이를 보간해서
         * 화면 움직임을 부드럽게 만든다.
         */

        double interpolatedY =
                Mth.lerp(
                        partialTick,
                        previousThreadY,
                        threadY
                );

        int renderY =
                (int) Math.round(
                        interpolatedY
                );

        graphics.fill(
                threadX,
                renderY,
                threadX + THREAD_WIDTH,
                renderY + THREAD_HEIGHT,
                0xFFFFFFFF
        );

        /*
         * 테스트 정보
         */

        graphics.drawString(
                this.font,
                "Y : "
                        + String.format(
                        "%.1f",
                        threadY
                ),
                10,
                10,
                0xFFFFFFFF
        );

        graphics.drawString(
                this.font,
                "Velocity : "
                        + String.format(
                        "%.2f",
                        velocityY
                ),
                10,
                22,
                0xFFFFFFFF
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}