package core.heal.healing;

import com.microsoft.playwright.Locator;
import config.LocatorReader;
import core.heal.dto.HealingResult;
import core.heal.dto.HealingStrategyType;
import core.heal.dto.ResolvedLocator;
import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorParser;
import core.heal.validator.LocatorValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiResolverStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AiResolverStrategy.class);

    private final AiHealingStrategy aiHealingStrategy;
    private final LocatorValidator locatorValidator;

    public AiResolverStrategy(
            AiHealingStrategy aiHealingStrategy,
            LocatorValidator locatorValidator
    ) {
        this.aiHealingStrategy = aiHealingStrategy;
        this.locatorValidator = locatorValidator;

        logger.info("[AI-RESOLVER] AiResolverStrategy initialized");
    }

    public ResolvedLocator heal(String elementName, LocatorDefinition primaryLocator) {

        logger.info("[AI-RESOLVER] Starting AI resolver for element: {}", elementName);

        HealingResult aiResult = aiHealingStrategy.heal(elementName, primaryLocator);

        if (aiResult == null || !aiResult.isSuccess()) {
            logger.warn("[AI-RESOLVER] AI returned no valid result for: {}", elementName);
            return null;
        }

        if (aiResult.getLocators() == null || aiResult.getLocators().isEmpty()) {
            logger.warn("[AI-RESOLVER] AI locator list is empty for: {}", elementName);
            return null;
        }

        for (String aiLocator : aiResult.getLocators()) {

            if (aiLocator == null || aiLocator.trim().isEmpty()) {
                logger.warn("[AI-RESOLVER] Empty AI locator ignored for element: {}", elementName);
                continue;
            }

            logger.info(
                    "[AI-RESOLVER] Trying AI locator for {}: {}",
                    elementName,
                    aiLocator
            );

            try {
                LocatorDefinition aiLocatorDefinition =
                        LocatorParser.parseOne(aiLocator);

                Locator validAiLocator =
                        locatorValidator.tryLocator(aiLocatorDefinition, HealingStrategyType.AI.toString());

                if (validAiLocator != null) {
                    logger.info(
                            "[AI-RESOLVER] AI locator OK for element: {} | locator: {}",
                            elementName,
                            aiLocator
                    );

                    LocatorReader.updatePrimaryLocator(elementName, aiLocator);

                    logger.info(
                            "[AI-RESOLVER] AI locator saved as new primary for: {}",
                            elementName
                    );

                    return new ResolvedLocator(
                            validAiLocator,
                            aiLocator,
                            HealingStrategyType.AI
                    );
                }

                logger.warn(
                        "[AI-RESOLVER] AI locator KO for element: {} | locator: {}",
                        elementName,
                        aiLocator
                );

            } catch (Exception e) {
                logger.warn(
                        "[AI-RESOLVER] Failed to parse/test AI locator for {}: {} | reason: {}",
                        elementName,
                        aiLocator,
                        e.getMessage()
                );
            }
        }

        logger.warn("[AI-RESOLVER] All AI locators KO for element: {}", elementName);

        return null;
    }
}