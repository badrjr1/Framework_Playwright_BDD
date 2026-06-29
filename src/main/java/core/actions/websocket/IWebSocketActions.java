package core.actions.websocket;

public interface IWebSocketActions {

    void connectToWebSocket(String endpoint);
    void subscribeToTopic(String topic);
    void sendWebSocketMessage(String message);
    void waitForWebSocketMessage(int timeoutSeconds);
    void assertWebSocketMessageContains(String expectedValue);
    void assertWebSocketMessageFieldEquals(String fieldName, String expectedValue);
    void saveWebSocketMessageField(String fieldName, String variableName);
    void closeWebSocketConnection();

}
