package io.github.leejihoon1592.minigames.game.path;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public class PathScreen extends Screen {

    /*
     * =========================
     * 화면 설정
     * =========================
     */

    private static final int PATH_THICKNESS = 30;
    private static final int CENTER_LINE_THICKNESS = 2;

    private static final int PATH_COLOR = 0xFFAAAAAA;
    private static final int CENTER_LINE_COLOR = 0xFFFF3333;

    private static final int START_COLOR = 0xFFFF5555;
    private static final int END_COLOR = 0xFFFF5555;

    /*
     * =========================
     * 체크포인트 표시
     * =========================
     */

    private static final int CHECKPOINT_COLOR = 0xFFFFFF00;
    private static final int CHECKPOINT_PASSED_COLOR = 0xFF55FF55;
    private static final int CHECKPOINT_THICKNESS = 3;

    /*
     * =========================
     * MISS 재시작점
     * =========================
     */

    private static final int RESTART_POINT_COLOR = 0xFF55FF55;
    private static final int RESTART_POINT_RADIUS = 5;

    private static final double RESTART_CLICK_RADIUS = 10.0;

    /*
     * =========================
     * 진행도 설정
     * =========================
     */

    private static final double MAX_PROGRESS_JUMP_PIXELS = 40.0;

    /*
     * =========================
     * 정확도 설정
     * =========================
     *
     * 마우스가 이 거리 이상 움직였을 때
     * 새로운 정확도 샘플을 기록한다.
     *
     * 렌더링 FPS가 아니라
     * 실제 마우스 이동거리를 기준으로 한다.
     */

    private static final double ACCURACY_SAMPLE_DISTANCE = 3.0;

    /*
     * =========================
     * Path
     * =========================
     */

    private Path path;

    /*
     * =========================
     * 체크포인트
     * =========================
     *
     * 10% ~ 90% = 노란 체크포인트 9개
     *
     * END는 마지막 10번째
     * 제작 단계로 취급한다.
     */

    private final List<PathCheckpoint> checkpoints =
            List.of(
                    new PathCheckpoint(0.10),
                    new PathCheckpoint(0.20),
                    new PathCheckpoint(0.30),
                    new PathCheckpoint(0.40),
                    new PathCheckpoint(0.50),
                    new PathCheckpoint(0.60),
                    new PathCheckpoint(0.70),
                    new PathCheckpoint(0.80),
                    new PathCheckpoint(0.90)
            );

    /*
     * =========================
     * 게임 상태
     * =========================
     */

    private GameState state = GameState.READY;

    private boolean currentlyInsidePath = false;

    private int missCount = 0;

    /*
     * =========================
     * 진행도
     * =========================
     */

    private double progress = 0.0;
    private double restartProgress = 0.0;

    /*
     * =========================
     * 정확도
     * =========================
     */

    /*
     * 현재 마우스 위치의 순간 정확도
     *
     * 0.0 ~ 1.0
     */

    private double currentAccuracy = 1.0;

    /*
     * 지금까지 기록된 정확도 합계
     */

    private double accuracyTotal = 0.0;

    /*
     * 정확도 샘플 개수
     */

    private int accuracySampleCount = 0;

    /*
     * 마지막 정확도 샘플을 기록한
     * 마우스 좌표
     */

    private double lastSampleMouseX = 0.0;
    private double lastSampleMouseY = 0.0;

    /*
     * 첫 샘플이 존재하는지 여부
     */

    private boolean hasAccuracySample = false;

    public PathScreen() {
        super(
                Component.literal("Path Game")
        );
    }

    /*
     * =========================
     * 화면 초기화
     * =========================
     */

    @Override
    protected void init() {
        super.init();

        this.path = createTestPath();
    }

    /*
     * =========================
     * 테스트 Path 생성
     * =========================
     */

    private Path createTestPath() {

        double centerX = this.width / 2.0;
        double centerY = this.height / 2.0;

        double startX = centerX - 180.0;
        double startY = centerY - 60.0;

        PathSegment segment1 =
                new PathSegment(
                        startX,
                        startY,
                        startX + 140.0,
                        startY
                );

        PathSegment segment2 =
                new PathSegment(
                        startX + 140.0,
                        startY,
                        startX + 140.0,
                        startY + 70.0
                );

        PathSegment segment3 =
                new PathSegment(
                        startX + 140.0,
                        startY + 70.0,
                        startX + 70.0,
                        startY + 70.0
                );

        PathSegment segment4 =
                new PathSegment(
                        startX + 70.0,
                        startY + 70.0,
                        startX + 70.0,
                        startY + 140.0
                );

        PathSegment segment5 =
                new PathSegment(
                        startX + 70.0,
                        startY + 140.0,
                        startX + 360.0,
                        startY + 140.0
                );

        return new Path(
                List.of(
                        segment1,
                        segment2,
                        segment3,
                        segment4,
                        segment5
                )
        );
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
        this.renderBackground(graphics);

        if (path == null) {
            super.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
            return;
        }

        drawPathArea(graphics);
        drawCenterLine(graphics);
        drawCheckpoints(graphics);
        drawStartAndEnd(graphics);

        if (state == GameState.MISS_PAUSED) {
            drawRestartPoint(graphics);
        }

        Path.ClosestPathPoint closest =
                path.findClosestPoint(
                        mouseX,
                        mouseY
                );

        boolean insidePath =
                closest.distance()
                        <= PATH_THICKNESS / 2.0;

        boolean insideStart =
                isInsideStart(
                        mouseX,
                        mouseY
                );

        boolean insideEnd =
                isInsideEnd(
                        mouseX,
                        mouseY
                );

        /*
         * 현재 위치의 순간 정확도 계산
         */

        currentAccuracy =
                calculateAccuracy(
                        closest.distance()
                );

        updateGameState(
                insidePath,
                insideEnd,
                closest,
                mouseX,
                mouseY
        );

        drawStatus(
                graphics,
                insidePath,
                insideStart,
                mouseX,
                mouseY
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
     * 마우스 클릭
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

        /*
         * START 클릭
         */

        if (state == GameState.READY
                && path != null
                && isInsideStart(
                mouseX,
                mouseY
        )) {

            state = GameState.PLAYING;

            currentlyInsidePath = true;

            missCount = 0;

            progress = 0.0;
            restartProgress = 0.0;

            /*
             * 정확도 초기화
             */

            currentAccuracy = 1.0;

            accuracyTotal = 0.0;
            accuracySampleCount = 0;

            lastSampleMouseX = mouseX;
            lastSampleMouseY = mouseY;

            hasAccuracySample = true;

            /*
             * START 자체를 첫 정확도 샘플로 기록
             */

            addAccuracySample(1.0);

            return true;
        }

        /*
         * MISS 후 재시작
         */

        if (state == GameState.MISS_PAUSED
                && path != null
                && isInsideRestartPoint(
                mouseX,
                mouseY
        )) {

            state = GameState.PLAYING;

            currentlyInsidePath = true;

            progress = restartProgress;

            /*
             * 초록점은 정확도 100%의
             * 중앙선 위에 있으므로
             * 새로운 샘플 기준점을 여기로 잡는다.
             */

            lastSampleMouseX = mouseX;
            lastSampleMouseY = mouseY;

            hasAccuracySample = true;

            currentAccuracy = 1.0;

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
     * 게임 상태 갱신
     * =========================
     */

    private void updateGameState(
            boolean insidePath,
            boolean insideEnd,
            Path.ClosestPathPoint closest,
            double mouseX,
            double mouseY
    ) {

        if (state == GameState.READY) {
            return;
        }

        if (state == GameState.MISS_PAUSED) {
            return;
        }

        if (state == GameState.FINISHED) {
            return;
        }

        /*
         * 여기부터 PLAYING
         */

        if (insidePath) {

            boolean progressed =
                    updateProgress(closest);

            if (progressed) {

                updateAccuracySampling(
                        mouseX,
                        mouseY,
                        currentAccuracy
                );
            }
        }

        /*
         * 정상 완료
         */

        if (insideEnd
                && progress >= 0.99) {

            progress = 1.0;

            state = GameState.FINISHED;

            return;
        }

        /*
         * Path 이탈
         */

        if (currentlyInsidePath
                && !insidePath) {

            triggerMiss();

            return;
        }

        currentlyInsidePath =
                insidePath;
    }

    /*
     * =========================
     * 정확도 계산
     * =========================
     */

    private double calculateAccuracy(
            double distanceFromCenter
    ) {

        double maximumDistance =
                PATH_THICKNESS / 2.0;

        /*
         * 중앙선
         */

        if (distanceFromCenter <= 0.0) {
            return 1.0;
        }

        /*
         * 회색 Path 밖
         */

        if (distanceFromCenter
                >= maximumDistance) {

            return 0.0;
        }

        /*
         * 현재는 가장 단순한 선형 계산
         *
         * 중앙 = 100%
         * 가장자리 = 0%
         */

        double accuracy =
                1.0
                        - (
                        distanceFromCenter
                                / maximumDistance
                );

        return Math.max(
                0.0,
                Math.min(
                        1.0,
                        accuracy
                )
        );
    }

    /*
     * =========================
     * 정확도 샘플링
     * =========================
     */

    private void updateAccuracySampling(
            double mouseX,
            double mouseY,
            double accuracy
    ) {

        if (!hasAccuracySample) {

            lastSampleMouseX = mouseX;
            lastSampleMouseY = mouseY;

            hasAccuracySample = true;

            addAccuracySample(accuracy);

            return;
        }

        double movedDistance =
                distance(
                        mouseX,
                        mouseY,
                        lastSampleMouseX,
                        lastSampleMouseY
                );

        /*
         * 3px 이상 움직였을 때만
         * 새로운 샘플 기록
         */

        if (movedDistance
                < ACCURACY_SAMPLE_DISTANCE) {

            return;
        }

        addAccuracySample(accuracy);

        lastSampleMouseX = mouseX;
        lastSampleMouseY = mouseY;
    }

    /*
     * =========================
     * 정확도 샘플 추가
     * =========================
     */

    private void addAccuracySample(
            double accuracy
    ) {

        accuracyTotal += accuracy;
        accuracySampleCount++;
    }

    /*
     * =========================
     * 누적 정확도
     * =========================
     */

    private double getAverageAccuracy() {

        if (accuracySampleCount <= 0) {
            return 1.0;
        }

        return accuracyTotal
                / accuracySampleCount;
    }

    /*
     * =========================
     * MISS
     * =========================
     */

    private void triggerMiss() {

        restartProgress = progress;

        missCount++;

        state = GameState.MISS_PAUSED;

        currentlyInsidePath = false;

        /*
         * MISS 상태에서는
         * 정확도 샘플링도 중단된다.
         */

        hasAccuracySample = false;
    }

    /*
     * =========================
     * 진행도
     * =========================
     */

    private boolean updateProgress(
            Path.ClosestPathPoint closest
    ) {

        double candidateProgress =
                closest.pathProgress();

        /*
         * 뒤로 움직이거나
         * 이미 지나간 구간이면
         * 새로운 진행이 아니다.
         */

        if (candidateProgress <= progress) {
            return false;
        }

        double currentDistance =
                progress
                        * path.getTotalLength();

        double candidateDistance =
                closest.distanceAlongPath();

        double forwardJump =
                candidateDistance
                        - currentDistance;

        /*
         * 지나치게 먼 위치로
         * 한 번에 건너뛰는 것을 방지한다.
         */

        if (forwardJump
                > MAX_PROGRESS_JUMP_PIXELS) {

            return false;
        }

        /*
         * 진행도 갱신 전 위치
         */

        double previousProgress =
                progress;

        progress =
                candidateProgress;

        /*
         * 이전 위치와 현재 위치 사이에
         * 체크포인트가 있으면 통과 처리
         */

        for (PathCheckpoint checkpoint :
                checkpoints) {

            if (!checkpoint.isAvailable()) {
                continue;
            }

            double checkpointProgress =
                    checkpoint.getProgress();

            if (checkpointProgress > previousProgress
                    && checkpointProgress <= progress) {

                checkpoint.pass();
            }
        }

        /*
         * 실제로 새로운 최고 진행도를
         * 획득했음을 알린다.
         */

        return true;
    }

    /*
     * =========================
     * 회색 Path
     * =========================
     */

    private void drawPathArea(
            GuiGraphics graphics
    ) {

        int halfThickness =
                PATH_THICKNESS / 2;

        for (PathSegment segment :
                path.getSegments()) {

            int startX =
                    (int) Math.round(
                            Math.min(
                                    segment.getStartX(),
                                    segment.getEndX()
                            )
                    );

            int startY =
                    (int) Math.round(
                            Math.min(
                                    segment.getStartY(),
                                    segment.getEndY()
                            )
                    );

            int endX =
                    (int) Math.round(
                            Math.max(
                                    segment.getStartX(),
                                    segment.getEndX()
                            )
                    );

            int endY =
                    (int) Math.round(
                            Math.max(
                                    segment.getStartY(),
                                    segment.getEndY()
                            )
                    );

            if (segment.getStartY()
                    == segment.getEndY()) {

                graphics.fill(
                        startX - halfThickness,
                        startY - halfThickness,
                        endX + halfThickness,
                        startY + halfThickness,
                        PATH_COLOR
                );

            } else if (segment.getStartX()
                    == segment.getEndX()) {

                graphics.fill(
                        startX - halfThickness,
                        startY - halfThickness,
                        startX + halfThickness,
                        endY + halfThickness,
                        PATH_COLOR
                );
            }
        }
    }

    /*
     * =========================
     * 빨간 중앙선
     * =========================
     */

    private void drawCenterLine(
            GuiGraphics graphics
    ) {

        int halfLine =
                CENTER_LINE_THICKNESS / 2;

        for (PathSegment segment :
                path.getSegments()) {

            int startX =
                    (int) Math.round(
                            segment.getStartX()
                    );

            int startY =
                    (int) Math.round(
                            segment.getStartY()
                    );

            int endX =
                    (int) Math.round(
                            segment.getEndX()
                    );

            int endY =
                    (int) Math.round(
                            segment.getEndY()
                    );

            if (startY == endY) {

                int left =
                        Math.min(
                                startX,
                                endX
                        );

                int right =
                        Math.max(
                                startX,
                                endX
                        );

                graphics.fill(
                        left,
                        startY - halfLine,
                        right,
                        startY - halfLine
                                + CENTER_LINE_THICKNESS,
                        CENTER_LINE_COLOR
                );

            } else if (startX == endX) {

                int top =
                        Math.min(
                                startY,
                                endY
                        );

                int bottom =
                        Math.max(
                                startY,
                                endY
                        );

                graphics.fill(
                        startX - halfLine,
                        top,
                        startX - halfLine
                                + CENTER_LINE_THICKNESS,
                        bottom,
                        CENTER_LINE_COLOR
                );
            }
        }
    }

    /*
     * =========================
     * 체크포인트 표시
     * =========================
     */

    private void drawCheckpoints(
            GuiGraphics graphics
    ) {

        int halfPath =
                PATH_THICKNESS / 2;

        for (PathCheckpoint checkpoint :
                checkpoints) {

            PathSegment.Point point =
                    path.getPointAtProgress(
                            checkpoint.getProgress()
                    );

            /*
             * 체크포인트가 위치한 Segment를
             * 확인한다.
             */

            Path.ClosestPathPoint closest =
                    path.findClosestPoint(
                            point.x(),
                            point.y()
                    );

            PathSegment segment =
                    path.getSegments().get(
                            closest.segmentIndex()
                    );

            int x =
                    (int) Math.round(
                            point.x()
                    );

            int y =
                    (int) Math.round(
                            point.y()
                    );

            int checkpointColor =
                    checkpoint.isPassed()
                            ? CHECKPOINT_PASSED_COLOR
                            : CHECKPOINT_COLOR;

            /*
             * Path가 가로 방향이면
             * 체크포인트는 세로선
             */

            if (segment.getStartY()
                    == segment.getEndY()) {

                graphics.fill(
                        x - CHECKPOINT_THICKNESS / 2,
                        y - halfPath,
                        x - CHECKPOINT_THICKNESS / 2
                                + CHECKPOINT_THICKNESS,
                        y + halfPath,
                        checkpointColor
                );
            }

            /*
             * Path가 세로 방향이면
             * 체크포인트는 가로선
             */

            else if (segment.getStartX()
                    == segment.getEndX()) {

                graphics.fill(
                        x - halfPath,
                        y - CHECKPOINT_THICKNESS / 2,
                        x + halfPath,
                        y - CHECKPOINT_THICKNESS / 2
                                + CHECKPOINT_THICKNESS,
                        checkpointColor
                );
            }
        }
    }

    /*
     * =========================
     * START / END
     * =========================
     */

    private void drawStartAndEnd(
            GuiGraphics graphics
    ) {

        PathSegment.Point start =
                path.getPointAtProgress(0.0);

        PathSegment.Point end =
                path.getPointAtProgress(1.0);

        int halfThickness =
                PATH_THICKNESS / 2;

        int startX =
                (int) Math.round(start.x());

        int startY =
                (int) Math.round(start.y());

        int endX =
                (int) Math.round(end.x());

        int endY =
                (int) Math.round(end.y());

        graphics.fill(
                startX - 3,
                startY - halfThickness,
                startX + 3,
                startY + halfThickness,
                START_COLOR
        );

        graphics.fill(
                endX - 3,
                endY - halfThickness,
                endX + 3,
                endY + halfThickness,
                END_COLOR
        );
    }

    /*
     * =========================
     * 재시작점
     * =========================
     */

    private void drawRestartPoint(
            GuiGraphics graphics
    ) {

        PathSegment.Point point =
                path.getPointAtProgress(
                        restartProgress
                );

        int x =
                (int) Math.round(point.x());

        int y =
                (int) Math.round(point.y());

        graphics.fill(
                x - RESTART_POINT_RADIUS,
                y - RESTART_POINT_RADIUS,
                x + RESTART_POINT_RADIUS + 1,
                y + RESTART_POINT_RADIUS + 1,
                RESTART_POINT_COLOR
        );
    }

    /*
     * =========================
     * START 판정
     * =========================
     */

    private boolean isInsideStart(
            double mouseX,
            double mouseY
    ) {

        PathSegment.Point start =
                path.getPointAtProgress(0.0);

        return distance(
                mouseX,
                mouseY,
                start.x(),
                start.y()
        ) <= PATH_THICKNESS / 2.0;
    }

    /*
     * =========================
     * END 판정
     * =========================
     */

    private boolean isInsideEnd(
            double mouseX,
            double mouseY
    ) {

        PathSegment.Point end =
                path.getPointAtProgress(1.0);

        return distance(
                mouseX,
                mouseY,
                end.x(),
                end.y()
        ) <= PATH_THICKNESS / 2.0;
    }

    /*
     * =========================
     * 재시작점 판정
     * =========================
     */

    private boolean isInsideRestartPoint(
            double mouseX,
            double mouseY
    ) {

        PathSegment.Point point =
                path.getPointAtProgress(
                        restartProgress
                );

        return distance(
                mouseX,
                mouseY,
                point.x(),
                point.y()
        ) <= RESTART_CLICK_RADIUS;
    }

    /*
     * =========================
     * 상태 표시
     * =========================
     */

    private void drawStatus(
            GuiGraphics graphics,
            boolean insidePath,
            boolean insideStart,
            double mouseX,
            double mouseY
    ) {

        String mainText;
        int mainColor;

        switch (state) {

            case READY -> {

                if (insideStart) {

                    mainText =
                            "CLICK TO START";

                    mainColor =
                            0xFF55FF55;

                } else {

                    mainText =
                            "MOVE TO START";

                    mainColor =
                            0xFFFFFFFF;
                }
            }

            case PLAYING -> {

                if (insidePath) {

                    mainText =
                            "PATH : INSIDE";

                    mainColor =
                            0xFF55FF55;

                } else {

                    mainText =
                            "PATH : OUTSIDE";

                    mainColor =
                            0xFFFF5555;
                }
            }

            case MISS_PAUSED -> {

                if (isInsideRestartPoint(
                        mouseX,
                        mouseY
                )) {

                    mainText =
                            "CLICK TO RESUME";

                    mainColor =
                            0xFF55FF55;

                } else {

                    mainText =
                            "MISS - RETURN TO GREEN POINT";

                    mainColor =
                            0xFFFF5555;
                }
            }

            case FINISHED -> {

                mainText =
                        "FINISHED";

                mainColor =
                        0xFFFFD700;
            }

            default -> {

                mainText = "";

                mainColor =
                        0xFFFFFFFF;
            }
        }

        graphics.drawCenteredString(
                this.font,
                mainText,
                this.width / 2,
                30,
                mainColor
        );

        if (state != GameState.READY) {

            String progressText =
                    String.format(
                            Locale.ROOT,
                            "Progress : %.1f%%",
                            progress * 100.0
                    );

            String accuracyText =
                    String.format(
                            Locale.ROOT,
                            "Accuracy : %.1f%%",
                            getAverageAccuracy()
                                    * 100.0
                    );

            String currentAccuracyText =
                    String.format(
                            Locale.ROOT,
                            "Current : %.1f%%",
                            currentAccuracy
                                    * 100.0
                    );

            graphics.drawCenteredString(
                    this.font,
                    progressText,
                    this.width / 2,
                    45,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    accuracyText,
                    this.width / 2,
                    60,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    currentAccuracyText,
                    this.width / 2,
                    75,
                    0xFFAAAAAA
            );

            graphics.drawCenteredString(
                    this.font,
                    "Miss : "
                            + missCount,
                    this.width / 2,
                    90,
                    0xFFFFFFFF
            );
        }
    }

    /*
     * =========================
     * 거리 계산
     * =========================
     */

    private double distance(
            double x1,
            double y1,
            double x2,
            double y2
    ) {

        double deltaX =
                x1 - x2;

        double deltaY =
                y1 - y2;

        return Math.sqrt(
                deltaX * deltaX
                        + deltaY * deltaY
        );
    }

    private enum GameState {

        READY,

        PLAYING,

        MISS_PAUSED,

        FINISHED
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}