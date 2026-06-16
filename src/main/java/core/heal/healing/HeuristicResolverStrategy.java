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

public class HeuristicResolverStrategy {

    private static final Logger logger =
            LoggerFactory.getLogger(HeuristicResolverStrategy.class);

    private final HeuristicStrategy heuristicStrategy;
    private final LocatorValidator locatorValidator;

    public HeuristicResolverStrategy(
            HeuristicStrategy heuristicStrategy,
            LocatorValidator locatorValidator
    ) {
        this.heuristicStrategy = heuristicStrategy;
        this.locatorValidator = locatorValidator;

        logger.info("[HEURISTIC-RESOLVER] HeuristicResolverStrategy initialized");
    }

    public ResolvedLocator heal(String elementName, LocatorDefinition primaryLocator) {

        logger.info("[HEURISTIC-RESOLVER] Starting heuristic resolver for element: {}", elementName);

        HealingResult heuristicResult = heuristicStrategy.heal(elementName, primaryLocator);

        if (heuristicResult == null || !heuristicResult.isSuccess()) {
            logger.warn("[HEURISTIC-RESOLVER] Heuristic returned no valid result for: {}", elementName);
            return null;
        }

        String heuristicLocator = heuristicResult.getLocator();

        if (heuristicLocator == null || heuristicLocator.trim().isEmpty()) {
            logger.warn("[HEURISTIC-RESOLVER] Heuristic locator is empty for: {}", elementName);
            return null;
        }

        logger.info(
                "[HEURISTIC-RESOLVER] Heuristic proposed locator for {}: {}",
                elementName,
                heuristicLocator
        );

        try {
            LocatorDefinition heuristicDefinition =
                    LocatorParser.parseOne(heuristicLocator);

            Locator validHeuristicLocator =
                    locatorValidator.tryLocator(heuristicDefinition, HealingStrategyType.HEURISTIC.toString());

            if (validHeuristicLocator != null) {
                logger.info(
                        "[HEURISTIC-RESOLVER] Heuristic locator OK for element: {} | locator: {}",
                        elementName,
                        heuristicLocator
                );

                LocatorReader.updatePrimaryLocator(elementName, heuristicLocator);

                logger.info(
                        "[HEURISTIC-RESOLVER] Heuristic locator saved as new primary for: {}",
                        elementName
                );

                return new ResolvedLocator(
                        validHeuristicLocator,
                        heuristicLocator,
                        HealingStrategyType.HEURISTIC
                );
            }

            logger.warn(
                    "[HEURISTIC-RESOLVER] Heuristic locator proposed but invalid for {}: {}",
                    elementName,
                    heuristicLocator
            );

            return null;

        } catch (Exception e) {
            logger.warn(
                    "[HEURISTIC-RESOLVER] Failed to parse/test heuristic locator for {}: {} | reason: {}",
                    elementName,
                    heuristicLocator,
                    e.getMessage()
            );

            return null;
        }
    }
}