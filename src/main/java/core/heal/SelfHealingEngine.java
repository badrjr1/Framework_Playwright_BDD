package core.heal;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import config.LocatorReader;

import core.heal.dto.HealingStrategyType;
import core.heal.dto.ResolvedLocator;
import core.heal.healing.*;
import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorFactory;
import core.heal.locator.LocatorParser;

import core.heal.validator.LocatorValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SelfHealingEngine {

    private static final Logger logger = LoggerFactory.getLogger(SelfHealingEngine.class);

    private final LocatorValidator locatorValidator;
    private final FallbackStrategy fallbackStrategy;
    private final HeuristicResolverStrategy heuristicResolverStrategy;
    private final AiResolverStrategy aiResolverStrategy;

    public SelfHealingEngine(Page page) {
        LocatorFactory locatorFactory = new LocatorFactory(page);
        this.locatorValidator = new LocatorValidator(locatorFactory);

        HeuristicStrategy heuristicStrategy = new HeuristicStrategy(page);
        AiHealingStrategy aiHealingStrategy = new AiHealingStrategy(page);

        this.fallbackStrategy = new FallbackStrategy(page);
        this.heuristicResolverStrategy = new HeuristicResolverStrategy(
                heuristicStrategy,
                locatorValidator
        );
        this.aiResolverStrategy = new AiResolverStrategy(
                aiHealingStrategy,
                locatorValidator
        );

        logger.info("[SELF-HEALING] SelfHealingEngine initialized");
    }

    public ResolvedLocator resolveLocator(String elementName) {

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

        Locator validPrimaryLocator = locatorValidator.tryLocator(primaryLocator,HealingStrategyType.PRIMARY.toString());

        if (validPrimaryLocator != null) {
            logger.info("[SELF-HEALING] Primary locator OK for element: {}", elementName);
            return new ResolvedLocator(
                    validPrimaryLocator,
                    primaryLocator.toStorageFormat(),
                    HealingStrategyType.PRIMARY
            );
        }

        logger.warn(
                "[SELF-HEALING] Primary locator KO for element: {} | locator: {}",
                elementName,
                primaryLocator.toStorageFormat()
        );

        // 2. Tester les fallback locators
        ResolvedLocator fallbackResolvedLocator =
                fallbackStrategy.heal(locators, elementName);

        if (fallbackResolvedLocator != null) {
            return fallbackResolvedLocator;
        }

        logger.warn("[SELF-HEALING] Fallback strategy failed for element: {}", elementName);

        // 3. Heuristic strategy
        ResolvedLocator heuristicResolvedLocator =
                heuristicResolverStrategy.heal(elementName, primaryLocator);

        if (heuristicResolvedLocator != null) {
            return heuristicResolvedLocator;
        }

        logger.warn("[SELF-HEALING] Heuristic strategy failed for element: {}", elementName);

        // 4. AI strategy
        ResolvedLocator aiResolvedLocator =
                aiResolverStrategy.heal(elementName, primaryLocator);

        if (aiResolvedLocator != null) {
            return aiResolvedLocator;
        }

        logger.warn("[SELF-HEALING] AI strategy failed for element: {}", elementName);



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
}