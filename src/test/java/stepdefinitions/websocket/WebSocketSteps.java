package stepdefinitions.websocket;

import core.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebSocketSteps {

    private WebSocket webSocket;
    private final StringBuilder receivedMessages = new StringBuilder();
    private CountDownLatch messageLatch;

    @When("websocket endpoint is available (.+)$")
    public void websocket_endpoint_is_available(String websocketEndpoint) {
        TestContext.put("websocketEndpoint", websocketEndpoint);
    }

    @When("subscribe to websocket topic (.+)$")
    public void subscribe_to_websocket_topic(String topic) {
        try {
            String baseUrl = TestContext.getString("apiBaseUrl");
            String websocketEndpoint = TestContext.getString("websocketEndpoint");

            String wsUrl = baseUrl
                    .replace("https://", "wss://")
                    .replace("http://", "ws://")
                    + websocketEndpoint;

            messageLatch = new CountDownLatch(1);

            webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {

                        @Override
                        public void onOpen(WebSocket webSocket) {
                            webSocket.request(1);
                        }

                        @Override
                        public CompletionStage<?> onText(
                                WebSocket webSocket,
                                CharSequence data,
                                boolean last
                        ) {
                            receivedMessages.append(data);
                            messageLatch.countDown();
                            webSocket.request(1);
                            return null;
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable error) {
                            throw new RuntimeException("WebSocket error", error);
                        }
                    })
                    .join();

            TestContext.put("websocketTopic", topic);

            /*
             * Si ton backend utilise STOMP, ici il faut envoyer une frame SUBSCRIBE.
             * Pour WebSocket simple, cette partie peut rester comme ça.
             */
            webSocket.sendText(topic, true);

        } catch (Exception e) {
            throw new RuntimeException("Failed to connect/subscribe to WebSocket", e);
        }
    }

    @Then("assert websocket message contains (.+)$")
    public void assert_websocket_message_contains(String expectedMessage) {
        String actualMessage = receivedMessages.toString();

        assertTrue(
                actualMessage.contains(expectedMessage),
                "WebSocket message does not contain expected value. Expected: "
                        + expectedMessage
                        + " | Actual: "
                        + actualMessage
        );
    }

    @Then("wait for websocket message within (\\d+) seconds$")
    public void wait_for_websocket_message_within_seconds(int seconds) throws InterruptedException {
        if (messageLatch != null) {
            messageLatch.await(seconds, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("WebSocket message latch is not initialized");
        }
    }
}