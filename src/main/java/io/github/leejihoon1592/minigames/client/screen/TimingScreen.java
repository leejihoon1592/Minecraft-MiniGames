package io.github.leejihoon1592.minigames.client.screen;

import io.github.leejihoon1592.minigames.game.timing.TimingConfig;
import io.github.leejihoon1592.minigames.game.timing.TimingGame;
import io.github.leejihoon1592.minigames.game.timing.TimingResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TimingScreen extends Screen {

    private static final int BAR_WIDTH = 600;
    private static final int BAR_HEIGHT = 20;

    private static final int POINTER_WIDTH = 4;
    private static final int POINTER_HEIGHT = 32;

    private static final double COUNTDOWN_SECONDS = 3.0;

    private final TimingConfig config;
    private final TimingGame game;

    private GameState state;

    private long countdownStartTime;
    private long gameStartTime;

    public TimingScreen() {
        this(TimingConfig.defaultConfig());
    }

    public TimingScreen(TimingConfig config) {
        super(Component.literal("Timing Game"));

        if (config == null) {
            throw new IllegalArgumentException(
                    "config must not be null"
            );
        }

        this.config = config;
        this.game = new TimingGame(config);

        this.state = GameState.READY;
    }

    /*
     * =========================
     * Screen 초기화
     *
     * 해상도 변경이나 전체화면 전환으로
     * init()이 다시 호출되어도
     * 게임 상태는 초기화하지 않는다.
     * =========================
     */

    @Override
    protected void init() {
        super.init();
    }

    /*
     * =========================
     * 화면 렌더링
     * =========================
     */

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        updateState();

        this.renderBackground(graphics);

        int centerX =
                this.width / 2;

        int centerY =
                this.height / 2;

        int barWidth =
                Math.min(
                        BAR_WIDTH,
                        Math.max(100, this.width - 40)
                );

        int barX =
                centerX - barWidth / 2;

        int barY =
                centerY - BAR_HEIGHT / 2;

        /*
         * =========================
         * 제목
         * =========================
         */

        graphics.drawCenteredString(
                this.font,
                "Timing Game",
                centerX,
                barY - 70,
                0xFFFFFFFF
        );

        /*
         * =========================
         * 상태별 상단 표시
         * =========================
         */

        switch (state) {

            case READY ->

                    graphics.drawCenteredString(
                            this.font,
                            "CLICK TO START",
                            centerX,
                            barY - 48,
                            0xFFFFFFFF
                    );

            case COUNTDOWN -> {

                int countdownNumber =
                        getCountdownNumber();

                graphics.drawCenteredString(
                        this.font,
                        Integer.toString(countdownNumber),
                        centerX,
                        barY - 48,
                        0xFFFFD700
                );
            }

            case PLAYING ->

                    graphics.drawCenteredString(
                            this.font,
                            "Round "
                                    + (game.getCurrentRound() + 1)
                                    + " / "
                                    + game.getMaxRounds(),
                            centerX,
                            barY - 48,
                            0xFFFFFFFF
                    );

            case FINISHED ->

                    graphics.drawCenteredString(
                            this.font,
                            "FINISHED",
                            centerX,
                            barY - 48,
                            0xFFFFD700
                    );
        }

        /*
         * =========================
         * 기본 바 - MISS
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

        double halfBarWidth =
                barWidth / 2.0;

        int goodHalfWidth =
                (int) Math.round(
                        config.getGoodRange()
                                * halfBarWidth
                );

        int perfectHalfWidth =
                (int) Math.round(
                        config.getPerfectRange()
                                * halfBarWidth
                );

        int fantasticHalfWidth =
                (int) Math.round(
                        config.getFantasticRange()
                                * halfBarWidth
                );

        /*
         * GOOD
         */

        graphics.fill(
                centerX - goodHalfWidth,
                barY,
                centerX + goodHalfWidth,
                barY + BAR_HEIGHT,
                0xFF55AA55
        );

        /*
         * PERFECT
         */

        graphics.fill(
                centerX - perfectHalfWidth,
                barY,
                centerX + perfectHalfWidth,
                barY + BAR_HEIGHT,
                0xFFE0B040
        );

        /*
         * FANTASTIC
         */

        graphics.fill(
                centerX - fantasticHalfWidth,
                barY,
                centerX + fantasticHalfWidth,
                barY + BAR_HEIGHT,
                0xFF55AAFF
        );

        /*
         * =========================
         * 포인터
         *
         * PLAYING 상태에서만 표시
         * =========================
         */

        if (state == GameState.PLAYING) {

            int pointerCenterX =
                    calculatePointerX(
                            barX,
                            barWidth
                    );

            int pointerX =
                    pointerCenterX
                            - POINTER_WIDTH / 2;

            graphics.fill(
                    pointerX,
                    barY
                            - (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                    pointerX + POINTER_WIDTH,
                    barY
                            + BAR_HEIGHT
                            + (POINTER_HEIGHT - BAR_HEIGHT) / 2,
                    0xFFFFFFFF
            );
        }

        /*
         * =========================
         * 진행 중 화면
         * =========================
         */

        if (state == GameState.PLAYING) {

            TimingGame.Judgement lastJudgement =
                    game.getLastJudgement();

            if (lastJudgement != null) {

                graphics.drawCenteredString(
                        this.font,
                        lastJudgement.getDisplayName(),
                        centerX,
                        barY + 38,
                        lastJudgement.getColor()
                );
            }

            graphics.drawCenteredString(
                    this.font,
                    "Total: "
                            + game.getTotalScore(),
                    centerX,
                    barY + 56,
                    0xFFFFFFFF
            );
        }

        /*
         * =========================
         * 최종 결과 화면
         * =========================
         */

        if (state == GameState.FINISHED) {

            TimingResult result =
                    game.getResult();

            TimingGame.Judgement lastJudgement =
                    game.getLastJudgement();

            if (lastJudgement != null) {

                graphics.drawCenteredString(
                        this.font,
                        lastJudgement.getDisplayName(),
                        centerX,
                        barY + 35,
                        lastJudgement.getColor()
                );
            }

            graphics.drawCenteredString(
                    this.font,
                    "Total Score: "
                            + result.getTotalScore()
                            + " / "
                            + result.getMaximumScore(),
                    centerX,
                    barY + 53,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    "Average: "
                            + String.format(
                            "%.1f",
                            result.getAverageScore()
                    )
                            + "    Score: "
                            + String.format(
                            "%.1f%%",
                            result.getScorePercent()
                    ),
                    centerX,
                    barY + 69,
                    0xFFFFFFFF
            );

            /*
             * 판정 횟수는 세로로 쌓지 않고
             * 한 줄에 표시한다.
             */

            String judgementSummary =
                    "Fantastic: "
                            + result.getFantasticCount()
                            + "    Perfect: "
                            + result.getPerfectCount()
                            + "    Good: "
                            + result.getGoodCount()
                            + "    Miss: "
                            + result.getMissCount();

            graphics.drawCenteredString(
                    this.font,
                    judgementSummary,
                    centerX,
                    barY + 88,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    "Click to Close",
                    centerX,
                    barY + 108,
                    0xFFAAAAAA
            );
        }

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    /*
     * =========================
     * 클릭 처리
     * =========================
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (button != 0) {

            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        switch (state) {

            /*
             * 첫 클릭
             * → 카운트다운 시작
             */

            case READY -> {

                startCountdown();

                return true;
            }

            /*
             * 카운트다운 중에는
             * 클릭 무시
             */

            case COUNTDOWN -> {

                return true;
            }

            /*
             * 게임 진행 중
             * → 타이밍 판정
             */

            case PLAYING -> {

                submitTiming();

                if (game.isFinished()) {
                    state = GameState.FINISHED;
                }

                return true;
            }

            /*
             * 게임 종료 후 클릭
             * → 화면 닫기
             */

            case FINISHED -> {

                Minecraft.getInstance()
                        .setScreen(null);

                return true;
            }
        }

        return true;
    }

    /*
     * =========================
     * 카운트다운 시작
     * =========================
     */

    private void startCountdown() {

        this.countdownStartTime =
                System.nanoTime();

        this.state =
                GameState.COUNTDOWN;
    }

    /*
     * =========================
     * 상태 갱신
     * =========================
     */

    private void updateState() {

        if (state != GameState.COUNTDOWN) {
            return;
        }

        double elapsedSeconds =
                (System.nanoTime()
                        - countdownStartTime)
                        / 1_000_000_000.0;

        if (elapsedSeconds >= COUNTDOWN_SECONDS) {

            this.gameStartTime =
                    System.nanoTime();

            this.state =
                    GameState.PLAYING;
        }
    }

    /*
     * =========================
     * 카운트다운 숫자
     * =========================
     */

    private int getCountdownNumber() {

        double elapsedSeconds =
                (System.nanoTime()
                        - countdownStartTime)
                        / 1_000_000_000.0;

        int number =
                3 - (int) elapsedSeconds;

        return Mth.clamp(
                number,
                1,
                3
        );
    }

    /*
     * =========================
     * 클릭 → TimingGame
     * =========================
     */

    private void submitTiming() {

        int centerX =
                this.width / 2;

        int barWidth =
                Math.min(
                        BAR_WIDTH,
                        Math.max(100, this.width - 40)
                );

        int barX =
                centerX - barWidth / 2;

        int pointerCenterX =
                calculatePointerX(
                        barX,
                        barWidth
                );

        double halfBarWidth =
                barWidth / 2.0;

        double distanceFromCenter =
                Math.abs(
                        pointerCenterX - centerX
                ) / halfBarWidth;

        game.submit(
                distanceFromCenter
        );
    }

    /*
     * =========================
     * 포인터 위치
     * =========================
     */

    private int calculatePointerX(
            int barX,
            int barWidth
    ) {

        double elapsedSeconds =
                (System.nanoTime()
                        - gameStartTime)
                        / 1_000_000_000.0;

        double cycle =
                elapsedSeconds
                        / config.getTravelTimeSeconds();

        double cyclePosition =
                cycle % 2.0;

        double progress;

        if (cyclePosition <= 1.0) {

            progress =
                    cyclePosition;

        } else {

            progress =
                    2.0 - cyclePosition;
        }

        progress =
                Mth.clamp(
                        progress,
                        0.0,
                        1.0
                );

        return barX
                + (int) Math.round(
                progress * barWidth
        );
    }

    /*
     * =========================
     * 게임 상태
     * =========================
     */

    private enum GameState {
        READY,
        COUNTDOWN,
        PLAYING,
        FINISHED
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}