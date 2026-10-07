package io.github.leejihoon1592.minigames.game.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Path {

    private final List<PathSegment> segments;
    private final double totalLength;

    public Path(List<PathSegment> segments) {

        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException(
                    "Path must contain at least one segment"
            );
        }

        this.segments =
                Collections.unmodifiableList(
                        new ArrayList<>(segments)
                );

        double length = 0.0;

        for (PathSegment segment : this.segments) {

            if (segment == null) {
                throw new IllegalArgumentException(
                        "Path segments must not contain null"
                );
            }

            length += segment.getLength();
        }

        this.totalLength = length;
    }

    /*
     * =========================
     * 마우스에서 가장 가까운
     * 전체 Path 중앙선 찾기
     * =========================
     */

    public ClosestPathPoint findClosestPoint(
            double pointX,
            double pointY
    ) {

        PathSegment.ClosestPoint bestPoint = null;

        int bestSegmentIndex = -1;

        double bestDistance =
                Double.POSITIVE_INFINITY;

        double lengthBeforeBestSegment = 0.0;
        double lengthBeforeCurrentSegment = 0.0;

        for (int i = 0; i < segments.size(); i++) {

            PathSegment segment =
                    segments.get(i);

            PathSegment.ClosestPoint candidate =
                    segment.findClosestPoint(
                            pointX,
                            pointY
                    );

            if (candidate.distance() < bestDistance) {

                bestPoint = candidate;
                bestSegmentIndex = i;
                bestDistance = candidate.distance();

                lengthBeforeBestSegment =
                        lengthBeforeCurrentSegment;
            }

            lengthBeforeCurrentSegment +=
                    segment.getLength();
        }

        if (bestPoint == null) {
            throw new IllegalStateException(
                    "Unable to find closest point"
            );
        }

        PathSegment bestSegment =
                segments.get(bestSegmentIndex);

        double distanceAlongSegment =
                bestSegment.getLength()
                        * bestPoint.progress();

        double distanceAlongPath =
                lengthBeforeBestSegment
                        + distanceAlongSegment;

        double pathProgress =
                distanceAlongPath
                        / totalLength;

        return new ClosestPathPoint(
                bestPoint.point(),
                bestSegmentIndex,
                bestPoint.progress(),
                pathProgress,
                distanceAlongPath,
                bestPoint.distance()
        );
    }

    /*
     * =========================
     * 전체 진행도에 해당하는
     * 중앙선 좌표 찾기
     * =========================
     *
     * progress:
     *
     * 0.0 = 전체 Path 시작
     * 0.5 = 전체 Path 절반
     * 1.0 = 전체 Path 끝
     */

    public PathSegment.Point getPointAtProgress(
            double progress
    ) {

        double clampedProgress =
                Math.max(
                        0.0,
                        Math.min(1.0, progress)
                );

        double targetDistance =
                totalLength
                        * clampedProgress;

        double accumulatedLength = 0.0;

        for (int i = 0; i < segments.size(); i++) {

            PathSegment segment =
                    segments.get(i);

            double segmentLength =
                    segment.getLength();

            double segmentEndDistance =
                    accumulatedLength
                            + segmentLength;

            /*
             * 목표 지점이 현재 Segment 안에 있음
             */

            if (targetDistance <= segmentEndDistance) {

                double distanceInsideSegment =
                        targetDistance
                                - accumulatedLength;

                double segmentProgress =
                        distanceInsideSegment
                                / segmentLength;

                return segment.getPointAt(
                        segmentProgress
                );
            }

            accumulatedLength =
                    segmentEndDistance;
        }

        /*
         * 부동소수점 오차 등으로
         * 마지막까지 도달했을 경우
         */

        PathSegment lastSegment =
                segments.get(
                        segments.size() - 1
                );

        return lastSegment.getPointAt(1.0);
    }

    /*
     * =========================
     * 기본 정보
     * =========================
     */

    public List<PathSegment> getSegments() {
        return segments;
    }

    public double getTotalLength() {
        return totalLength;
    }

    public int getSegmentCount() {
        return segments.size();
    }

    /*
     * =========================
     * 전체 Path에서 가장 가까운
     * 중앙선 정보
     * =========================
     */

    public record ClosestPathPoint(

            PathSegment.Point point,

            int segmentIndex,

            double segmentProgress,

            double pathProgress,

            double distanceAlongPath,

            double distance

    ) {
    }
}