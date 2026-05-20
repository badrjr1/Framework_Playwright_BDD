package stepdefinitions.api;

import core.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiSteps {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @When("api base url is (.+)$")
    public void api_base_url_is(String apiBaseUrl) {
        TestContext.put("apiBaseUrl", apiBaseUrl);
    }

    @When("call api GET (.+)$")
    public void call_api_get(String endpoint) throws Exception {
        String finalEndpoint = replaceVariables(endpoint);
        String apiBaseUrl = TestContext.getString("apiBaseUrl");

        String url = apiBaseUrl + finalEndpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TestContext.put("apiStatusCode", response.statusCode());
        TestContext.put("apiResponseBody", response.body());
    }

    @When("call api PATCH (.+) with body (.+)$")
    public void call_api_patch_with_body(String endpoint, String body) throws Exception {
        String finalEndpoint = replaceVariables(endpoint);
        String finalBody = replaceVariables(body);
        String apiBaseUrl = TestContext.getString("apiBaseUrl");

        String url = apiBaseUrl + finalEndpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(finalBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        TestContext.put("apiStatusCode", response.statusCode());
        TestContext.put("apiResponseBody", response.body());
    }

    @Then("assert api status code equals (\\d+)$")
    public void assert_api_status_code_equals(int expectedStatusCode) {
        int actualStatusCode = (int) TestContext.get("apiStatusCode");

        assertEquals(
                expectedStatusCode,
                actualStatusCode,
                "Invalid API status code"
        );
    }

    @Then("assert api response field (.+) equals (.+)$")
    public void assert_api_response_field_equals(String fieldName, String expectedValue) {
        String responseBody = TestContext.getString("apiResponseBody");

        assertTrue(
                responseBody.contains("\"" + fieldName + "\""),
                "API response does not contain field: " + fieldName
        );

        assertTrue(
                responseBody.contains(expectedValue),
                "API response field " + fieldName + " does not contain expected value: " + expectedValue
        );
    }

    private String replaceVariables(String text) {
        if (text == null) {
            return null;
        }

        String result = text;

        while (result.contains("${")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);

            if (end == -1) {
                break;
            }

            String variableName = result.substring(start + 2, end);
            String variableValue = TestContext.getString(variableName);

            if (variableValue == null) {
                throw new RuntimeException("Variable not found in TestContext: " + variableName);
            }

            result = result.substring(0, start)
                    + variableValue
                    + result.substring(end + 1);
        }

        return result;
    }
}