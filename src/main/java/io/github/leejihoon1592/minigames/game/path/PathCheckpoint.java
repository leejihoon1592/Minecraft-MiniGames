package io.github.leejihoon1592.minigames.game.path;

public class PathCheckpoint {

    /*
     * Path 전체에서의 위치
     *
     * 0.0 = START
     * 1.0 = END
     */

    private final double progress;

    /*
     * 플레이어가 정상적으로
     * 이 체크포인트를 통과했는지
     */

    private boolean passed;

    /*
     * MISS 때문에 이번 게임에서
     * 획득할 수 없게 되었는지
     */

    private boolean disabled;

    public PathCheckpoint(double progress) {

        if (progress < 0.0 || progress > 1.0) {
            throw new IllegalArgumentException(
                    "progress must be between 0.0 and 1.0"
            );
        }

        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isPassed() {
        return passed;
    }

    public boolean isDisabled() {
        return disabled;
    }

    /*
     * 체크포인트 통과
     */

    public void pass() {

        if (disabled) {
            return;
        }

        passed = true;
    }

    /*
     * MISS로 체크포인트 비활성화
     */

    public void disable() {

        /*
         * 이미 획득한 체크포인트는
         * 빼앗지 않는다.
         */

        if (passed) {
            return;
        }

        disabled = true;
    }

    /*
     * 실제로 획득 가능한 상태인지
     */

    public boolean isAvailable() {
        return !passed && !disabled;
    }
}