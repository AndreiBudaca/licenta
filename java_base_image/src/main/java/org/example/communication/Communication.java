package org.example.communication;

public interface Communication {
    String getMessage();
    void sendMessage(String message);

    void sendLog(String message);
}
