package core.websocket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebSocketMessageStore {

    private volatile String lastMessage;
    private volatile boolean messageReceived;
    private CountDownLatch latch = new CountDownLatch(1);

    public void storeMessage(String message) {
        this.lastMessage = message;
        this.messageReceived = true;
        this.latch.countDown();
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isMessageReceived() {
        return messageReceived;
    }

    public boolean waitForMessage(int timeoutSeconds) {
        try {
            return latch.await(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void reset() {
        this.lastMessage = null;
        this.messageReceived = false;
        this.latch = new CountDownLatch(1);
    }
}