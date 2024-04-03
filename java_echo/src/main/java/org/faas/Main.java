package org.faas;

import java.util.Objects;
import java.util.Random;

public class Main {
    public String main(String arg) throws InterruptedException {
        Random rand = new Random();
        Thread.sleep(200 + rand.nextInt(800));
        return Objects.requireNonNullElse(arg, "Input was empty");
    }
}