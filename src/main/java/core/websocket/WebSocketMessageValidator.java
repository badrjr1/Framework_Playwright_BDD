package core.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.data.DynamicValueResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketMessageValidator {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageValidator.class);

    private final WebSocketMessageStore messageStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketMessageValidator(WebSocketMessageStore messageStore) {
        this.messageStore = messageStore;
    }

    public void assertMessageReceived() {
        assertTrue(
                messageStore.isMessageReceived(),
                "No WebSocket message was received"
        );
    }

    public void assertValidJson() {
        String message = getLastMessageOrFail();

        try {
            objectMapper.readTree(message);
            logger.info("[WS-VALIDATOR] Message is valid JSON");
        } catch (Exception e) {
            fail("WebSocket message is not valid JSON: " + message);
        }
    }

    public void assertContains(String expectedValue) {
        String message = getLastMessageOrFail();
        String resolvedExpectedValue = DynamicValueResolver.resolve(expectedValue);

        assertTrue(
                message.contains(resolvedExpectedValue),
                "WebSocket message does not contain expected value. Expected: "
                        + resolvedExpectedValue
                        + " | Actual: "
                        + message
        );
    }

    public void assertContainsField(String fieldName) {
        JsonNode root = getJsonMessage();

        assertTrue(
                root.has(fieldName),
                "WebSocket JSON message does not contain field: " + fieldName
        );
    }

    public void assertFieldEquals(String fieldName, String expectedValue) {
        JsonNode root = getJsonMessage();
        String resolvedExpectedValue = DynamicValueResolver.resolve(expectedValue);

        assertTrue(
                root.has(fieldName),
                "WebSocket JSON message does not contain field: " + fieldName
        );

        String actualValue = root.get(fieldName).asText();

        assertEquals(
                resolvedExpectedValue,
                actualValue,
                "Invalid WebSocket field value for field: " + fieldName
        );
    }

    public String getFieldValue(String fieldName) {
        JsonNode root = getJsonMessage();

        assertTrue(
                root.has(fieldName),
                "WebSocket JSON message does not contain field: " + fieldName
        );

        return root.get(fieldName).asText();
    }

    private JsonNode getJsonMessage() {
        String message = getLastMessageOrFail();

        try {
            return objectMapper.readTree(message);
        } catch (Exception e) {
            throw new RuntimeException("WebSocket message is not valid JSON: " + message, e);
        }
    }

    private String getLastMessageOrFail() {
        String message = messageStore.getLastMessage();

        if (message == null || message.isBlank()) {
            throw new AssertionError("No WebSocket message found");
        }

        return message;
    }
}