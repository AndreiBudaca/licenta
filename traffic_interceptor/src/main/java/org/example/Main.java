package org.example;

import org.example.communication.RedisCommunication;
import org.example.configuration.ModuleConfiguration;
import org.example.configuration.RunningModule;
import org.example.configuration.RunningModuleParser;
import org.example.jobs.Job;
import org.example.jobs.JobParser;
import org.example.jobs.JobRunner;

import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        RedisCommunication redis = new RedisCommunication();
        JobRunner runner = new JobRunner();

        while (true) {
            List<String> message = redis.getMessage();
            if (message == null) continue;

            String command = message.get(1);

            if (command.startsWith("quit")) {
                break;
            }

            else if (command.startsWith("job")) {
                Job job = JobParser.parse(command.substring(4));
                runner.schedule(job);

            } else if (command.startsWith("add_module")) {
                RunningModule module = RunningModuleParser.parse(command.substring(11));
                ModuleConfiguration.runningModules.add(module);

            } else if (command.startsWith("delete_module")) {
                String moduleName = command.substring(14);

                for (RunningModule module: ModuleConfiguration.runningModules) {
                    if (Objects.equals(module.getName(), moduleName)) {
                        ModuleConfiguration.runningModules.remove(module);
                        break;
                    }
                }
            }
        }

        runner.stop();
    }
}