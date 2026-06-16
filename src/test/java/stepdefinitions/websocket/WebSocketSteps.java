package stepdefinitions.websocket;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import stepdefinitions.BaseSteps;

public class WebSocketSteps extends BaseSteps {

    @When("^connect to websocket (.+)$")
    public void connect_to_websocket(String endpoint) {
        webSocketActions().connectToWebSocket(endpoint);
    }

    @When("^subscribe to websocket topic (.+)$")
    public void subscribe_to_websocket_topic(String topic) {
        webSocketActions().subscribeToTopic(topic);
    }

    @When("^send websocket message (.+)$")
    public void send_websocket_message(String message) {
        webSocketActions().sendWebSocketMessage(message);
    }

    @Then("^wait for websocket message within (\\d+) seconds$")
    public void wait_for_websocket_message_within_seconds(int timeoutSeconds) {
        webSocketActions().waitForWebSocketMessage(timeoutSeconds);
    }

    @Then("^assert websocket message contains (.+)$")
    public void assert_websocket_message_contains(String expectedValue) {
        webSocketActions().assertWebSocketMessageContains(expectedValue);
    }

    @Then("^assert websocket message field (\\S+) equals (.+)$")
    public void assert_websocket_message_field_equals(String fieldName, String expectedValue) {
        webSocketActions().assertWebSocketMessageFieldEquals(fieldName, expectedValue);
    }

    @When("^save websocket message field (\\S+) for later in variable (\\S+)$")
    public void save_websocket_message_field_for_later(String fieldName, String variableName) {
        webSocketActions().saveWebSocketMessageField(fieldName, variableName);
    }

    @When("^close websocket connection$")
    public void close_websocket_connection() {
        webSocketActions().closeWebSocketConnection();
    }
}