package io.github.leejihoon1592.minigames.game.path;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.function.Consumer;

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
    private PathResult result = null;

    /*
     * =========================
     * 결과 콜백
     * =========================
     *
     * 게임이 끝났을 때
     * PathResult를 호출한 쪽으로 전달한다.
     */

    private final Consumer<PathResult> resultCallback;

    /*
     * =========================
     * 진행도
     * =========================
     */

    private double progress = 0.0;
    private double restartProgress = 0.0;

    /*
     * =========================
     * 플레이어 Trail
     * =========================
     */

    /*
     * 실제 마우스가 지나간 위치를 저장한다.
     *
     * Trail은 시각적 표현만 담당하며
     * Path 판정이나 Accuracy 계산에는
     * 사용하지 않는다.
     */

    /*
     * Trail은 MISS가 발생할 때마다
     * 서로 독립된 구간으로 나뉜다.
     *
     * 각 내부 List가 하나의 연속된 Trail이다.
     */

    private final List<List<PathSegment.Point>> trailSegments =
            new ArrayList<>();

    /*
     * 현재 기록 중인 Trail 구간
     */

    private List<PathSegment.Point> currentTrail =
            null;

    /*
     * Trail 좌표 기록 간격
     */

    private static final double TRAIL_SAMPLE_DISTANCE = 3.0;

    /*
     * 마지막으로 Trail에 기록한 마우스 좌표
     */

    private double lastTrailMouseX = 0.0;
    private double lastTrailMouseY = 0.0;

    private boolean hasTrailPoint = false;

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
        this(
                result -> {
                }
        );
    }

    public PathScreen(
            Consumer<PathResult> resultCallback
    ) {
        super(
                Component.literal("Path Game")
        );

        this.resultCallback =
                resultCallback != null
                        ? resultCallback
                        : result -> {
                };
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

        /*
         * 플레이어가 실제로 지나간 Trail
         */

        drawTrail(graphics);

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

            /*
             * Trail 초기화
             */

            trailSegments.clear();

            currentTrail = null;

            hasTrailPoint = false;

            /*
             * START 위치를
             * 첫 번째 Trail 좌표로 기록
             */

            updateTrail(
                    mouseX,
                    mouseY
            );

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

            /*
             * MISS 이전 Trail과 연결되지 않도록
             * 새로운 Trail 구간을 시작한다.
             */

            currentTrail = null;

            hasTrailPoint = false;

            updateTrail(
                    mouseX,
                    mouseY
            );

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

            /*
             * 실제 플레이어의 마우스 이동 경로를
             * Trail로 기록한다.
             */

            updateTrail(
                    mouseX,
                    mouseY
            );

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

            result = createResult();

            /*
             * 게임 결과를
             * 호출한 쪽으로 전달한다.
             *
             * state를 먼저 FINISHED로 바꾸기 때문에
             * 다음 render부터는 이 부분이
             * 다시 실행되지 않는다.
             */

            resultCallback.accept(
                    result
            );

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
     * Trail 기록
     * =========================
     */

    private void updateTrail(
            double mouseX,
            double mouseY
    ) {

        /*
         * 새로운 Trail 구간 시작
         */

        if (!hasTrailPoint
                || currentTrail == null) {

            currentTrail =
                    new ArrayList<>();

            trailSegments.add(
                    currentTrail
            );

            currentTrail.add(
                    new PathSegment.Point(
                            mouseX,
                            mouseY
                    )
            );

            lastTrailMouseX = mouseX;
            lastTrailMouseY = mouseY;

            hasTrailPoint = true;

            return;
        }

        /*
         * 마지막 Trail 좌표에서
         * 현재 마우스 위치까지의 거리 계산
         */

        double deltaX =
                mouseX - lastTrailMouseX;

        double deltaY =
                mouseY - lastTrailMouseY;

        double movedDistance =
                Math.sqrt(
                        deltaX * deltaX
                                + deltaY * deltaY
                );

        /*
         * 3px 미만이라면
         * 아직 새로운 좌표를 만들지 않는다.
         */

        if (movedDistance
                < TRAIL_SAMPLE_DISTANCE) {

            return;
        }

        /*
         * 이동 방향의 단위 벡터
         */

        double directionX =
                deltaX / movedDistance;

        double directionY =
                deltaY / movedDistance;

        /*
         * 마지막 Trail 좌표에서 현재 마우스 위치까지
         * TRAIL_SAMPLE_DISTANCE 간격으로
         * 중간 좌표를 생성한다.
         *
         * 마우스가 한 프레임에 멀리 이동하더라도
         * Trail이 중간에서 끊기지 않게 한다.
         */

        double distanceToAdd =
                TRAIL_SAMPLE_DISTANCE;

        while (distanceToAdd
                <= movedDistance) {

            double pointX =
                    lastTrailMouseX
                            + directionX
                            * distanceToAdd;

            double pointY =
                    lastTrailMouseY
                            + directionY
                            * distanceToAdd;

            currentTrail.add(
                    new PathSegment.Point(
                            pointX,
                            pointY
                    )
            );

            distanceToAdd +=
                    TRAIL_SAMPLE_DISTANCE;
        }

        /*
         * 마지막으로 실제 추가된 Trail 좌표를
         * 다음 보간의 기준점으로 사용한다.
         *
         * 현재 마우스 좌표를 그대로 기준으로 삼으면
         * 3px보다 작은 나머지 이동거리가 매번 버려진다.
         */

        PathSegment.Point lastPoint =
                currentTrail.get(
                        currentTrail.size() - 1
                );

        lastTrailMouseX =
                lastPoint.x();

        lastTrailMouseY =
                lastPoint.y();
    }

    /*
     * =========================
     * Trail 렌더링
     * =========================
     */

    private void drawTrail(
            GuiGraphics graphics
    ) {

        /*
         * 각각의 Trail 구간을
         * 서로 독립적으로 그린다.
         */

        for (List<PathSegment.Point> trail : trailSegments) {

            /*
             * 좌표가 하나도 없다면
             * 그릴 것이 없다.
             */

            if (trail.isEmpty()) {
                continue;
            }

            /*
             * 현재는 Trail 좌표마다
             * 작은 사각형을 그린다.
             *
             * 기록 간격이 3px이므로
             * 화면에서는 하나의 선처럼 보인다.
             */

            for (PathSegment.Point point : trail) {

                int x =
                        (int) Math.round(
                                point.x()
                        );

                int y =
                        (int) Math.round(
                                point.y()
                        );

                graphics.fill(
                        x - 1,
                        y - 1,
                        x + 2,
                        y + 2,
                        0xFF55AAFF
                );
            }
        }
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
     * 게임 결과 생성
     * =========================
     */

    private PathResult createResult() {

        int checkpointsPassed = 0;
        int checkpointsDisabled = 0;

        for (PathCheckpoint checkpoint : checkpoints) {

            if (checkpoint.isPassed()) {
                checkpointsPassed++;
            }

            if (checkpoint.isDisabled()) {
                checkpointsDisabled++;
            }
        }

        return new PathResult(
                getAverageAccuracy(),
                checkpointsPassed,
                checkpointsDisabled,
                checkpoints.size(),
                missCount,
                state == GameState.FINISHED
        );
    }

    /*
     * =========================
     * MISS
     * =========================
     */

    private void triggerMiss() {

        restartProgress = progress;

        missCount++;

        /*
         * 아직 통과하지 않은 체크포인트 중
         * 가장 뒤쪽의 체크포인트 하나를 제거한다.
         *
         * 이미 통과한 체크포인트는
         * 절대로 제거하지 않는다.
         */

        for (int i = checkpoints.size() - 1;
             i >= 0;
             i--) {

            PathCheckpoint checkpoint =
                    checkpoints.get(i);

            if (checkpoint.isAvailable()) {

                checkpoint.disable();
                break;
            }
        }

        state = GameState.MISS_PAUSED;

        currentlyInsidePath = false;

        /*
         * MISS 상태에서는
         * 정확도 샘플링도 중단된다.
         */

        hasAccuracySample = false;

        /*
         * 현재 Trail 구간도 여기서 종료한다.
         *
         * 초록점으로 돌아가는 동안의
         * 마우스 이동은 Trail에 기록하지 않는다.
         */

        currentTrail = null;
        hasTrailPoint = false;
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

            /*
             * MISS로 비활성화된 체크포인트는
             * 화면에 표시하지 않는다.
             */

            if (checkpoint.isDisabled()) {
                continue;
            }

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

        /*
         * START 방향은 첫 번째 세그먼트,
         * END 방향은 마지막 세그먼트를 기준으로 한다.
         */

        PathSegment firstSegment =
                path.getSegments().get(0);

        PathSegment lastSegment =
                path.getSegments().get(
                        path.getSegments().size() - 1
                );

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

        /*
         * =========================
         * START
         * =========================
         *
         * 진행 방향이 가로라면
         * START 선은 세로로 그린다.
         *
         * 진행 방향이 세로라면
         * START 선은 가로로 그린다.
         */

        double startDeltaX =
                firstSegment.getEndX()
                        - firstSegment.getStartX();

        double startDeltaY =
                firstSegment.getEndY()
                        - firstSegment.getStartY();

        if (Math.abs(startDeltaX)
                >= Math.abs(startDeltaY)) {

            /*
             * 가로 진행 → 세로 START
             */

            graphics.fill(
                    startX - 3,
                    startY - halfThickness,
                    startX + 3,
                    startY + halfThickness,
                    START_COLOR
            );

        } else {

            /*
             * 세로 진행 → 가로 START
             */

            graphics.fill(
                    startX - halfThickness,
                    startY - 3,
                    startX + halfThickness,
                    startY + 3,
                    START_COLOR
            );
        }

        /*
         * =========================
         * END
         * =========================
         *
         * 마지막 진행 방향이 가로라면
         * END 선은 세로로 그린다.
         *
         * 마지막 진행 방향이 세로라면
         * END 선은 가로로 그린다.
         */

        double endDeltaX =
                lastSegment.getEndX()
                        - lastSegment.getStartX();

        double endDeltaY =
                lastSegment.getEndY()
                        - lastSegment.getStartY();

        if (Math.abs(endDeltaX)
                >= Math.abs(endDeltaY)) {

            /*
             * 가로 진행 → 세로 END
             */

            graphics.fill(
                    endX - 3,
                    endY - halfThickness,
                    endX + 3,
                    endY + halfThickness,
                    END_COLOR
            );

        } else {

            /*
             * 세로 진행 → 가로 END
             */

            graphics.fill(
                    endX - halfThickness,
                    endY - 3,
                    endX + halfThickness,
                    endY + 3,
                    END_COLOR
            );
        }
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

        PathSegment firstSegment =
                path.getSegments().get(0);

        double deltaX =
                firstSegment.getEndX()
                        - firstSegment.getStartX();

        double deltaY =
                firstSegment.getEndY()
                        - firstSegment.getStartY();

        double markerHalfWidth = 3.0;
        double pathHalfWidth =
                PATH_THICKNESS / 2.0;

        /*
         * 첫 번째 세그먼트가 가로 방향이면
         * START는 세로선이다.
         */

        if (Math.abs(deltaX)
                >= Math.abs(deltaY)) {

            return mouseX >= start.x() - markerHalfWidth
                    && mouseX <= start.x() + markerHalfWidth
                    && mouseY >= start.y() - pathHalfWidth
                    && mouseY <= start.y() + pathHalfWidth;
        }

        /*
         * 첫 번째 세그먼트가 세로 방향이면
         * START는 가로선이다.
         */

        return mouseX >= start.x() - pathHalfWidth
                && mouseX <= start.x() + pathHalfWidth
                && mouseY >= start.y() - markerHalfWidth
                && mouseY <= start.y() + markerHalfWidth;
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

        PathSegment lastSegment =
                path.getSegments().get(
                        path.getSegments().size() - 1
                );

        double deltaX =
                lastSegment.getEndX()
                        - lastSegment.getStartX();

        double deltaY =
                lastSegment.getEndY()
                        - lastSegment.getStartY();

        double markerHalfWidth = 3.0;
        double pathHalfWidth =
                PATH_THICKNESS / 2.0;

        /*
         * 마지막 세그먼트가 가로 방향이면
         * END는 세로선이다.
         */

        if (Math.abs(deltaX)
                >= Math.abs(deltaY)) {

            return mouseX >= end.x() - markerHalfWidth
                    && mouseX <= end.x() + markerHalfWidth
                    && mouseY >= end.y() - pathHalfWidth
                    && mouseY <= end.y() + pathHalfWidth;
        }

        /*
         * 마지막 세그먼트가 세로 방향이면
         * END는 가로선이다.
         */

        return mouseX >= end.x() - pathHalfWidth
                && mouseX <= end.x() + pathHalfWidth
                && mouseY >= end.y() - markerHalfWidth
                && mouseY <= end.y() + markerHalfWidth;
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

        /*
         * =========================
         * 최종 결과 표시
         * =========================
         */

        if (state == GameState.FINISHED
                && result != null) {

            String qualityText =
                    String.format(
                            Locale.ROOT,
                            "Quality : %.1f",
                            result.getQualityScore()
                    );

            String checkpointText =
                    "Work : "
                            + result.getCheckpointsPassed()
                            + " / "
                            + result.getTotalCheckpoints();

            String disabledText =
                    "Disabled : "
                            + result.getCheckpointsDisabled();

            String completedText =
                    "Completed : "
                            + result.isCompleted();

            graphics.drawCenteredString(
                    this.font,
                    qualityText,
                    this.width / 2,
                    110,
                    0xFFFFD700
            );

            graphics.drawCenteredString(
                    this.font,
                    checkpointText,
                    this.width / 2,
                    125,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    disabledText,
                    this.width / 2,
                    140,
                    0xFFFFFFFF
            );

            graphics.drawCenteredString(
                    this.font,
                    completedText,
                    this.width / 2,
                    155,
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