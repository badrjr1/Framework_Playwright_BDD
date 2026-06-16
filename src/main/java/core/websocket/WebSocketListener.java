package core.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketListener implements WebSocket.Listener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketListener.class);

    private final WebSocketMessageStore messageStore;

    public WebSocketListener(WebSocketMessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        logger.info("[WS-LISTENER] WebSocket opened");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(
            WebSocket webSocket,
            CharSequence data,
            boolean last
    ) {
        String message = data.toString();

        logger.info("[WS-LISTENER] Message received: {}", message);

        messageStore.storeMessage(message);

        webSocket.request(1);

        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        logger.error("[WS-LISTENER] WebSocket error: {}", error.getMessage(), error);
    }
}