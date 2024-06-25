package org.example.configuration;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ModuleConfiguration {
    public static ConcurrentLinkedQueue<RunningModule> runningModules = new ConcurrentLinkedQueue<>();

    public static char separator = ';';
}
