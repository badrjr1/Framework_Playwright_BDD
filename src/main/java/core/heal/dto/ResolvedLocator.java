package core.heal.dto;

import com.microsoft.playwright.Locator;

public class ResolvedLocator {

    private final Locator locator;
    private final String locatorValue;
    private final HealingStrategyType strategyType;

    public ResolvedLocator(
            Locator locator,
            String locatorValue,
            HealingStrategyType strategyType
    ) {
        this.locator = locator;
        this.locatorValue = locatorValue;
        this.strategyType = strategyType;
    }

    public Locator getLocator() {
        return locator;
    }

    public String getLocatorValue() {
        return locatorValue;
    }

    public HealingStrategyType getStrategyType() {
        return strategyType;
    }
}
