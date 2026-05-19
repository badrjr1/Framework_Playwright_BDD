package core.heal.ai.clientsIA;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ConfigReader;
import core.heal.ai.AiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class OpenAiClient implements AiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiClient.class);

    private static final String OPENAI_RESPONSES_URL = ConfigReader.get("OPENAI_RESPONSES_URL");

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        logger.info("[OPENAI] OpenAiClient initialized with model: {}", model);
    }

    @Override
    public List<String> generateLocator(String prompt) {
        try {
            logger.info("[OPENAI] Sending locator generation request");

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "input", prompt
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_RESPONSES_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error(
                        "[OPENAI] API error | status: {} | body: {}",
                        response.statusCode(),
                        response.body()
                );

                throw new RuntimeException(
                        "Erreur OpenAI API. Status: " + response.statusCode()
                );
            }

            String locator = extractLocator(response.body());

            logger.info("[OPENAI] Locator generated: {}", locator);

            return cleanLocators(locator);

        } catch (Exception e) {
            logger.error("[OPENAI] Failed to generate locator", e);
            throw new RuntimeException("Erreur lors de l'appel OpenAI", e);
        }
    }

    private String extractLocator(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // OpenAI Responses API can expose output_text in many cases.
        if (root.has("output_text")) {
            return root.get("output_text").asText();
        }

        // Fallback generic parsing: output[0].content[0].text
        JsonNode output = root.path("output");

        if (output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.path("content");

                if (content.isArray()) {
                    for (JsonNode contentItem : content) {
                        if (contentItem.has("text")) {
                            return contentItem.get("text").asText();
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Impossible d'extraire le locator depuis la réponse OpenAI");
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
