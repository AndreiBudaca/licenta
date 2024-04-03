package org.faas;

import java.util.Objects;

public class Main {
    public String main(String arg) {
        return Objects.requireNonNullElse(arg, "Input was empty");
    }
}