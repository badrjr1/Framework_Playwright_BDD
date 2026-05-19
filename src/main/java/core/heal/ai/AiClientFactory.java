package core.heal.ai;

import config.ConfigReader;
import core.heal.ai.clientsIA.ClaudeAiClient;
import core.heal.ai.clientsIA.GeminiClient;
import core.heal.ai.clientsIA.OpenAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(AiClientFactory.class);

    private AiClientFactory() {
        // Utility class
    }

    public static AiClient create() {
        String openAiKey = getOptional("OPENAI_API_KEY");
        String geminiKey = getOptional("GEMINI_API_KEY");
        String claudeKey = getOptional("CLAUDE_API_KEY");

        if (isFilled(openAiKey)) {
            logger.info("[AI-FACTORY] OpenAI API key detected. Using OpenAI client.");
            return new OpenAiClient(
                    openAiKey,
                    getOptionalOrDefault("OPENAI_MODEL", "gpt-4.1-mini"));
        }

        if (isFilled(geminiKey)) {
            logger.info("[AI-FACTORY] Gemini API key detected. Using Gemini client.");
            return new GeminiClient(
                    geminiKey,
                    getOptionalOrDefault("GEMINI_MODEL", "gemini-2.5-flash"));
        }

        if (isFilled(claudeKey)) {
            logger.info("[AI-FACTORY] Claude API key detected. Using Claude client.");
            return new ClaudeAiClient(
                    claudeKey,
                    getOptionalOrDefault("CLAUDE_MODEL", "claude-3-5-haiku-latest")
            );
        }

        logger.error("[AI-FACTORY] No AI API key found in config.properties.");

        throw new RuntimeException(
                "Aucune clé IA trouvée. Remplir au moins une clé : OPENAI_API_KEY, GEMINI_API_KEY ou CLAUDE_API_KEY"
        );
    }

    private static String getOptional(String key) {
        try {
            return System.getenv(key);
        } catch (Exception e) {
            return "";
        }
    }

    private static String getOptionalOrDefault(String key, String defaultValue) {
        String value = getOptional(key);
        return isFilled(value) ? value : defaultValue;
    }

    private static boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }
}