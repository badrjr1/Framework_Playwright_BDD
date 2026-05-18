package core.heal.ai.clientsIA;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.heal.ai.AiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ClaudeAiClient implements AiClient {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeAiClient.class);

    private static final String CLAUDE_MESSAGES_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClaudeAiClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        logger.info("[CLAUDE] ClaudeAiClient initialized with model: {}", model);
    }

    @Override
    public List<String> generateLocator(String prompt) {
        try {
            logger.info("[CLAUDE] Sending locator generation request");

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", 100,
                    "temperature", 0,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CLAUDE_MESSAGES_URL))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error(
                        "[CLAUDE] API error | status: {} | body: {}",
                        response.statusCode(),
                        response.body()
                );

                throw new RuntimeException(
                        "Erreur Claude API. Status: " + response.statusCode()
                );
            }

            String locator = extractLocator(response.body());

            logger.info("[CLAUDE] Locator generated: {}", locator);

            return cleanLocators(locator);

        } catch (Exception e) {
            logger.error("[CLAUDE] Failed to generate locator", e);
            throw new RuntimeException("Erreur lors de l'appel Claude API", e);
        }
    }

    private String extractLocator(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode content = root.path("content");

        if (!content.isArray() || content.isEmpty()) {
            throw new RuntimeException("Aucun contenu retourné par Claude");
        }

        for (JsonNode item : content) {
            String type = item.path("type").asText();

            if ("text".equalsIgnoreCase(type)) {
                String text = item.path("text").asText();

                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }

        throw new RuntimeException("Impossible d'extraire le locator depuis la réponse Claude");
    }

    private List<String> cleanLocators(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return List.of();
        }

        return rawText
                .replace("```", "")
                .replace("java", "")
                .replace("gherkin", "")
                .trim()
                .lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.matches("^\\d+[\\).]\\s+.*"))
                .map(line -> line.replaceFirst("^[-*]\\s+", ""))
                .map(line -> line.replaceFirst("^\\d+[\\).]\\s*", ""))
                .filter(line -> line.contains("|"))
                .distinct()
                .limit(4)
                .toList();
    }
}
