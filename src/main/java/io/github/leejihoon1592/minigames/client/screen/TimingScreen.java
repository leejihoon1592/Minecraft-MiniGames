package io.github.leejihoon1592.minigames.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimingScreen extends Screen {

    public TimingScreen() {
        super(Component.literal("Timing Game"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 기존 Minecraft 화면 배경
        this.renderBackground(graphics);

        // 화면 중앙 좌표
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 테스트용 타이밍 바
        int barWidth = 300;
        int barHeight = 20;

        int barX = centerX - barWidth / 2;
        int barY = centerY - barHeight / 2;

        // 타이밍 바 배경
        graphics.fill(
                barX,
                barY,
                barX + barWidth,
                barY + barHeight,
                0xFF555555
        );

        // 가운데 판정 영역
        int targetWidth = 60;
        int targetX = centerX - targetWidth / 2;

        graphics.fill(
                targetX,
                barY,
                targetX + targetWidth,
                barY + barHeight,
                0xFF55AA55
        );

        // 제목
        graphics.drawCenteredString(
                this.font,
                "Timing Game",
                centerX,
                barY - 30,
                0xFFFFFFFF
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}