package core.actions.websocket;

import config.ConfigReader;
import core.context.TestContext;
import core.data.DynamicValueResolver;
import core.websocket.WebSocketClientManager;
import core.websocket.WebSocketMessageStore;
import core.websocket.WebSocketMessageValidator;
import core.websocket.WebSocketUrlBuilder;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketActions implements IWebSocketActions {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketActions.class);

    private final WebSocketMessageStore messageStore;
    private final WebSocketClientManager clientManager;
    private final WebSocketMessageValidator validator;

    public WebSocketActions() {
        this.messageStore = new WebSocketMessageStore();
        this.clientManager = new WebSocketClientManager(messageStore);
        this.validator = new WebSocketMessageValidator(messageStore);

        logger.info("[WS-ACTIONS] WebSocketActions initialized");
    }

    @Override
    public void connectToWebSocket(String endpoint) {
        Allure.step("Connect to WebSocket: " + endpoint, () -> {
            try {
                String wsBaseUrl = ConfigReader.get("ws.base.url");
                String resolvedEndpoint = DynamicValueResolver.resolve(endpoint);

                String finalUrl = WebSocketUrlBuilder.build(wsBaseUrl, resolvedEndpoint);

                logger.info("[WS] Connecting to WebSocket | endpoint: {} | finalUrl: {}", resolvedEndpoint, finalUrl);

                clientManager.connect(finalUrl);

                logger.info("[WS] WebSocket connected successfully | finalUrl: {}", finalUrl);

                Allure.addAttachment(
                        "WebSocket connection",
                        "text/plain",
                        """
                        Action: connectToWebSocket
                        Base URL: %s
                        Endpoint: %s
                        Final URL: %s
                        Status: CONNECTED
                        """.formatted(
                                wsBaseUrl,
                                resolvedEndpoint,
                                finalUrl
                        )
                );

            } catch (Exception e) {
                logger.error(
                        "[WS] WebSocket connection failed | endpoint: {} | reason: {}",
                        endpoint,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void subscribeToTopic(String topic) {
        Allure.step("Subscribe to WebSocket topic: " + topic, () -> {
            try {
                String resolvedTopic = DynamicValueResolver.resolve(topic);

                logger.info("[WS] Subscribing to topic: {}", resolvedTopic);

                clientManager.send(resolvedTopic);

                TestContext.put("wsTopic", resolvedTopic);

                logger.info("[WS] Topic subscription message sent successfully | topic: {}", resolvedTopic);

                Allure.addAttachment(
                        "WebSocket subscription",
                        "text/plain",
                        """
                        Action: subscribeToTopic
                        Topic: %s
                        Status: SENT
                        """.formatted(resolvedTopic)
                );

            } catch (Exception e) {
                logger.error(
                        "[WS] Subscribe to topic failed | topic: {} | reason: {}",
                        topic,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void sendWebSocketMessage(String message) {
        Allure.step("Send WebSocket message", () -> {
            try {
                String resolvedMessage = DynamicValueResolver.resolve(message);

                logger.info("[WS] Sending WebSocket message: {}", resolvedMessage);

                clientManager.send(resolvedMessage);

                logger.info("[WS] WebSocket message sent successfully");

                Allure.addAttachment(
                        "Sent WebSocket message",
                        "text/plain",
                        resolvedMessage
                );

            } catch (Exception e) {
                logger.error(
                        "[WS] Send WebSocket message failed | message: {} | reason: {}",
                        message,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void waitForWebSocketMessage(int timeoutSeconds) {
        Allure.step("Wait for WebSocket message within " + timeoutSeconds + " seconds", () -> {
            try {
                logger.info("[WS] Waiting for WebSocket message | timeout: {} seconds", timeoutSeconds);

                boolean received = clientManager.waitForMessage(timeoutSeconds);

                Allure.addAttachment(
                        "WebSocket wait result",
                        "text/plain",
                        """
                        Action: waitForWebSocketMessage
                        Timeout seconds: %s
                        Message received: %s
                        Last message: %s
                        """.formatted(
                                timeoutSeconds,
                                received,
                                messageStore.getLastMessage()
                        )
                );

                if (!received) {
                    logger.error("[WS] No WebSocket message received within {} seconds", timeoutSeconds);

                    throw new AssertionError(
                            "No WebSocket message received within " + timeoutSeconds + " seconds"
                    );
                }

                logger.info("[WS] WebSocket message received successfully | message: {}", messageStore.getLastMessage());

            } catch (Exception e) {
                logger.error(
                        "[WS] Wait for WebSocket message failed | timeout: {} | reason: {}",
                        timeoutSeconds,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void assertWebSocketMessageContains(String expectedValue) {
        Allure.step("Assert WebSocket message contains: " + expectedValue, () -> {
            try {
                String resolvedExpectedValue = DynamicValueResolver.resolve(expectedValue);

                logger.info("[WS-ASSERT] Checking WebSocket message contains: {}", resolvedExpectedValue);

                validator.assertContains(resolvedExpectedValue);

                logger.info("[WS-ASSERT] WebSocket message contains expected value: {}", resolvedExpectedValue);

                Allure.addAttachment(
                        "WebSocket contains assertion",
                        "text/plain",
                        """
                        Action: assertWebSocketMessageContains
                        Expected value: %s
                        Last message: %s
                        """.formatted(
                                resolvedExpectedValue,
                                messageStore.getLastMessage()
                        )
                );

            } catch (Exception e) {
                logger.error(
                        "[WS-ASSERT] WebSocket contains assertion failed | expected: {} | reason: {}",
                        expectedValue,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void assertWebSocketMessageFieldEquals(String fieldName, String expectedValue) {
        Allure.step("Assert WebSocket field " + fieldName + " equals " + expectedValue, () -> {
            try {
                String resolvedExpectedValue = DynamicValueResolver.resolve(expectedValue);

                logger.info(
                        "[WS-ASSERT] Checking WebSocket field equals | field: {} | expected: {}",
                        fieldName,
                        resolvedExpectedValue
                );

                validator.assertFieldEquals(fieldName, resolvedExpectedValue);

                logger.info(
                        "[WS-ASSERT] WebSocket field assertion passed | field: {} | expected: {}",
                        fieldName,
                        resolvedExpectedValue
                );

                Allure.addAttachment(
                        "WebSocket field assertion",
                        "text/plain",
                        """
                        Action: assertWebSocketMessageFieldEquals
                        Field: %s
                        Expected value: %s
                        Last message: %s
                        """.formatted(
                                fieldName,
                                resolvedExpectedValue,
                                messageStore.getLastMessage()
                        )
                );

            } catch (Exception e) {
                logger.error(
                        "[WS-ASSERT] WebSocket field assertion failed | field: {} | expected: {} | reason: {}",
                        fieldName,
                        expectedValue,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void saveWebSocketMessageField(String fieldName, String variableName) {
        Allure.step("Save WebSocket field " + fieldName + " in variable " + variableName, () -> {
            try {
                logger.info(
                        "[WS] Saving WebSocket message field | field: {} | variable: {}",
                        fieldName,
                        variableName
                );

                String fieldValue = validator.getFieldValue(fieldName);

                TestContext.put(variableName, fieldValue);

                logger.info(
                        "[WS] WebSocket field saved successfully | field: {} | variable: {} | value: {}",
                        fieldName,
                        variableName,
                        fieldValue
                );

                Allure.addAttachment(
                        "Saved WebSocket field",
                        "text/plain",
                        """
                        Action: saveWebSocketMessageField
                        Field: %s
                        Variable: %s
                        Value: %s
                        """.formatted(
                                fieldName,
                                variableName,
                                fieldValue
                        )
                );

            } catch (Exception e) {
                logger.error(
                        "[WS] Save WebSocket field failed | field: {} | variable: {} | reason: {}",
                        fieldName,
                        variableName,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void closeWebSocketConnection() {
        Allure.step("Close WebSocket connection", () -> {
            try {
                logger.info("[WS] Closing WebSocket connection");

                clientManager.close();

                logger.info("[WS] WebSocket connection closed successfully");

                Allure.addAttachment(
                        "WebSocket close",
                        "text/plain",
                        "WebSocket connection closed successfully"
                );

            } catch (Exception e) {
                logger.error("[WS] Close WebSocket connection failed | reason: {}", e.getMessage(), e);
                throw e;
            }
        });
    }
}