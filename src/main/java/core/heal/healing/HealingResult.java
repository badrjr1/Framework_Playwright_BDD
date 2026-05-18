package core.heal.healing;

import java.util.List;

public class HealingResult {

    private final boolean success;
    private final String elementName;
    private final String locator;
    private final List<String> locators;
    private final String strategy;

    public HealingResult(boolean success, String elementName, String locator, String strategy) {
        this.success = success;
        this.elementName = elementName;
        this.locator = locator;
        this.locators = locator != null ? List.of(locator) : List.of();
        this.strategy = strategy;
    }

    public HealingResult(boolean success, String elementName, List<String> locators, String strategy) {
        this.success = success;
        this.elementName = elementName;
        this.locators = locators != null ? locators : List.of();
        this.locator = this.locators.isEmpty() ? null : this.locators.get(0);
        this.strategy = strategy;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getElementName() {
        return elementName;
    }

    public String getLocator() {
        return locator;
    }

    public List<String> getLocators() {
        return locators;
    }

    public String getStrategy() {
        return strategy;
    }
}