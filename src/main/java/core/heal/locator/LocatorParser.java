package core.heal.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LocatorParser {

    private static final Logger logger = LoggerFactory.getLogger(LocatorParser.class);

    private LocatorParser() {
        // Utility class
    }

    public static List<LocatorDefinition> parse(String rawLocators) {
        logger.debug("[LOCATOR-PARSER] Start parsing raw locators: {}", rawLocators);

        List<LocatorDefinition> result = new ArrayList<>();

        if (rawLocators == null || rawLocators.trim().isEmpty()) {
            logger.warn("[LOCATOR-PARSER] Raw locators is null or empty");
            return result;
        }

        String[] locators = rawLocators.split(";");

        logger.debug("[LOCATOR-PARSER] Number of locator candidates found: {}", locators.length);

        for (String locator : locators) {
            String trimmedLocator = locator.trim();

            if (trimmedLocator.isEmpty()) {
                logger.warn("[LOCATOR-PARSER] Empty locator candidate ignored");
                continue;
            }

            try {
                LocatorDefinition definition = parseOne(trimmedLocator);
                result.add(definition);

                logger.debug(
                        "[LOCATOR-PARSER] Locator parsed successfully: {}",
                        definition.toStorageFormat()
                );

            } catch (Exception e) {
                logger.error(
                        "[LOCATOR-PARSER] Failed to parse locator candidate: {} | reason: {}",
                        trimmedLocator,
                        e.getMessage()
                );

                throw e;
            }
        }

        logger.info(
                "[LOCATOR-PARSER] Parsing completed. Valid locators count: {}",
                result.size()
        );

        return result;
    }

    public static LocatorDefinition parseOne(String rawLocator) {
        logger.debug("[LOCATOR-PARSER] Parsing single locator: {}", rawLocator);

        if (rawLocator == null || rawLocator.trim().isEmpty()) {
            logger.error("[LOCATOR-PARSER] Locator is null or empty");
            throw new RuntimeException("Locator vide ou null");
        }

        String[] parts = rawLocator.split("\\|");

        logger.debug(
                "[LOCATOR-PARSER] Locator parts count for '{}': {}",
                rawLocator,
                parts.length
        );

        if (parts.length == 2) {
            String type = parts[0].trim();
            String value = parts[1].trim();

            validatePart(type, "type", rawLocator);
            validatePart(value, "value", rawLocator);

            logger.debug(
                    "[LOCATOR-PARSER] Locator parsed as type/value | type: {} | value: {}",
                    type,
                    value
            );

            return new LocatorDefinition(type, value, null);
        }

        if (parts.length == 3) {
            String type = parts[0].trim();
            String value = parts[1].trim();
            String option = parts[2].trim();

            validatePart(type, "type", rawLocator);
            validatePart(value, "value", rawLocator);
            validatePart(option, "option", rawLocator);

            logger.debug(
                    "[LOCATOR-PARSER] Locator parsed as type/value/option | type: {} | value: {} | option: {}",
                    type,
                    value,
                    option
            );

            return new LocatorDefinition(type, value, option);
        }

        logger.error(
                "[LOCATOR-PARSER] Invalid locator format: {}. Expected format: type|value or type|value|option",
                rawLocator
        );

        throw new RuntimeException("Format locator invalide : " + rawLocator);
    }

    private static void validatePart(String part, String partName, String rawLocator) {
        if (part == null || part.trim().isEmpty()) {
            logger.error(
                    "[LOCATOR-PARSER] Invalid locator part '{}' in locator: {}",
                    partName,
                    rawLocator
            );

            throw new RuntimeException(
                    "Partie '" + partName + "' vide dans le locator : " + rawLocator
            );
        }
    }
}