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
    private final Object lock = new Object();

    public JobRunner() {
        this.redis = new RedisCommunication();
        this.random = new Random();
        jobs = new ConcurrentLinkedQueue<>();

        new Thread(this::run).start();
    }

    public void schedule(Job job) {
        jobs.add(job);

        synchronized (lock) {
            lock.notify();
        }
    }

    public void stop() {
        working = false;
    }

    private void run() {
        while (working) {
            if (jobs.isEmpty()) {
                try {
                    synchronized (lock) {
                        lock.wait();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Failed to wait for new job");
                    continue;
                }
            }

            Job job = jobs.poll();
            executeJob(job);
        }
    }

    private void executeJob(Job job) {
        if (job == null) return;

        for (int i = 0; i < job.getRequests(); i++) {
            long currentTime = System.currentTimeMillis();
            int taskId = random.nextInt();

            redis.sendLog(String.format("%d traffic_interceptor %d", taskId, currentTime));
            redis.sendAlert(taskId);

            for (RunningModule module : ModuleConfiguration.runningModules) {
                sendData(module, taskId);
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

    private void sendData(RunningModule module, int taskId) {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[255];
        random.nextBytes(buffer);
        builder.append(taskId);

        for (int layer : module.getDataLayers()) {
            builder.append(ModuleConfiguration.separator);
            builder.append(layer);
            builder.append(ModuleConfiguration.separator);
            builder.append(new String(Base64.getEncoder().encode(buffer)));
        }

        redis.sendMessage(module.getQueueName(), builder.toString());
    }
}
