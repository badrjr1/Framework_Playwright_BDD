package core.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class ApiClient {

    private String apiKey;

    public ApiClient(String baseUrl, String apiKey) {
        this.apiKey = apiKey;
        // Configure RestAssured base URI
        RestAssured.baseURI = baseUrl;
    }

    // Méthode GET
    public Response get(String endpoint, Map<String, String> queryParams) {
        RequestSpecification request = RestAssured.given();
        if (apiKey != null) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        if (queryParams != null) {
            request.queryParams(queryParams);
        }
        return request.get(endpoint);
    }

    // Méthode POST
    public Response post(String endpoint, Object body, Map<String, String> queryParams) {
        RequestSpecification request = RestAssured.given();
        if (apiKey != null) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        if (queryParams != null) {
            request.queryParams(queryParams);
        }
        request.body(body);
        return request.post(endpoint);
    }

    // Méthode PUT
    public Response put(String endpoint, Object body, Map<String, String> queryParams) {
        RequestSpecification request = RestAssured.given();
        if (apiKey != null) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        if (queryParams != null) {
            request.queryParams(queryParams);
        }
        request.body(body);
        return request.put(endpoint);
    }

    // Méthode DELETE
    public Response delete(String endpoint, Map<String, String> queryParams) {
        RequestSpecification request = RestAssured.given();
        if (apiKey != null) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        if (queryParams != null) {
            request.queryParams(queryParams);
        }
        return request.delete(endpoint);
    }

}
