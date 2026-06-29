package core.websocket;

public class WebSocketUrlBuilder {

    private WebSocketUrlBuilder() {
    }

    public static String build(String baseUrl, String endpoint) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException("WebSocket base URL is empty");
        }

        if (endpoint == null || endpoint.isBlank()) {
            throw new RuntimeException("WebSocket endpoint is empty");
        }

        String normalizedBaseUrl = baseUrl.trim();
        String normalizedEndpoint = endpoint.trim();

        if (normalizedBaseUrl.endsWith("/") && normalizedEndpoint.startsWith("/")) {
            return normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1)
                    + normalizedEndpoint;
        }

        if (!normalizedBaseUrl.endsWith("/") && !normalizedEndpoint.startsWith("/")) {
            return normalizedBaseUrl + "/" + normalizedEndpoint;
        }

        return normalizedBaseUrl + normalizedEndpoint;
    }
}