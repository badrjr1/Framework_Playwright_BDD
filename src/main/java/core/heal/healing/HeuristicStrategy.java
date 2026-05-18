package core.heal.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorFactory;
import core.heal.locator.LocatorParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeuristicStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HeuristicStrategy.class);

    private final Page page;
    private final LocatorFactory locatorFactory;

    public HeuristicStrategy(Page page) {
        this.page = page;
        this.locatorFactory = new LocatorFactory(page);

        logger.info("[HEURISTIC] HeuristicStrategy initialized");
    }

    public HealingResult heal(String elementName, LocatorDefinition failedLocator) {

        logger.info("[HEURISTIC] Start heuristic healing for element: {}", elementName);

        if (failedLocator != null) {
            logger.warn(
                    "[HEURISTIC] Failed locator received for {}: {}",
                    elementName,
                    failedLocator.toStorageFormat()
            );
        }

        if (elementName == null || elementName.trim().isEmpty()) {
            logger.error("[HEURISTIC] Element name is null or empty");
            return new HealingResult(false, elementName, (String) null, "HEURISTIC");
        }

        if (elementName.startsWith("txt_")) {
            return healTextField(elementName);
        }

        if (elementName.startsWith("btn_")) {
            return healButton(elementName);
        }

        logger.warn(
                "[HEURISTIC] No heuristic rule found for element: {}. Supported prefixes: txt_, btn_",
                elementName
        );

        return new HealingResult(false, elementName, (String) null, "HEURISTIC");
    }

    private HealingResult healTextField(String elementName) {

        String fieldName = elementName.replace("txt_", "");

        logger.info(
                "[HEURISTIC] Element {} detected as text field. Extracted field name: {}",
                elementName,
                fieldName
        );

        String candidate1 = "css|input[name='" + fieldName + "']";
        if (isValid(candidate1)) {
            logger.info("[HEURISTIC] Valid locator found for {}: {}", elementName, candidate1);
            return new HealingResult(true, elementName, candidate1, "HEURISTIC");
        }

        String candidate2 = "css|input[id*='" + fieldName + "']";
        if (isValid(candidate2)) {
            logger.info("[HEURISTIC] Valid locator found for {}: {}", elementName, candidate2);
            return new HealingResult(true, elementName, candidate2, "HEURISTIC");
        }

        String candidate3 = "placeholder|" + normalizeName(fieldName);
        if (isValid(candidate3)) {
            logger.info("[HEURISTIC] Valid locator found for {}: {}", elementName, candidate3);
            return new HealingResult(true, elementName, candidate3, "HEURISTIC");
        }

        logger.warn("[HEURISTIC] No valid heuristic locator found for text field: {}", elementName);

        return new HealingResult(false, elementName, (String) null, "HEURISTIC");
    }

    private HealingResult healButton(String elementName) {

        String buttonName = elementName
                .replace("btn_", "")
                .replace("_", " ");

        String normalizedButtonName = normalizeName(buttonName);

        logger.info(
                "[HEURISTIC] Element {} detected as button. Extracted button name: {}",
                elementName,
                normalizedButtonName
        );

        String candidate1 = "role|button|" + normalizedButtonName;
        if (isValid(candidate1)) {
            logger.info("[HEURISTIC] Valid locator found for {}: {}", elementName, candidate1);
            return new HealingResult(true, elementName, candidate1, "HEURISTIC");
        }

        String candidate2 = "text|" + normalizedButtonName;
        if (isValid(candidate2)) {
            logger.info("[HEURISTIC] Valid locator found for {}: {}", elementName, candidate2);
            return new HealingResult(true, elementName, candidate2, "HEURISTIC");
        }

        logger.warn("[HEURISTIC] No valid heuristic locator found for button: {}", elementName);

        return new HealingResult(false, elementName, (String) null, "HEURISTIC");
    }

    private boolean isValid(String rawLocator) {
        try {
            logger.debug("[HEURISTIC] Testing candidate locator: {}", rawLocator);

            LocatorDefinition definition = LocatorParser.parseOne(rawLocator);
            Locator locator = locatorFactory.create(definition);

            int count = locator.count();

            logger.debug(
                    "[HEURISTIC] Candidate locator count: {} -> {}",
                    rawLocator,
                    count
            );

            if (count > 0) {
                logger.info("[HEURISTIC] Candidate locator is valid: {}", rawLocator);
                return true;
            }

            logger.debug("[HEURISTIC] Candidate locator not found in DOM: {}", rawLocator);
            return false;

        } catch (Exception e) {
            logger.debug(
                    "[HEURISTIC] Candidate locator failed: {} | reason: {}",
                    rawLocator,
                    e.getMessage()
            );
            return false;
        }
    }

    private String normalizeName(String value) {
        if (value == null || value.isEmpty()) {
            logger.debug("[HEURISTIC] normalizeName received null or empty value");
            return value;
        }

        String[] parts = value.split("_| ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            result.append(part.substring(0, 1).toUpperCase())
                    .append(part.substring(1).toLowerCase())
                    .append(" ");
        }

        String normalizedValue = result.toString().trim();

        logger.debug(
                "[HEURISTIC] Normalized value: '{}' -> '{}'",
                value,
                normalizedValue
        );

        return normalizedValue;
    }
}