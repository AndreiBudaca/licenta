package org.example;

public class Main {

    public static void main(String[] args) {
        Aggregator agg = new Aggregator();

        try {
            agg.HandleTasks();
        }
        catch (Exception e) {
            agg.StopHandling();
        }
    }
}