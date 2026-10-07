package io.github.leejihoon1592.minigames.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TimingScreen extends Screen {

    private static final int BAR_WIDTH = 600;
    private static final int BAR_HEIGHT = 20;

    private static final int TARGET_WIDTH = 120;

    private static final int POINTER_WIDTH = 4;
    private static final int POINTER_HEIGHT = 32;

    // 포인터가 바 전체를 한쪽 끝에서 반대쪽 끝까지 이동하는 시간
    private static final double TRAVEL_TIME_SECONDS = 1.5;

    private long startTime;

    public TimingScreen() {
        super(Component.literal("Timing Game"));
    }

    @Override
    protected void init() {
        super.init();

        // 화면이 열린 순간을 게임 시작 시각으로 저장
        this.startTime = System.nanoTime();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 작은 해상도에서도 화면 밖으로 나가지 않도록 제한
        int barWidth = Math.min(BAR_WIDTH, this.width - 40);

        int barX = centerX - barWidth / 2;
        int barY = centerY - BAR_HEIGHT / 2;

        /*
         * =========================
         * 타이밍 바
         * =========================
         */

        graphics.fill(
                barX,
                barY,
                barX + barWidth,
                barY + BAR_HEIGHT,
                0xFF555555
        );

        /*
         * =========================
         * 판정 영역
         * =========================
         */

        int targetWidth = Math.min(TARGET_WIDTH, barWidth);

        int targetX = centerX - targetWidth / 2;

        graphics.fill(
                targetX,
                barY,
                targetX + targetWidth,
                barY + BAR_HEIGHT,
                0xFF55AA55
        );

        /*
         * =========================
         * 포인터 위치 계산
         * =========================
         */

        double elapsedSeconds =
                (System.nanoTime() - this.startTime) / 1_000_000_000.0;

        // 0 → 1 → 0 → 1 형태의 왕복값
        double cycle = elapsedSeconds / TRAVEL_TIME_SECONDS;
        double cyclePosition = cycle % 2.0;

        double progress;

        if (cyclePosition <= 1.0) {
            progress = cyclePosition;
        } else {
            progress = 2.0 - cyclePosition;
        }

        progress = Mth.clamp(progress, 0.0, 1.0);

        int pointerCenterX =
                barX + (int) Math.round(progress * barWidth);

        int pointerX =
                pointerCenterX - POINTER_WIDTH / 2;

        /*
         * =========================
         * 포인터 렌더링
         * =========================
         */

        graphics.fill(
                pointerX,
                barY - (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                pointerX + POINTER_WIDTH,
                barY + BAR_HEIGHT + (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 제목
         * =========================
         */

        graphics.drawCenteredString(
                this.font,
                "Timing Game",
                centerX,
                barY - 35,
                0xFFFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}