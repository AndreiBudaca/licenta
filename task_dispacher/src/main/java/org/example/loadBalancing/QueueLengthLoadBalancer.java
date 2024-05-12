package org.example.loadBalancing;

public class QueueLengthLoadBalancer {
    private long previousQueueLength = 0;
    private long increaseStreak = 0;
    private long decreaseStreak = 0;
    private long overLimitStreak = 0;
    private long zeroStreak = 0;

    public int balance(long queueLength) {
        long streakDecisionThreshold = 10;
        long maxQueueLength = 10;

        if (queueLength > maxQueueLength) {
            ++overLimitStreak;
            increaseStreak = 0;
            decreaseStreak = 0;
            zeroStreak = 0;
        } else if (previousQueueLength == 0 && queueLength == 0) {
            ++zeroStreak;
            increaseStreak = 0;
            decreaseStreak = 0;
        }
        else {
            long diff = previousQueueLength - queueLength;

            if (diff < 0) {
                ++increaseStreak;
                zeroStreak = 0;
                decreaseStreak = 0;
            } else if (diff > 0) {
                ++decreaseStreak;
                zeroStreak = 0;
                increaseStreak = 0;
            }
        }

        previousQueueLength = queueLength;

        if (overLimitStreak == streakDecisionThreshold) {
            overLimitStreak = 0;
            return 1;
        }

        if (increaseStreak == streakDecisionThreshold) {
            increaseStreak = 0;
            return 1;
        }

        if (decreaseStreak == streakDecisionThreshold) {
            decreaseStreak = 0;
            return -1;
        }

        if (zeroStreak == streakDecisionThreshold) {
            zeroStreak = 0;
            return -1;
        }

        return 0;
    }
}
