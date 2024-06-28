package org.example.TTLQueue;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TTLList<T extends TTLFindable<K>, K> {
    private final long initialTTL;
    private final int maxSize;
    private final ConcurrentLinkedQueue<TTLElement<T, K>> list;
    private final TTLElementHandler<T> handler;
    private final Thread handlerThread;
    private boolean watchTTL;

    public TTLList(long ttl, int maxSize, TTLElementHandler<T> handler) {
        this.initialTTL = ttl;
        this.maxSize = maxSize;
        this.handler = handler;
        this.list = new ConcurrentLinkedQueue<>();

        handlerThread = new Thread(this::handleElements);
        startHandling();
    }

    public void addElement(T element) {
        if (list.size() < maxSize) {
            list.add(new TTLElement<>(element, initialTTL));
        }
    }

    public T findElement(K id) {
        TTLElement<T, K> element = list.stream().filter(it -> it.hasIdentifier(id)).findFirst().orElse(null);

        if (element == null) return null;
        return element.getElement();
    }

    public void updateElement(T element) {
        TTLElement<T, K> queueElement = list.stream()
                .filter(it -> it.hasIdentifier(element.getIdentifier()))
                .findFirst().orElse(null);

        if (queueElement == null) return;

        list.remove(queueElement);
        list.add(new TTLElement<>(element, queueElement.getTtl()));
    }

    public void deleteElement(K id) {
        list.stream()
                .filter(it -> it.hasIdentifier(id))
                .findFirst().ifPresent(list::remove);
    }

    public void stopHandling() {
        watchTTL = false;
    }

    public void startHandling() {
        try {
            handlerThread.start();
            watchTTL = true;
        } catch (IllegalThreadStateException exp) {
            System.out.println("Thread was already running...");
        }
    }

    private void handleElements() {
        long minSleep = 10;

        long lastTime = System.currentTimeMillis();
        long currentTime = System.currentTimeMillis();
        while (watchTTL) {
            long timeDiff = currentTime - lastTime;

            for (TTLElement<T, K> element : list) {
                element.setTtl(element.getTtl() - timeDiff);

                if (element.getTtl() <= 0) {
                    list.remove(element);
                    handler.handleElement(element.getElement());
                }
            }

            if (timeDiff < minSleep) {
                try {
                    Thread.sleep(minSleep - timeDiff);
                } catch (Exception e) {
                    System.err.println("Failed to sleep...");
                }
            }

            lastTime = currentTime;
            currentTime = System.currentTimeMillis();
        }
    }
}
