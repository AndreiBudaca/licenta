package org.example;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Aggregator agg = new Aggregator();

        try {
            agg.HandleTasks();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(Arrays.toString(e.getStackTrace()));
            agg.StopHandling();
        }
    }
}