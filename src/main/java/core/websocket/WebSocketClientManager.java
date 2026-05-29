package core.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

public class WebSocketClientManager {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientManager.class);

    private final HttpClient httpClient;
    private final WebSocketMessageStore messageStore;

    private WebSocket webSocket;

    public WebSocketClientManager(WebSocketMessageStore messageStore) {
        this.httpClient = HttpClient.newHttpClient();
        this.messageStore = messageStore;
    }

    public void connect(String url) {
        try {
            messageStore.reset();

            logger.info("[WS-CLIENT] Connecting to WebSocket: {}", url);

            this.webSocket = httpClient
                    .newWebSocketBuilder()
                    .buildAsync(
                            URI.create(url),
                            new WebSocketListener(messageStore)
                    )
                    .join();

            logger.info("[WS-CLIENT] Connected successfully");

        } catch (Exception e) {
            logger.error("[WS-CLIENT] Connection failed | url: {} | reason: {}", url, e.getMessage(), e);
            throw new RuntimeException("WebSocket connection failed: " + url, e);
        }
    }

    public void connectWithToken(String url, String token) {
        try {
            messageStore.reset();

            logger.info("[WS-CLIENT] Connecting to WebSocket with token: {}", url);

            this.webSocket = httpClient
                    .newWebSocketBuilder()
                    .header("Authorization", "Bearer " + token)
                    .buildAsync(
                            URI.create(url),
                            new WebSocketListener(messageStore)
                    )
                    .join();

            logger.info("[WS-CLIENT] Connected successfully with token");

        } catch (Exception e) {
            logger.error("[WS-CLIENT] Connection with token failed | url: {} | reason: {}", url, e.getMessage(), e);
            throw new RuntimeException("WebSocket connection with token failed: " + url, e);
        }
    }

    public void send(String message) {
        if (webSocket == null) {
            throw new RuntimeException("WebSocket is not connected. Please connect before sending message.");
        }

        logger.info("[WS-CLIENT] Sending message: {}", message);

        webSocket.sendText(message, true).join();
    }

    public boolean waitForMessage(int timeoutSeconds) {
        return messageStore.waitForMessage(timeoutSeconds);
    }

    public void close() {
        if (webSocket != null) {
            logger.info("[WS-CLIENT] Closing WebSocket connection");

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closed by test framework").join();

            webSocket = null;
        }
    }
}