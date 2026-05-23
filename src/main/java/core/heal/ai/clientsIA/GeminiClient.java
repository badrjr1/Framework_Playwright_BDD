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

public class GeminiClient implements AiClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);

    private static final String GEMINI_BASE_URL = ConfigReader.get("ai.gemini.base.url");

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        logger.info("[GEMINI] GeminiClient initialized with model: {}", model);
    }

    @Override
    public List<String> generateLocator(String prompt) {
        try {
            logger.info("[GEMINI] Sending locator generation request");

            String url = GEMINI_BASE_URL.formatted(model, apiKey);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.error(
                        "[GEMINI] API error | status: {} | body: {}",
                        response.statusCode(),
                        response.body()
                );

                throw new RuntimeException(
                        "Erreur Gemini API. Status: " + response.statusCode()
                );
            }

            String locator = extractLocator(response.body());

            logger.info("[GEMINI] Locator generated: {}", locator);

            return cleanLocators(locator);

        } catch (Exception e) {
            logger.error("[GEMINI] Failed to generate locator", e);
            throw new RuntimeException("Erreur lors de l'appel Gemini", e);
        }
    }

    private String extractLocator(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        JsonNode candidates = root.path("candidates");

        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("Aucun candidate retourné par Gemini");
        }

        JsonNode parts = candidates
                .get(0)
                .path("content")
                .path("parts");

        if (!parts.isArray() || parts.isEmpty()) {
            throw new RuntimeException("Aucune part retournée par Gemini");
        }

        JsonNode textNode = parts.get(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Texte Gemini vide");
        }

        return textNode.asText();
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
