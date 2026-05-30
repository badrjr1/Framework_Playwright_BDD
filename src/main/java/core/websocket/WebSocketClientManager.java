package core.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WebSocketClientManager implements WebSocket.Listener {

    private WebSocket webSocket;
    private final List<String> receivedMessages = new ArrayList<>();
    private final CountDownLatch messageLatch = new CountDownLatch(1);

    public void connect(String wsUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            this.webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(wsUrl), this)
                    .join();

            System.out.println("WebSocket connected to: " + wsUrl);

        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to WebSocket: " + wsUrl, e);
        }
    }

    public void sendMessage(String message) {
        if (webSocket == null) {
            throw new RuntimeException("WebSocket is not connected");
        }

        webSocket.sendText(message, true).join();
    }

    public boolean waitForMessage(String expectedMessage, int timeoutInSeconds) {
        try {
            boolean messageReceived = messageLatch.await(timeoutInSeconds, TimeUnit.SECONDS);

            if (!messageReceived) {
                return false;
            }

            return receivedMessages.stream()
                    .anyMatch(message -> message.contains(expectedMessage));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for WebSocket message", e);
        }
    }

    public boolean hasReceivedMessageContaining(String expectedMessage) {
        return receivedMessages.stream()
                .anyMatch(message -> message.contains(expectedMessage));
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }

    public void close() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Test finished").join();
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket connection opened");
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();

        System.out.println("WebSocket message received: " + message);

        receivedMessages.add(message);
        messageLatch.countDown();

        webSocket.request(1);

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + statusCode + " - " + reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        WebSocket.Listener.super.onError(webSocket, error);
    }
}