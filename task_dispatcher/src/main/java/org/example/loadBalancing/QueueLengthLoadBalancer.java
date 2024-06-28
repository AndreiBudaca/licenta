package org.example.loadBalancing;

public class QueueLengthLoadBalancer {
    private long previousQueueLength = 0;
    private long increaseStreak = 0;
    private long decreaseStreak = 0;
    private long overLimitStreak = 0;
    private long zeroStreak = 0;

    public int balance(long queueLength) {

        if (queueLength > EnvConfiguration.maxQueueLength) {
            ++overLimitStreak;
            increaseStreak = 0;
            decreaseStreak = 0;
            zeroStreak = 0;
        } else if (previousQueueLength == 0 && queueLength == 0) {
            ++zeroStreak;
            increaseStreak = 0;
            decreaseStreak = 0;
            overLimitStreak= 0;
        }
        else {
            long diff = previousQueueLength - queueLength;

            if (diff < 0) {
                ++increaseStreak;
                zeroStreak = 0;
                decreaseStreak = 0;
                overLimitStreak= 0;
            } else if (diff > 0) {
                ++decreaseStreak;
                zeroStreak = 0;
                increaseStreak = 0;
                overLimitStreak= 0;
            }
        }

        previousQueueLength = queueLength;

        if (overLimitStreak == EnvConfiguration.streakDecisionThreshold) {
            overLimitStreak = 0;
            return 1;
        }

        if (increaseStreak == EnvConfiguration.streakDecisionThreshold) {
            increaseStreak = 0;
            return 1;
        }

        if (decreaseStreak == EnvConfiguration.streakDecisionThreshold) {
            decreaseStreak = 0;
            return -1;
        }

        if (zeroStreak == EnvConfiguration.streakDecisionThreshold) {
            zeroStreak = 0;
            return -1;
        }

        return 0;
    }

    private static class EnvConfiguration {
        public final static int streakDecisionThreshold = System.getenv("STREAK_THRESHOLD") == null ?
                20 : Integer.parseInt(System.getenv("STREAK_THRESHOLD"));

        public final static int maxQueueLength = System.getenv("MAX_QUEUE_LENGTH") == null ?
                10 : Integer.parseInt(System.getenv("MAX_QUEUE_LENGTH"));
    }
}
