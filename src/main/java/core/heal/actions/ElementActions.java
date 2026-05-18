package core.heal.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import core.heal.healing.SelfHealingEngine;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElementActions {

    private final Page page;
    private final SelfHealingEngine selfHealingEngine;

    public ElementActions(Page page) {
        this.page = page;
        this.selfHealingEngine = new SelfHealingEngine(page);
    }

    public void click(String elementName) {
        Locator locator = selfHealingEngine.findLocator(elementName);
        locator.click();
    }

    public void write(String elementName, String value) {
        Locator locator = selfHealingEngine.findLocator(elementName);
        locator.fill(value);
    }

    public String getText(String elementName) {
        Locator locator = selfHealingEngine.findLocator(elementName);
        return locator.textContent();
    }

    public String getValue(String elementName) {
        if (elementName.equalsIgnoreCase("lnk_current_url")) {
            return page.url();
        }

        Locator locator = selfHealingEngine.findLocator(elementName);
        return locator.inputValue();
    }

    public void assertElementContains(String elementName, String expectedValue, boolean expectedStatus) {
        Locator locator = selfHealingEngine.findLocator(elementName);

        String actualText = locator.textContent();
        boolean actualStatus = actualText != null && actualText.contains(expectedValue);

        assertEquals(
                expectedStatus,
                actualStatus,
                "Assertion failed. Expected element '"
                        + elementName
                        + "' to contain '"
                        + expectedValue
                        + "' = "
                        + expectedStatus
                        + ", but actual text was: "
                        + actualText
        );
    }

    public void assertElementValueContains(String elementName, String expectedValue, boolean expectedStatus) {
        String actualValue = getValue(elementName);
        boolean actualStatus = actualValue != null && actualValue.contains(expectedValue);

        assertEquals(
                expectedStatus,
                actualStatus,
                "Assertion failed. Expected value of '"
                        + elementName
                        + "' to contain '"
                        + expectedValue
                        + "' = "
                        + expectedStatus
                        + ", but actual value was: "
                        + actualValue
        );
    }

    public void assertElementEquals(String elementName, String expectedValue, boolean expectedStatus) {
        Locator locator = selfHealingEngine.findLocator(elementName);

        String actualText = locator.textContent();
        boolean actualStatus = actualText != null && actualText.equals(expectedValue);

        assertEquals(
                expectedStatus,
                actualStatus,
                "Assertion failed. Expected element '"
                        + elementName
                        + "' to equal '"
                        + expectedValue
                        + "' = "
                        + expectedStatus
                        + ", but actual text was: "
                        + actualText
        );
    }

    public void assertElementVisible(String elementName) {
        Locator locator = selfHealingEngine.findLocator(elementName);
        assertThat(locator).isVisible();
    }

    public void assertElementEnabled(String elementName) {
        Locator locator = selfHealingEngine.findLocator(elementName);
        assertThat(locator).isEnabled();
    }

    public void assertElementChecked(String elementName) {
        Locator locator = selfHealingEngine.findLocator(elementName);
        assertThat(locator).isChecked();
    }
}
