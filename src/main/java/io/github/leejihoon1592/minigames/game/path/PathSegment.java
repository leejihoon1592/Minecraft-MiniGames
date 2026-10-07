package io.github.leejihoon1592.minigames.game.path;

public class PathSegment {

    private final double startX;
    private final double startY;

    private final double endX;
    private final double endY;

    private final double length;

    public PathSegment(
            double startX,
            double startY,
            double endX,
            double endY
    ) {
        this.startX = startX;
        this.startY = startY;

        this.endX = endX;
        this.endY = endY;

        double deltaX = endX - startX;
        double deltaY = endY - startY;

        this.length = Math.sqrt(
                deltaX * deltaX
                        + deltaY * deltaY
        );

        if (length <= 0.0) {
            throw new IllegalArgumentException(
                    "PathSegment length must be greater than 0"
            );
        }
    }

    /*
     * =========================
     * 구간 위의 위치 계산
     * =========================
     *
     * progress:
     *
     * 0.0 = 시작점
     * 0.5 = 중앙
     * 1.0 = 끝점
     */

    public Point getPointAt(double progress) {

        double clampedProgress =
                Math.max(
                        0.0,
                        Math.min(1.0, progress)
                );

        double x =
                startX
                        + (endX - startX)
                        * clampedProgress;

        double y =
                startY
                        + (endY - startY)
                        * clampedProgress;

        return new Point(x, y);
    }

    /*
     * =========================
     * 마우스에서 중앙선까지
     * 가장 가까운 지점 계산
     * =========================
     */

    public ClosestPoint findClosestPoint(
            double pointX,
            double pointY
    ) {

        double segmentX = endX - startX;
        double segmentY = endY - startY;

        double pointOffsetX = pointX - startX;
        double pointOffsetY = pointY - startY;

        double lengthSquared =
                segmentX * segmentX
                        + segmentY * segmentY;

        double progress =
                (
                        pointOffsetX * segmentX
                                + pointOffsetY * segmentY
                )
                        / lengthSquared;

        progress =
                Math.max(
                        0.0,
                        Math.min(1.0, progress)
                );

        Point closestPoint =
                getPointAt(progress);

        double distanceX =
                pointX - closestPoint.x();

        double distanceY =
                pointY - closestPoint.y();

        double distance =
                Math.sqrt(
                        distanceX * distanceX
                                + distanceY * distanceY
                );

        return new ClosestPoint(
                closestPoint,
                progress,
                distance
        );
    }

    /*
     * =========================
     * 기본 정보
     * =========================
     */

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getLength() {
        return length;
    }

    /*
     * =========================
     * 좌표
     * =========================
     */

    public record Point(
            double x,
            double y
    ) {
    }

    /*
     * =========================
     * 가장 가까운 중앙선 정보
     * =========================
     */

    public record ClosestPoint(
            Point point,
            double progress,
            double distance
    ) {
    }
}