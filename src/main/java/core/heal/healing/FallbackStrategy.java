package core.heal.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.LocatorReader;
import core.heal.dto.HealingStrategyType;
import core.heal.dto.ResolvedLocator;
import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorFactory;
import core.heal.validator.LocatorValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FallbackStrategy {
    private static final Logger logger = LoggerFactory.getLogger(FallbackStrategy.class);
    private final LocatorValidator locatorValidator;


    public FallbackStrategy(Page page) {
        LocatorFactory locatorFactory = new LocatorFactory(page);
        this.locatorValidator = new LocatorValidator(locatorFactory);

        logger.info("[FALLBACK] FallbackStrategy initialized");
    }
    public ResolvedLocator heal(List<LocatorDefinition> locators,String elementName){

        if (elementName == null || elementName.trim().isEmpty()) {
            logger.error("[FALLBACK] Element name is null or empty");
            return null;
        }

        if (locators == null || locators.size() <= 1) {
            logger.warn(
                    "[FALLBACK] No fallback locators available for element: {}",
                    elementName
            );
            return null;
        }

        logger.info(
                "[FALLBACK] Start fallback healing for element: {} | fallback count: {}",
                elementName,
                locators.size() - 1
        );

        for (int i = 1; i < locators.size(); i++) {
            LocatorDefinition fallbackLocator = locators.get(i);

            logger.info(
                    "[FALLBACK] Trying fallback locator {} for {}: {}",
                    i,
                    elementName,
                    fallbackLocator.toStorageFormat()
            );

            Locator validFallbackLocator = locatorValidator.tryLocator(fallbackLocator,HealingStrategyType.FALLBACK.toString());

            if (validFallbackLocator != null) {
                logger.info(
                        "[FALLBACK] Fallback locator OK for element: {} | locator: {}",
                        elementName,
                        fallbackLocator.toStorageFormat()
                );

                LocatorReader.updatePrimaryLocator(elementName, fallbackLocator.toStorageFormat());

                logger.info(
                        "[FALLBACK] Fallback locator saved as new primary for {}",
                        elementName
                );

                return new ResolvedLocator(
                        validFallbackLocator,
                        fallbackLocator.toStorageFormat(),
                        HealingStrategyType.FALLBACK
                );
            }

            logger.warn(
                    "[FALLBACK] Fallback locator KO for element: {} | locator: {}",
                    elementName,
                    fallbackLocator.toStorageFormat()
            );
        }
        logger.warn("[FALLBACK] All fallback locators KO for element: {}", elementName);
        return null;
    }
//    private Locator tryLocator(LocatorDefinition locatorDefinition) {
//        try {
//            logger.debug(
//                    "[FALLBACK] Testing locator: {}",
//                    locatorDefinition.toStorageFormat()
//            );
//
//            Locator locator = locatorFactory.create(locatorDefinition);
//
//            WaitUtils.waitUntilVisible(locator);
//
//            int count = locator.count();
//
//            logger.debug(
//                    "[FALLBACK] Locator count for {}: {}",
//                    locatorDefinition.toStorageFormat(),
//                    count
//            );
//
//            if (count > 0) {
//                Locator firstLocator = locator.first();
//
//                firstLocator.waitFor(
//                        new Locator.WaitForOptions()
//                                .setTimeout(3000)
//                );
//
//                logger.debug(
//                        "[FALLBACK] Locator is valid: {}",
//                        locatorDefinition.toStorageFormat()
//                );
//
//                return firstLocator;
//            }
//
//            logger.debug(
//                    "[FALLBACK] Locator not found in DOM: {}",
//                    locatorDefinition.toStorageFormat()
//            );
//
//            return null;
//
//        } catch (Exception e) {
//            logger.debug(
//                    "[FALLBACK] Locator failed: {} | reason: {}",
//                    locatorDefinition.toStorageFormat(),
//                    e.getMessage()
//            );
//
//            return null;
//        }
//    }
}
