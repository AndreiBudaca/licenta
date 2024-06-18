package org.example.TTLQueue;

public interface TTLFindable<K> {
    boolean hasIdentifier(K id);
    K getIdentifier();
}
