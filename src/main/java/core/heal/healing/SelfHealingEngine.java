package core.heal.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import config.LocatorReader;

import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorFactory;
import core.heal.locator.LocatorParser;

import core.wait.WaitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SelfHealingEngine {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingEngine.class);

    private final Page page;
    private final LocatorFactory locatorFactory;
    private final HeuristicStrategy heuristicStrategy;
    private final AiHealingStrategy aiHealingStrategy;

    public SelfHealingEngine(Page page) {
        this.page = page;
        this.locatorFactory = new LocatorFactory(page);
        this.heuristicStrategy = new HeuristicStrategy(page);
        this.aiHealingStrategy = new AiHealingStrategy(page);

        logger.info("[SELF-HEALING] SelfHealingEngine initialized");
    }

    public Locator findLocator(String elementName) {

        logger.info("[SELF-HEALING] Start finding locator for element: {}", elementName);

        String rawLocators = LocatorReader.get(elementName);
        logger.debug("[SELF-HEALING] Raw locators for {}: {}", elementName, rawLocators);

        List<LocatorDefinition> locators = LocatorParser.parse(rawLocators);

        if (locators.isEmpty()) {
            logger.error("[SELF-HEALING] No locator found for element: {}", elementName);
            throw new RuntimeException("Aucun locator trouvé pour l'élément : " + elementName);
        }

        // 1. Tester le primary locator
        LocatorDefinition primaryLocator = locators.get(0);

        logger.info(
                "[SELF-HEALING] Trying primary locator for {}: {}",
                elementName,
                primaryLocator.toStorageFormat()
        );

        Locator validPrimaryLocator = tryLocator(primaryLocator);

        if (validPrimaryLocator != null) {
            logger.info("[SELF-HEALING] Primary locator OK for element: {}", elementName);
            return validPrimaryLocator;
        }

        logger.warn(
                "[SELF-HEALING] Primary locator KO for element: {} | locator: {}",
                elementName,
                primaryLocator.toStorageFormat()
        );

        // 2. Tester les fallback locators
        for (int i = 1; i < locators.size(); i++) {
            LocatorDefinition fallbackLocator = locators.get(i);

            logger.info(
                    "[SELF-HEALING] Trying fallback locator {} for {}: {}",
                    i,
                    elementName,
                    fallbackLocator.toStorageFormat()
            );

            Locator validFallbackLocator = tryLocator(fallbackLocator);

            if (validFallbackLocator != null) {
                logger.info(
                        "[SELF-HEALING] Fallback locator OK for element: {} | locator: {}",
                        elementName,
                        fallbackLocator.toStorageFormat()
                );

                // Optionnel : sauvegarder le fallback valide comme nouveau primary
                LocatorReader.updatePrimaryLocator(elementName, fallbackLocator.toStorageFormat());

                logger.info(
                        "[SELF-HEALING] Fallback locator saved as new primary for {}",
                        elementName
                );

                return validFallbackLocator;
            }

            logger.warn(
                    "[SELF-HEALING] Fallback locator KO for element: {} | locator: {}",
                    elementName,
                    fallbackLocator.toStorageFormat()
            );
        }

        logger.warn("[SELF-HEALING] All fallback locators KO for element: {}", elementName);

        // 3. Heuristic strategy
        logger.info("[SELF-HEALING] Starting heuristic strategy for element: {}", elementName);

        HealingResult heuristicResult = heuristicStrategy.heal(elementName, primaryLocator);

        if (heuristicResult != null && heuristicResult.isSuccess()) {
            logger.info(
                    "[SELF-HEALING] Heuristic proposed locator for {}: {}",
                    elementName,
                    heuristicResult.getLocator()
            );

            LocatorDefinition healedLocatorDefinition =
                    LocatorParser.parseOne(heuristicResult.getLocator());

            Locator validHeuristicLocator = tryLocator(healedLocatorDefinition);

            if (validHeuristicLocator != null) {
                logger.info(
                        "[SELF-HEALING] Heuristic locator OK for element: {} | locator: {}",
                        elementName,
                        heuristicResult.getLocator()
                );

                LocatorReader.updatePrimaryLocator(elementName, heuristicResult.getLocator());

                logger.info(
                        "[SELF-HEALING] Heuristic locator saved as new primary for {}",
                        elementName
                );

                return validHeuristicLocator;
            }

            logger.warn(
                    "[SELF-HEALING] Heuristic locator proposed but invalid for {}: {}",
                    elementName,
                    heuristicResult.getLocator()
            );
        } else {
            logger.warn("[SELF-HEALING] Heuristic strategy failed for element: {}", elementName);
        }

        // 4. AI strategy
        logger.info("[SELF-HEALING] Starting AI strategy for element: {}", elementName);

        HealingResult aiResult = aiHealingStrategy.heal(elementName, primaryLocator);

        if (aiResult != null && aiResult.isSuccess()) {

            for (String aiLocator : aiResult.getLocators()) {
                logger.info(
                        "[SELF-HEALING] Trying AI locator for {}: {}",
                        elementName,
                        aiLocator
                );

                try {
                    LocatorDefinition aiLocatorDefinition = LocatorParser.parseOne(aiLocator);
                    Locator validAiLocator = tryLocator(aiLocatorDefinition);

                    if (validAiLocator != null) {
                        logger.info(
                                "[SELF-HEALING] AI locator OK for element: {} | locator: {}",
                                elementName,
                                aiLocator
                        );

                        LocatorReader.updatePrimaryLocator(elementName, aiLocator);

                        logger.info(
                                "[SELF-HEALING] AI locator saved as new primary for {}",
                                elementName
                        );

                        return validAiLocator;
                    }

                    logger.warn(
                            "[SELF-HEALING] AI locator KO for element: {} | locator: {}",
                            elementName,
                            aiLocator
                    );

                } catch (Exception e) {
                    logger.warn(
                            "[SELF-HEALING] Failed to parse/test AI locator for {}: {} | reason: {}",
                            elementName,
                            aiLocator,
                            e.getMessage()
                    );
                }
            }
        }

        logger.warn("[SELF-HEALING] All AI locators KO for element: {}", elementName);

        logger.error(
                "[SELF-HEALING] Failed to find valid locator for element: {} after Primary, Fallback, Heuristic and AI",
                elementName
        );

        throw new RuntimeException(
                "Impossible de trouver un locator valide pour l'élément : "
                        + elementName
                        + ". Primary, fallback, heuristic et AI ont échoué."
        );
    }

    private Locator tryLocator(LocatorDefinition locatorDefinition) {
        try {
            logger.debug(
                    "[SELF-HEALING] Testing locator: {}",
                    locatorDefinition.toStorageFormat()
            );

            Locator locator = locatorFactory.create(locatorDefinition);

            WaitUtils.waitUntilVisible(locator);

            int count = locator.count();

            logger.debug(
                    "[SELF-HEALING] Locator count for {}: {}",
                    locatorDefinition.toStorageFormat(),
                    count
            );

            if (count > 0) {
                Locator firstLocator = locator.first();

                firstLocator.waitFor(
                        new Locator.WaitForOptions()
                                .setTimeout(3000)
                );

                logger.debug(
                        "[SELF-HEALING] Locator is valid: {}",
                        locatorDefinition.toStorageFormat()
                );

                return firstLocator;
            }

            logger.debug(
                    "[SELF-HEALING] Locator not found in DOM: {}",
                    locatorDefinition.toStorageFormat()
            );

            return null;

        } catch (Exception e) {
            logger.debug(
                    "[SELF-HEALING] Locator failed: {} | reason: {}",
                    locatorDefinition.toStorageFormat(),
                    e.getMessage()
            );

            return null;
        }
    }
}