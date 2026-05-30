package stepdefinitions.websocket;

import core.websocket.WebSocketClientManager;
import io.cucumber.java.After;
import io.cucumber.java.en.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebsocketSteps {

    private WebSocketClientManager wsClient;

    @Given("connect to websocket")
    public void connect_to_websocket(String string) {
        wsClient = new WebSocketClientManager();
        wsClient.connect(string);
    }
    @When("send websocket message {string}")
    public void send_websocket_message(String string) {
        wsClient.sendMessage(string);
    }
    @Then("websocket message should contain {string}")
    public void websocket_message_should_contain(String expectedMessage) {
        boolean isMessageReceived = wsClient.waitForMessage(expectedMessage, 10);

        assertTrue(
                isMessageReceived,
                "Expected WebSocket message containing: " + expectedMessage
                        + " but received: " + wsClient.getReceivedMessages()
        );
    }
    @After("@websocket")
    public void closeWebSocketConnection() {
        if (wsClient != null) {
            wsClient.close();
        }
    }
}
