package org.faas;

import java.util.Objects;
import java.util.Random;

public class Main {
    public static String echo(String arg) throws InterruptedException {
        Random rand = new Random();
        Thread.sleep(50 + rand.nextInt(150));
        return Objects.requireNonNullElse(arg, "Input was empty");
    }
}