package pages.actions;

import com.microsoft.playwright.Page;
import config.LocatorReader;
import core.heal.dto.ResolvedLocator;
import core.heal.SelfHealingEngine;
import io.qameta.allure.Allure;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElementActions implements IElementActions{

    private final Page page;
    private final SelfHealingEngine selfHealingEngine;

    public ElementActions(Page page) {
        this.page = page;
        this.selfHealingEngine = new SelfHealingEngine(page);
    }

    @Override
    public void click(String elementName) {
        Allure.step("Click on element: " + elementName, () -> {
            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("click", elementName, resolvedLocator);

            resolvedLocator.getLocator().click();
        });
    }

    @Override
    public void write(String elementName, String value) {
        Allure.step("Write '" + value + "' in field: " + elementName, () -> {
            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("write", elementName, resolvedLocator);

            resolvedLocator.getLocator().fill(value);
        });
    }

    @Override
    public String getText(String elementName) {
        return Allure.step("Get text from element: " + elementName, () -> {
            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("getText", elementName, resolvedLocator);

            return resolvedLocator.getLocator().textContent();
        });
    }

    @Override
    public String getValue(String elementName) {
        return Allure.step("Get value from element: " + elementName, () -> {

            if (elementName.equalsIgnoreCase("lnk_current_url")) {
                String currentUrl = page.url();

                Allure.addAttachment(
                        "Self-healing locator info",
                        "text/plain",
                        """
                        Action: getValue
                        Element: %s
                        Strategy used: SYSTEM_VALUE
                        Locator used: page.url()
                        Actual value: %s
                        """.formatted(elementName, currentUrl)
                );

                return currentUrl;
            }

            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("getValue", elementName, resolvedLocator);

            return resolvedLocator.getLocator().inputValue();
        });
    }

    @Override
    public void assertElementContains(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert element " + elementName + " contains '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                    addLocatorInfoToAllure("assertElementContains", elementName, resolvedLocator);

                    String actualText = resolvedLocator.getLocator().textContent();
                    boolean actualStatus = actualText != null && actualText.contains(expectedValue);

                    Allure.addAttachment(
                            "Assertion details",
                            "text/plain",
                            """
                            Assertion: element contains
                            Element: %s
                            Expected value: %s
                            Expected status: %s
                            Actual text: %s
                            Actual status: %s
                            """.formatted(
                                    elementName,
                                    expectedValue,
                                    expectedStatus,
                                    actualText,
                                    actualStatus
                            )
                    );

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
        );
    }

    @Override
    public void assertElementContains(String elementName, String expectedValue) {

    }

    @Override
    public void assertUrlContains(String elementName, boolean expectedStatus) {
        Allure.step(
                "Assert current URL contains value from: " + elementName + " is " + expectedStatus,
                () -> {
                    String actualUrl = page.url();

                    String strategyUsed = "SYSTEM_VALUE";
                    String locatorUsed = "page.url()";

                    Allure.addAttachment(
                            "URL assertion locator info",
                            "text/plain",
                            """
                            Action: assertUrlContains
                            Element: lnk_current_url
                            Strategy used: %s
                            Locator used: %s
                            Expected URL part: %s
                            Actual URL: %s
                            """.formatted(
                                    strategyUsed,
                                    locatorUsed,
                                    elementName,
                                    actualUrl
                            )
                    );

                    boolean actualStatus = actualUrl != null && actualUrl.contains(elementName);

                    Allure.addAttachment(
                            "URL assertion details",
                            "text/plain",
                            """
                            Assertion: current URL contains expected value
                            Element: lnk_current_url
                            Expected value: %s
                            Expected status: %s
                            Actual URL: %s
                            Actual status: %s
                            """.formatted(
                                    elementName,
                                    expectedStatus,
                                    actualUrl,
                                    actualStatus
                            )
                    );

                    assertEquals(
                            expectedStatus,
                            actualStatus,
                            "URL assertion failed. Expected current URL to contain '"
                                    + elementName
                                    + "' = "
                                    + expectedStatus
                                    + ", but actual URL was: "
                                    + actualUrl
                    );
                }
        );
    }

    @Override
    public void assertElementValueContains(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert value of element " + elementName + " contains '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                    addLocatorInfoToAllure(
                            "assertElementValueContains",
                            elementName,
                            resolvedLocator
                    );

                    String actualValue = resolvedLocator.getLocator().inputValue();

                    boolean actualStatus = actualValue != null && actualValue.contains(expectedValue);

                    Allure.addAttachment(
                            "Assertion details",
                            "text/plain",
                            """
                            Assertion: element value contains
                            Element: %s
                            Expected value: %s
                            Expected status: %s
                            Actual value: %s
                            Actual status: %s
                            """.formatted(
                                    elementName,
                                    expectedValue,
                                    expectedStatus,
                                    actualValue,
                                    actualStatus
                            )
                    );

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
        );
    }

    @Override
    public void assertElementValueContains(String elementName, String expectedValue) {

    }

    @Override
    public void assertElementEquals(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert element " + elementName + " equals '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                    addLocatorInfoToAllure("assertElementEquals", elementName, resolvedLocator);

                    String actualText = resolvedLocator.getLocator().textContent();
                    boolean actualStatus = actualText != null && actualText.equals(expectedValue);

                    Allure.addAttachment(
                            "Assertion details",
                            "text/plain",
                            """
                            Assertion: element equals
                            Element: %s
                            Expected value: %s
                            Expected status: %s
                            Actual text: %s
                            Actual status: %s
                            """.formatted(
                                    elementName,
                                    expectedValue,
                                    expectedStatus,
                                    actualText,
                                    actualStatus
                            )
                    );

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
        );
    }

    @Override
    public void assertElementEquals(String elementName, String expectedValue) {

    }

    @Override
    public void assertElementVisible(String elementName) {
        Allure.step("Assert element visible: " + elementName, () -> {
            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("assertElementVisible", elementName, resolvedLocator);

            assertThat(resolvedLocator.getLocator()).isVisible();
        });
    }

    @Override
    public void assertUrlContains(String elementName) {

    }

    @Override
    public void assertElementEnabled(String elementName) {
        Allure.step("Assert element enabled: " + elementName, () -> {
            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("assertElementEnabled", elementName, resolvedLocator);

            assertThat(resolvedLocator.getLocator()).isEnabled();
        });
    }

    @Override
    public void assertElementChecked(String elementName) {
        Allure.step("Assert element checked: " + elementName, () -> {
            ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

            addLocatorInfoToAllure("assertElementChecked", elementName, resolvedLocator);

            assertThat(resolvedLocator.getLocator()).isChecked();
        });
    }




    private void addLocatorInfoToAllure(
            String action,
            String elementName,
            ResolvedLocator resolvedLocator
    ) {
        Allure.addAttachment(
                "Self-healing locator info",
                "text/plain",
                """
                Action: %s
                Element: %s
                Strategy used: %s
                Locator used: %s
                """.formatted(
                        action,
                        elementName,
                        resolvedLocator.getStrategyType(),
                        resolvedLocator.getLocatorValue()
                )
        );
    }
}
