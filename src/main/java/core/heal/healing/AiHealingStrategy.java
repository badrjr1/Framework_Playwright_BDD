package core.heal.healing;

import com.microsoft.playwright.Page;
import core.heal.ai.AiClient;
import core.heal.ai.AiClientFactory;
import core.heal.ai.AiPromptBuilder;
import core.heal.dom.DomExtractor;
import core.heal.locator.LocatorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AiHealingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AiHealingStrategy.class);

    private final Page page;
    private final AiClient aiClient;
    private final DomExtractor domExtractor;

    public AiHealingStrategy(Page page) {
        this.page = page;
        this.aiClient = AiClientFactory.create();
        this.domExtractor = new DomExtractor(page);

        logger.info("[AI-HEALING] AiHealingStrategy initialized");
    }

    public HealingResult heal(String elementName, LocatorDefinition failedLocator) {
        logger.info("[AI-HEALING] Start AI healing for element: {}", elementName);

        try {
            if (elementName == null || elementName.trim().isEmpty()) {
                logger.error("[AI-HEALING] Element name is null or empty");

                return new HealingResult(false, elementName, List.of(), "AI");
            }

            if (failedLocator != null) {
                logger.warn(
                        "[AI-HEALING] Failed locator received for {}: {}",
                        elementName,
                        failedLocator.toStorageFormat()
                );
            }

            String keyword = extractKeywordFromElementName(elementName);

            logger.debug(
                    "[AI-HEALING] Keyword extracted from element name {}: {}",
                    elementName,
                    keyword
            );

            String simplifiedDom = domExtractor.extractDomAroundKeyword(keyword);

            if (simplifiedDom == null || simplifiedDom.trim().isEmpty()) {
                logger.warn(
                        "[AI-HEALING] Simplified DOM is empty. AI healing cannot continue for element: {}",
                        elementName
                );

                return new HealingResult(false, elementName, List.of(), "AI");
            }

            String prompt = AiPromptBuilder.buildLocatorPrompt(
                    elementName,
                    failedLocator,
                    simplifiedDom
            );

            logger.info("[AI-HEALING] Sending prompt to AI for element: {}", elementName);

            List<String> aiLocators = aiClient.generateLocator(prompt);

            if (aiLocators == null || aiLocators.isEmpty()) {
                logger.warn("[AI-HEALING] AI returned no locator for element: {}", elementName);
                return new HealingResult(false, elementName, List.of(), "AI");
            }

            List<String> validFormatLocators = aiLocators.stream()
                    .map(String::trim)
                    .filter(this::isLocatorFormatValid)
                    .distinct()
                    .limit(4)
                    .toList();

            logger.info(
                    "[AI-HEALING] AI proposed {} valid-format locator(s) for {}: {}",
                    validFormatLocators.size(),
                    elementName,
                    validFormatLocators
            );

            if (validFormatLocators.isEmpty()) {
                return new HealingResult(false, elementName, List.of(), "AI");
            }

            return new HealingResult(true, elementName, validFormatLocators, "AI");

        } catch (Exception e) {
            logger.error("[AI-HEALING] AI healing failed for element: {}", elementName, e);
            return new HealingResult(false, elementName, List.of(), "AI");
        }
    }

    private String extractKeywordFromElementName(String elementName) {
        String keyword = elementName;

        if (keyword.startsWith("txt_")) {
            keyword = keyword.replace("txt_", "");
        } else if (keyword.startsWith("btn_")) {
            keyword = keyword.replace("btn_", "");
        } else if (keyword.startsWith("lnk_")) {
            keyword = keyword.replace("lnk_", "");
        } else if (keyword.startsWith("lbl_")) {
            keyword = keyword.replace("lbl_", "");
        } else if (keyword.startsWith("msg_")) {
            keyword = keyword.replace("msg_", "");
        }

        return keyword.replace("_", " ").trim();
    }

    private boolean isLocatorFormatValid(String locator) {
        if (locator == null || locator.trim().isEmpty()) {
            return false;
        }

        String[] parts = locator.split("\\|");

        if (parts.length < 2 || parts.length > 3) {
            return false;
        }

        String type = parts[0].trim().toLowerCase();

        return type.equals("css")
                || type.equals("xpath")
                || type.equals("text")
                || type.equals("label")
                || type.equals("placeholder")
                || type.equals("role");
    }
}