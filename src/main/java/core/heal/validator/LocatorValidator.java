package core.heal.validator;

import com.microsoft.playwright.Locator;
import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorFactory;
import core.wait.WaitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorValidator {

    private static final Logger logger = LoggerFactory.getLogger(LocatorValidator.class);

    private final LocatorFactory locatorFactory;

    public LocatorValidator(LocatorFactory locatorFactory) {
        this.locatorFactory = locatorFactory;
    }

    public Locator tryLocator(LocatorDefinition locatorDefinition, String strategyName) {
        try {
            if (locatorDefinition == null) {
                logger.warn("[{}] LocatorDefinition is null", strategyName);
                return null;
            }

            String locatorText = locatorDefinition.toStorageFormat();

            logger.debug(
                    "[{}] Testing locator: {}",
                    strategyName,
                    locatorText
            );

            Locator locator = locatorFactory.create(locatorDefinition);

            WaitUtils.waitUntilVisible(locator);

            int count = locator.count();

            logger.debug(
                    "[{}] Locator count for {}: {}",
                    strategyName,
                    locatorText,
                    count
            );

            if (count <= 0) {
                logger.debug(
                        "[{}] Locator not found in DOM: {}",
                        strategyName,
                        locatorText
                );
                return null;
            }

            Locator firstLocator = locator.first();

            WaitUtils.waitUntilVisible(firstLocator);

            logger.debug(
                    "[{}] Locator is valid and visible: {}",
                    strategyName,
                    locatorText
            );

            return firstLocator;

        } catch (Exception e) {
            logger.debug(
                    "[{}] Locator failed: {} | reason: {}",
                    strategyName,
                    locatorDefinition != null ? locatorDefinition.toStorageFormat() : "null",
                    e.getMessage()
            );

            return null;
        }
    }
}