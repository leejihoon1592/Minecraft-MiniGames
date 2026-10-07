package io.github.leejihoon1592.minigames.game.path;

public class PathResult {

    private final double accuracy;

    private final int checkpointsPassed;
    private final int checkpointsDisabled;
    private final int totalCheckpoints;

    private final int missCount;

    private final boolean completed;

    public PathResult(
            double accuracy,
            int checkpointsPassed,
            int checkpointsDisabled,
            int totalCheckpoints,
            int missCount,
            boolean completed
    ) {

        this.accuracy = accuracy;

        this.checkpointsPassed = checkpointsPassed;
        this.checkpointsDisabled = checkpointsDisabled;
        this.totalCheckpoints = totalCheckpoints;

        this.missCount = missCount;

        this.completed = completed;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int getCheckpointsPassed() {
        return checkpointsPassed;
    }

    public int getCheckpointsDisabled() {
        return checkpointsDisabled;
    }

    public int getTotalCheckpoints() {
        return totalCheckpoints;
    }

    public int getMissCount() {
        return missCount;
    }

    public boolean isCompleted() {
        return completed;
    }
}