package org.example.jobs;

import org.example.communication.RedisCommunication;
import org.example.configuration.ModuleConfiguration;
import org.example.configuration.RunningModule;

import java.util.Arrays;
import java.util.Base64;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JobRunner {

    private final RedisCommunication redis;
    private boolean working = true;
    private ConcurrentLinkedQueue<Job> jobs;
    private final Random random;

    public JobRunner() {
        this.redis = new RedisCommunication();
        this.random = new Random();
        jobs = new ConcurrentLinkedQueue<>();

        new Thread(this::run).start();
    }

    public void schedule(Job job) {
        jobs.add(job);
    }

    public void stop() {
        working = false;
    }

    private void run() {
        while (working) {
            if (jobs.isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.out.println("Failed to wait for new job");
                }
                continue;
            }

            Job job = jobs.poll();
            executeJob(job);
        }
    }

    private void executeJob(Job job) {
        if (job == null) return;

        byte[] buffer = new byte[255];
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < job.getRequests(); i++) {
            long currentTime = System.currentTimeMillis();
            int jobId = random.nextInt();
            random.nextBytes(buffer);

            redis.sendLog(String.format("%d%c%d", jobId, ModuleConfiguration.separator, currentTime));

            for (RunningModule module : ModuleConfiguration.runningModules) {
                builder.setLength(0);

                builder.append(jobId);

                for (int layer : module.getDataLayers()) {
                    builder.append(ModuleConfiguration.separator);
                    builder.append(layer);
                    builder.append(ModuleConfiguration.separator);
                    builder.append(new String(Base64.getEncoder().encode(buffer)));
                }

                redis.sendMessage(module.getQueueName(), builder.toString());
            }

            long processingTime = System.currentTimeMillis() - currentTime;
            int delay = job.getDelay().getNextDelay((double) i / job.getRequests());

            try {
                Thread.sleep(Math.max(delay - processingTime, 0));
            } catch (Exception e) {
                System.out.println("Failed to wait for new job");
            }
        }
    }
}
