package org.example.TTLQueue;

public class TTLElement<T extends TTLFindable<K>, K> {
    private final T element;
    private long ttl;

    public TTLElement(T element, long ttl) {
        this.element = element;
        this.ttl = ttl;
    }

    public boolean hasIdentifier(K id) {
        return element.hasIdentifier(id);
    }

    public K getIdentifier() { return element.getIdentifier(); }

    public T getElement() {
        return element;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
