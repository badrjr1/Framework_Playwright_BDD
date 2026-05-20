package core.actions;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import config.ConfigReader;
import config.LocatorReader;
import core.context.TestContext;
import core.heal.SelfHealingEngine;
import core.heal.dto.ResolvedLocator;
import core.wait.WaitUtils;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElementActions implements IElementActions {

    private static final Logger logger = LoggerFactory.getLogger(ElementActions.class);

    private final Page page;
    private final SelfHealingEngine selfHealingEngine;
    private Frame currentFrame;

    public ElementActions(Page page) {
        this.page = page;
        this.selfHealingEngine = new SelfHealingEngine(page);

        logger.info("[ELEMENT-ACTIONS] ElementActions initialized");
    }

    @Override
    public void login() {
        Allure.step("Log in to the application", () -> {
            try {
                String baseUrl = ConfigReader.get("base.url");

                logger.info("[UI] Navigating to application: {}", baseUrl);

                page.navigate(baseUrl);

                logger.info("[UI] Application opened successfully: {}", baseUrl);

                Allure.addAttachment(
                        "Login navigation",
                        "text/plain",
                        """
                        Action: login
                        Base URL: %s
                        Current URL: %s
                        """.formatted(baseUrl, page.url())
                );

            } catch (Exception e) {
                logger.error("[UI] Failed to login/navigate to application | reason: {}", e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void navigate(String baseUrl) {
        Allure.step("Navigate to URL: " + baseUrl, () -> {
           try {
                String finalBaseUrl = resolveDynamicValue(baseUrl);

                logger.info("[UI] Navigating to URL: {}", finalBaseUrl);

                page.navigate(finalBaseUrl);

                logger.info("[UI] Navigation completed successfully | URL: {}", finalBaseUrl);

                Allure.addAttachment(
                          "Navigation info",
                          "text/plain",
                          """
                          Action: navigate
                          Input URL: %s
                          Resolved URL: %s
                          Current URL after navigation: %s
                          """.formatted(baseUrl, finalBaseUrl, page.url())
                );
           } catch (Exception e) {
               logger.error("[UI] Navigation failed | URL: {} | reason: {}", baseUrl, e.getMessage(), e);
               throw e;
           }
        });
    }

    @Override
    public void click(String elementName) {
        Allure.step("Click on element: " + elementName, () -> {
            try {
                logger.info("[UI] Click action started on element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("click", elementName, resolvedLocator);

                resolvedLocator.getLocator().click();

                logger.info(
                        "[UI] Click action completed successfully | element: {} | strategy: {} | locator: {}",
                        elementName,
                        resolvedLocator.getStrategyType(),
                        resolvedLocator.getLocatorValue()
                );

            } catch (Exception e) {
                logger.error("[UI] Click action failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void write(String elementName, String value) {
        Allure.step("Write '" + value + "' in field: " + elementName, () -> {
            try {
                String finalValue = resolveDynamicValue(value);

                logger.info("[UI] Write action started | element: {} | value: {}", elementName, finalValue);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("write", elementName, resolvedLocator);

                resolvedLocator.getLocator().fill(finalValue);

                logger.info(
                        "[UI] Write action completed successfully | element: {} | strategy: {} | locator: {}",
                        elementName,
                        resolvedLocator.getStrategyType(),
                        resolvedLocator.getLocatorValue()
                );

            } catch (Exception e) {
                logger.error("[UI] Write action failed | element: {} | value: {} | reason: {}", elementName, value, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public String getText(String elementName) {
        return Allure.step("Get text from element: " + elementName, () -> {
            try {
                logger.info("[UI] Get text action started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("getText", elementName, resolvedLocator);

                String text = resolvedLocator.getLocator().textContent();

                logger.info("[UI] Get text completed | element: {} | value: {}", elementName, text);

                Allure.addAttachment(
                        "Get text result",
                        "text/plain",
                        """
                        Element: %s
                        Text: %s
                        """.formatted(elementName, text)
                );

                return text;

            } catch (Exception e) {
                logger.error("[UI] Get text failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public String getValue(String elementName) {
        return Allure.step("Get value from element: " + elementName, () -> {
            try {
                logger.info("[UI] Get value action started | element: {}", elementName);

                if (elementName.equalsIgnoreCase("lnk_current_url")) {
                    String currentUrl = page.url();

                    logger.info("[UI] Current URL value retrieved: {}", currentUrl);

                    Allure.addAttachment(
                            "System value info",
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

                String value;

                try {
                    value = resolvedLocator.getLocator().inputValue();
                    logger.debug("[UI] Value retrieved using inputValue() | element: {} | value: {}", elementName, value);
                } catch (Exception e) {
                    logger.debug("[UI] inputValue() failed, trying textContent() | element: {}", elementName);
                    value = resolvedLocator.getLocator().textContent();
                }

                logger.info("[UI] Get value completed | element: {} | value: {}", elementName, value);

                return value;

            } catch (Exception e) {
                logger.error("[UI] Get value failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void assertElementContains(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert element " + elementName + " contains '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    try {
                        String finalExpectedValue = resolveDynamicValue(expectedValue);

                        logger.info(
                                "[ASSERT] Element contains started | element: {} | expectedValue: {} | expectedStatus: {}",
                                elementName,
                                finalExpectedValue,
                                expectedStatus
                        );


                        ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                        WaitUtils.waitUntilVisible(resolvedLocator.getLocator());

                        addLocatorInfoToAllure("assertElementContains", elementName, resolvedLocator);

                        String actualText = resolvedLocator.getLocator().textContent();

                        System.out.println("==".repeat(20));
                        System.out.println(actualText);
                        System.out.println("==".repeat(20));



                        boolean actualStatus = actualText != null && actualText.contains(finalExpectedValue);

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
                                        finalExpectedValue,
                                        expectedStatus,
                                        actualText,
                                        actualStatus
                                )
                        );

                        logger.info(
                                "[ASSERT] Element contains result | element: {} | actualStatus: {} | expectedStatus: {}",
                                elementName,
                                actualStatus,
                                expectedStatus
                        );

                        assertEquals(
                                expectedStatus,
                                actualStatus,
                                "Assertion failed. Expected element '"
                                        + elementName
                                        + "' to contain '"
                                        + finalExpectedValue
                                        + "' = "
                                        + expectedStatus
                                        + ", but actual text was: "
                                        + actualText
                        );

                    } catch (Exception e) {
                        logger.error(
                                "[ASSERT] Element contains failed | element: {} | expectedValue: {} | reason: {}",
                                elementName,
                                expectedValue,
                                e.getMessage(),
                                e
                        );
                        throw e;
                    }
                }
        );
    }

    @Override
    public void assertElementContains(String elementName, String expectedValue) {
        assertElementContains(elementName, expectedValue, true);
    }

    @Override
    public void saveElementValue(String elementName, String variableName) {
        Allure.step("Save value of element " + elementName + " in variable " + variableName, () -> {
            try {
                logger.info("[UI] Save element value started | element: {} | variable: {}", elementName, variableName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("saveElementValue", elementName, resolvedLocator);

                String value;

                try {
                    value = resolvedLocator.getLocator().inputValue();
                    logger.debug("[UI] Saved value retrieved using inputValue() | element: {}", elementName);
                } catch (Exception e) {
                    logger.debug("[UI] inputValue() failed, using innerText() | element: {}", elementName);
                    value = resolvedLocator.getLocator().innerText();
                }

                TestContext.put(variableName, value);

                logger.info(
                        "[UI] Value saved successfully | element: {} | variable: {} | value: {}",
                        elementName,
                        variableName,
                        value
                );

                Allure.addAttachment(
                        "Saved variable",
                        "text/plain",
                        """
                        Element: %s
                        Variable name: %s
                        Saved value: %s
                        """.formatted(elementName, variableName, value)
                );

            } catch (Exception e) {
                logger.error(
                        "[UI] Save element value failed | element: {} | variable: {} | reason: {}",
                        elementName,
                        variableName,
                        e.getMessage(),
                        e
                );
                throw e;
            }
        });
    }

    @Override
    public void saveScreenshot(String screenshotName) {
        Allure.step("Save screenshot with name: " + screenshotName, () -> {
            try {
                logger.info("[UI] Taking screenshot | name: {}", screenshotName);

                byte[] screenshot = page.screenshot();

                Allure.addAttachment(
                        screenshotName,
                        "image/png",
                        new ByteArrayInputStream(screenshot),
                        ".png"
                );

                logger.info("[UI] Screenshot saved successfully | name: {}", screenshotName);

            } catch (Exception e) {
                logger.error("[UI] Screenshot failed | name: {} | reason: {}", screenshotName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void searchLocatorsInFile(String fileName) {
        Allure.step("Search locators in file: " + fileName, () -> {
            logger.info("[LOCATOR] Search locators in file requested: {}", fileName);

            /*
             * Si ton LocatorReader charge déjà tous les fichiers .properties
             * depuis src/main/resources/locators, cette méthode peut rester informative.
             *
             * Elle existe seulement pour respecter la syntaxe Gherkin :
             * * search locators in the order_page file
             */

            Allure.addAttachment(
                    "Locator file info",
                    "text/plain",
                    """
                    Requested locator file: %s
                    Status: LocatorReader loads locators globally
                    """.formatted(fileName)
            );
        });
    }

    @Override
    public void assertElementValueEquals(String elementName, String expectedValue) {
        assertElementValueEquals(elementName, expectedValue, true);
    }

    @Override
    public void assertElementValueEquals(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert value of element " + elementName + " equals '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    try {
                        String finalExpectedValue = resolveDynamicValue(expectedValue);

                        logger.info(
                                "[ASSERT] Element value equals started | element: {} | expectedValue: {} | expectedStatus: {}",
                                elementName,
                                finalExpectedValue,
                                expectedStatus
                        );

                        ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                        addLocatorInfoToAllure("assertElementValueEquals", elementName, resolvedLocator);

                        String actualValue = resolvedLocator.getLocator().inputValue();

                        boolean actualStatus = actualValue != null && actualValue.equals(finalExpectedValue);

                        Allure.addAttachment(
                                "Assertion details",
                                "text/plain",
                                """
                                Assertion: element value equals
                                Element: %s
                                Expected value: %s
                                Expected status: %s
                                Actual value: %s
                                Actual status: %s
                                """.formatted(
                                        elementName,
                                        finalExpectedValue,
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
                                        + "' to equal '"
                                        + finalExpectedValue
                                        + "' = "
                                        + expectedStatus
                                        + ", but actual value was: "
                                        + actualValue
                        );

                        logger.info("[ASSERT] Element value equals completed | element: {}", elementName);

                    } catch (Exception e) {
                        logger.error(
                                "[ASSERT] Element value equals failed | element: {} | expectedValue: {} | reason: {}",
                                elementName,
                                expectedValue,
                                e.getMessage(),
                                e
                        );
                        throw e;
                    }
                }
        );
    }

    @Override
    public void assertDropdownSelectedValue(String elementName, String expectedValue) {
        Allure.step(
                "Assert '" + expectedValue + "' is selected in dropdown: " + elementName,
                () -> {
                    try {
                        String finalExpectedValue = resolveDynamicValue(expectedValue);

                        logger.info(
                                "[ASSERT] Dropdown selected value started | element: {} | expectedValue: {}",
                                elementName,
                                finalExpectedValue
                        );

                        ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                        addLocatorInfoToAllure("assertDropdownSelectedValue", elementName, resolvedLocator);

                        String actualValue = resolvedLocator.getLocator().inputValue();

                        boolean actualStatus = actualValue != null && actualValue.equals(finalExpectedValue);

                        Allure.addAttachment(
                                "Dropdown assertion details",
                                "text/plain",
                                """
                                Assertion: dropdown selected value
                                Element: %s
                                Expected selected value: %s
                                Actual selected value: %s
                                Actual status: %s
                                """.formatted(
                                        elementName,
                                        finalExpectedValue,
                                        actualValue,
                                        actualStatus
                                )
                        );

                        assertEquals(
                                true,
                                actualStatus,
                                "Dropdown assertion failed. Expected selected value of '"
                                        + elementName
                                        + "' to be '"
                                        + finalExpectedValue
                                        + "', but actual value was: "
                                        + actualValue
                        );

                        logger.info("[ASSERT] Dropdown selected value completed | element: {}", elementName);

                    } catch (Exception e) {
                        logger.error(
                                "[ASSERT] Dropdown selected value failed | element: {} | expectedValue: {} | reason: {}",
                                elementName,
                                expectedValue,
                                e.getMessage(),
                                e
                        );
                        throw e;
                    }
                }
        );
    }

    @Override
    public void assertElementDisabled(String elementName) {
        Allure.step("Assert element disabled: " + elementName, () -> {
            try {
                logger.info("[ASSERT] Element disabled started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("assertElementDisabled", elementName, resolvedLocator);

                assertThat(resolvedLocator.getLocator()).isDisabled();

                logger.info("[ASSERT] Element is disabled | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[ASSERT] Element disabled failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void assertUrlContains(String expectedUrlPart, boolean expectedStatus) {
        Allure.step(
                "Assert current URL contains: " + expectedUrlPart + " is " + expectedStatus,
                () -> {
                    try {
                        String actualUrl = page.url();
                        String finalExpectedUrlPart = resolveUrlExpectedValue(expectedUrlPart);

                        logger.info(
                                "[ASSERT] URL contains started | expected: {} | actualUrl: {} | expectedStatus: {}",
                                finalExpectedUrlPart,
                                actualUrl,
                                expectedStatus
                        );

                        boolean actualStatus = actualUrl != null && actualUrl.contains(finalExpectedUrlPart);

                        Allure.addAttachment(
                                "URL assertion details",
                                "text/plain",
                                """
                                Assertion: current URL contains expected value
                                Input value: %s
                                Resolved expected value: %s
                                Expected status: %s
                                Actual URL: %s
                                Actual status: %s
                                """.formatted(
                                        expectedUrlPart,
                                        finalExpectedUrlPart,
                                        expectedStatus,
                                        actualUrl,
                                        actualStatus
                                )
                        );

                        logger.info(
                                "[ASSERT] URL contains result | expected: {} | actualStatus: {} | expectedStatus: {}",
                                finalExpectedUrlPart,
                                actualStatus,
                                expectedStatus
                        );

                        assertEquals(
                                expectedStatus,
                                actualStatus,
                                "URL assertion failed. Expected current URL to contain '"
                                        + finalExpectedUrlPart
                                        + "' = "
                                        + expectedStatus
                                        + ", but actual URL was: "
                                        + actualUrl
                        );

                    } catch (Exception e) {
                        logger.error(
                                "[ASSERT] URL contains failed | expected: {} | reason: {}",
                                expectedUrlPart,
                                e.getMessage(),
                                e
                        );
                        throw e;
                    }
                }
        );
    }

    @Override
    public void assertUrlContains(String expectedUrlPart) {
        assertUrlContains(expectedUrlPart, true);
    }

    @Override
    public void assertElementValueContains(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert value of element " + elementName + " contains '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    try {
                        String finalExpectedValue = resolveDynamicValue(expectedValue);

                        logger.info(
                                "[ASSERT] Element value contains started | element: {} | expectedValue: {} | expectedStatus: {}",
                                elementName,
                                finalExpectedValue,
                                expectedStatus
                        );

                        ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                        addLocatorInfoToAllure(
                                "assertElementValueContains",
                                elementName,
                                resolvedLocator
                        );

                        String actualValue = resolvedLocator.getLocator().inputValue();

                        boolean actualStatus = actualValue != null && actualValue.contains(finalExpectedValue);

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
                                        finalExpectedValue,
                                        expectedStatus,
                                        actualValue,
                                        actualStatus
                                )
                        );

                        logger.info(
                                "[ASSERT] Element value contains result | element: {} | actualStatus: {} | expectedStatus: {}",
                                elementName,
                                actualStatus,
                                expectedStatus
                        );

                        assertEquals(
                                expectedStatus,
                                actualStatus,
                                "Assertion failed. Expected value of '"
                                        + elementName
                                        + "' to contain '"
                                        + finalExpectedValue
                                        + "' = "
                                        + expectedStatus
                                        + ", but actual value was: "
                                        + actualValue
                        );

                    } catch (Exception e) {
                        logger.error(
                                "[ASSERT] Element value contains failed | element: {} | expectedValue: {} | reason: {}",
                                elementName,
                                expectedValue,
                                e.getMessage(),
                                e
                        );
                        throw e;
                    }
                }
        );
    }

    @Override
    public void assertElementValueContains(String elementName, String expectedValue) {
        assertElementValueContains(elementName, expectedValue, true);
    }

    @Override
    public void assertElementEquals(String elementName, String expectedValue, boolean expectedStatus) {
        Allure.step(
                "Assert element " + elementName + " equals '" + expectedValue + "' is " + expectedStatus,
                () -> {
                    try {
                        String finalExpectedValue = resolveDynamicValue(expectedValue);

                        logger.info(
                                "[ASSERT] Element equals started | element: {} | expectedValue: {} | expectedStatus: {}",
                                elementName,
                                finalExpectedValue,
                                expectedStatus
                        );

                        ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                        addLocatorInfoToAllure("assertElementEquals", elementName, resolvedLocator);

                        String actualText = resolvedLocator.getLocator().textContent();
                        boolean actualStatus = actualText != null && actualText.equals(finalExpectedValue);

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
                                        finalExpectedValue,
                                        expectedStatus,
                                        actualText,
                                        actualStatus
                                )
                        );

                        logger.info(
                                "[ASSERT] Element equals result | element: {} | actualStatus: {} | expectedStatus: {}",
                                elementName,
                                actualStatus,
                                expectedStatus
                        );

                        assertEquals(
                                expectedStatus,
                                actualStatus,
                                "Assertion failed. Expected element '"
                                        + elementName
                                        + "' to equal '"
                                        + finalExpectedValue
                                        + "' = "
                                        + expectedStatus
                                        + ", but actual text was: "
                                        + actualText
                        );

                    } catch (Exception e) {
                        logger.error(
                                "[ASSERT] Element equals failed | element: {} | expectedValue: {} | reason: {}",
                                elementName,
                                expectedValue,
                                e.getMessage(),
                                e
                        );
                        throw e;
                    }
                }
        );
    }

    @Override
    public void assertElementEquals(String elementName, String expectedValue) {
        assertElementEquals(elementName, expectedValue, true);
    }

    @Override
    public void assertElementVisible(String elementName) {
        Allure.step("Assert element visible: " + elementName, () -> {
            try {
                logger.info("[ASSERT] Element visible started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("assertElementVisible", elementName, resolvedLocator);

                assertThat(resolvedLocator.getLocator()).isVisible();

                logger.info("[ASSERT] Element is visible | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[ASSERT] Element visible failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void assertElementEnabled(String elementName) {
        Allure.step("Assert element enabled: " + elementName, () -> {
            try {
                logger.info("[ASSERT] Element enabled started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("assertElementEnabled", elementName, resolvedLocator);

                assertThat(resolvedLocator.getLocator()).isEnabled();

                logger.info("[ASSERT] Element is enabled | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[ASSERT] Element enabled failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void assertElementChecked(String elementName) {
        Allure.step("Assert element checked: " + elementName, () -> {
            try {
                logger.info("[ASSERT] Element checked started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("assertElementChecked", elementName, resolvedLocator);

                assertThat(resolvedLocator.getLocator()).isChecked();

                logger.info("[ASSERT] Element is checked | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[ASSERT] Element checked failed | element: {} | reason: {}", elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    private void addLocatorInfoToAllure(
            String action,
            String elementName,
            ResolvedLocator resolvedLocator
    ) {
        if (resolvedLocator == null) {
            logger.warn("[ALLURE] ResolvedLocator is null | action: {} | element: {}", action, elementName);

            Allure.addAttachment(
                    "Self-healing locator info",
                    "text/plain",
                    """
                    Action: %s
                    Element: %s
                    Strategy used: NULL
                    Locator used: NULL
                    """.formatted(action, elementName)
            );

            return;
        }

        logger.debug(
                "[ALLURE] Adding locator info | action: {} | element: {} | strategy: {} | locator: {}",
                action,
                elementName,
                resolvedLocator.getStrategyType(),
                resolvedLocator.getLocatorValue()
        );

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

    private String resolveUrlExpectedValue(String expectedUrlPart) {
        String value = resolveDynamicValue(expectedUrlPart);

        try {
            if (LocatorReader.containsKey(value)) {
                String locatorValue = LocatorReader.get(value);

                logger.debug(
                        "[URL] Expected URL part resolved from LocatorReader | key: {} | value: {}",
                        value,
                        locatorValue
                );

                return locatorValue;
            }
        } catch (Exception e) {
            logger.debug("[URL] Value is not a locator key, using it directly: {}", value);
        }

        return value;
    }

    private String resolveDynamicValue(String value) {
        if (value == null) {
            return null;
        }

        String result = value;

        while (result.contains("${")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);

            if (end == -1) {
                logger.warn("[CONTEXT] Invalid dynamic variable syntax in value: {}", value);
                break;
            }

            String variableName = result.substring(start + 2, end);
            String variableValue = TestContext.getString(variableName);

            if (variableValue == null) {
                logger.error("[CONTEXT] Variable not found in TestContext: {}", variableName);
                throw new RuntimeException("Variable not found in TestContext: " + variableName);
            }

            logger.debug(
                    "[CONTEXT] Dynamic variable resolved | variable: {} | value: {}",
                    variableName,
                    variableValue
            );

            result = result.substring(0, start)
                    + variableValue
                    + result.substring(end + 1);
        }

        return result;
    }

    @Override
    public void waitForElementVisible(String elementName) {
        Allure.step("Wait for element " + elementName + " to become visible", () -> {
            try {
                logger.info("[UI] Waiting for element to become visible | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("waitForElementVisible", elementName, resolvedLocator);

                assertThat(resolvedLocator.getLocator()).isVisible();

                logger.info("[UI] Element is visible | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[UI] Wait for element visible failed | element: {} | reason: {}",
                        elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void switchToFrameByIndex(int index) {
        Allure.step("Switch to frame index: " + index, () -> {
            try {
                logger.info("[FRAME] Switching to frame by index: {}", index);

                if (index < 0 || index >= page.frames().size()) {
                    throw new RuntimeException(
                            "Frame index not found: " + index
                                    + " | Available frames: " + page.frames().size()
                    );
                }

                currentFrame = page.frames().get(index);

                logger.info("[FRAME] Switched to frame index: {} | url: {}", index, currentFrame.url());

                Allure.addAttachment(
                        "Frame switch details",
                        "text/plain",
                        """
                        Switch type: index
                        Frame index: %s
                        Frame URL: %s
                        """.formatted(index, currentFrame.url())
                );

            } catch (Exception e) {
                logger.error("[FRAME] Switch to frame by index failed | index: {} | reason: {}",
                        index, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void switchToParentFrame() {
        Allure.step("Switch to parent frame", () -> {
            try {
                logger.info("[FRAME] Switching to parent frame / main page");

                currentFrame = null;

                logger.info("[FRAME] Switched to main page context");

                Allure.addAttachment(
                        "Frame switch details",
                        "text/plain",
                        "Switched to main page context"
                );

            } catch (Exception e) {
                logger.error("[FRAME] Switch to parent frame failed | reason: {}", e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void switchToFrameByName(String frameName) {
        Allure.step("Switch to frame name: " + frameName, () -> {
            try {
                logger.info("[FRAME] Switching to frame by name: {}", frameName);

                currentFrame = page.frame(frameName);

                if (currentFrame == null) {
                    throw new RuntimeException("Frame not found by name: " + frameName);
                }

                logger.info("[FRAME] Switched to frame name: {} | url: {}", frameName, currentFrame.url());

                Allure.addAttachment(
                        "Frame switch details",
                        "text/plain",
                        """
                        Switch type: name
                        Frame name: %s
                        Frame URL: %s
                        """.formatted(frameName, currentFrame.url())
                );

            } catch (Exception e) {
                logger.error("[FRAME] Switch to frame by name failed | name: {} | reason: {}",
                        frameName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void switchToFrameById(String frameId) {
        Allure.step("Switch to frame id: " + frameId, () -> {
            try {
                logger.info("[FRAME] Switching to frame by id: {}", frameId);

                Locator iframe = page.locator("iframe#" + frameId);

                if (iframe.count() == 0) {
                    iframe = page.locator("iframe[id='" + frameId + "']");
                }

                if (iframe.count() == 0) {
                    throw new RuntimeException("Iframe not found by id: " + frameId);
                }

                String frameName = iframe.first().getAttribute("name");

                if (frameName == null || frameName.isBlank()) {
                    throw new RuntimeException(
                            "Iframe found by id '" + frameId + "' but it has no name attribute"
                    );
                }

                currentFrame = page.frame(frameName);

                if (currentFrame == null) {
                    throw new RuntimeException("Playwright frame not resolved for id: " + frameId);
                }

                logger.info("[FRAME] Switched to frame id: {} | name: {} | url: {}",
                        frameId, frameName, currentFrame.url());

                Allure.addAttachment(
                        "Frame switch details",
                        "text/plain",
                        """
                        Switch type: id
                        Frame id: %s
                        Frame name: %s
                        Frame URL: %s
                        """.formatted(frameId, frameName, currentFrame.url())
                );

            } catch (Exception e) {
                logger.error("[FRAME] Switch to frame by id failed | id: {} | reason: {}",
                        frameId, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void clickSvgElement(String elementName) {
        Allure.step("Click SVG element: " + elementName, () -> {
            try {
                logger.info("[UI] Click SVG element started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("clickSvgElement", elementName, resolvedLocator);

                resolvedLocator.getLocator().click();

                logger.info("[UI] SVG element clicked successfully | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[UI] Click SVG failed | element: {} | reason: {}",
                        elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void selectIndexFromDropdown(String elementName, int index) {
        Allure.step("Select index " + index + " from dropdown: " + elementName, () -> {
            try {
                logger.info("[UI] Select dropdown by index started | element: {} | index: {}",
                        elementName, index);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("selectIndexFromDropdown", elementName, resolvedLocator);

                resolvedLocator.getLocator().selectOption(new SelectOption().setIndex(index));

                logger.info("[UI] Dropdown index selected successfully | element: {} | index: {}",
                        elementName, index);

            } catch (Exception e) {
                logger.error("[UI] Select dropdown by index failed | element: {} | index: {} | reason: {}",
                        elementName, index, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void selectTextFromDropdown(String elementName, String text) {
        Allure.step("Select text '" + text + "' from dropdown: " + elementName, () -> {
            try {
                String finalText = resolveDynamicValue(text);

                logger.info("[UI] Select dropdown by text started | element: {} | text: {}",
                        elementName, finalText);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("selectTextFromDropdown", elementName, resolvedLocator);

                resolvedLocator.getLocator().selectOption(new SelectOption().setLabel(finalText));

                logger.info("[UI] Dropdown text selected successfully | element: {} | text: {}",
                        elementName, finalText);

            } catch (Exception e) {
                logger.error("[UI] Select dropdown by text failed | element: {} | text: {} | reason: {}",
                        elementName, text, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void writeValueOfKey(String key, String elementName) {
        Allure.step("Write value of key " + key + " in element: " + elementName, () -> {
            try {
                String cleanKey = key
                        .replace("<", "")
                        .replace(">", "")
                        .trim();

                logger.info("[UI] Write value of key started | key: {} | element: {}",
                        cleanKey, elementName);

                String value = ConfigReader.get(cleanKey);

                if (value == null || value.isBlank()) {
                    throw new RuntimeException("No value found in config for key: " + cleanKey);
                }

                logger.info("[UI] Key resolved successfully | key: {} | value: {}",
                        cleanKey, value);

                write(elementName, value);

            } catch (Exception e) {
                logger.error("[UI] Write value of key failed | key: {} | element: {} | reason: {}",
                        key, elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void pressKeyOnElement(String key, String elementName) {
        Allure.step("Press " + key + " on element: " + elementName, () -> {
            try {
                logger.info("[UI] Press key started | key: {} | element: {}", key, elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("pressKeyOnElement", elementName, resolvedLocator);

                resolvedLocator.getLocator().press(key.toUpperCase());

                logger.info("[UI] Key pressed successfully | key: {} | element: {}", key, elementName);

            } catch (Exception e) {
                logger.error("[UI] Press key failed | key: {} | element: {} | reason: {}",
                        key, elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void scrollToTop() {
        Allure.step("Scroll to top", () -> {
            try {
                logger.info("[UI] Scrolling to top");

                page.evaluate("window.scrollTo(0, 0)");

                logger.info("[UI] Scrolled to top successfully");

            } catch (Exception e) {
                logger.error("[UI] Scroll to top failed | reason: {}", e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void scrollToBottom() {
        Allure.step("Scroll to bottom", () -> {
            try {
                logger.info("[UI] Scrolling to bottom");

                page.evaluate("window.scrollTo(0, document.body.scrollHeight)");

                logger.info("[UI] Scrolled to bottom successfully");

            } catch (Exception e) {
                logger.error("[UI] Scroll to bottom failed | reason: {}", e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void scrollToElement(String elementName) {
        Allure.step("Scroll to element: " + elementName, () -> {
            try {
                logger.info("[UI] Scroll to element started | element: {}", elementName);

                ResolvedLocator resolvedLocator = selfHealingEngine.resolveLocator(elementName);

                addLocatorInfoToAllure("scrollToElement", elementName, resolvedLocator);

                resolvedLocator.getLocator().scrollIntoViewIfNeeded();

                logger.info("[UI] Scrolled to element successfully | element: {}", elementName);

            } catch (Exception e) {
                logger.error("[UI] Scroll to element failed | element: {} | reason: {}",
                        elementName, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void waitSeconds(int seconds) {
        Allure.step("Wait " + seconds + " seconds", () -> {
            try {
                logger.info("[WAIT] Waiting {} seconds", seconds);

                page.waitForTimeout(seconds * 1000L);

                logger.info("[WAIT] Wait completed | seconds: {}", seconds);

            } catch (Exception e) {
                logger.error("[WAIT] Wait failed | seconds: {} | reason: {}",
                        seconds, e.getMessage(), e);
                throw e;
            }
        });
    }

    @Override
    public void debug() {
        Allure.step("Debug execution", () -> {
            try {
                String currentFrameInfo = currentFrame == null ? "MAIN_PAGE" : currentFrame.url();

                logger.info("[DEBUG] Debug step reached");
                logger.info("[DEBUG] Current URL: {}", page.url());
                logger.info("[DEBUG] Current title: {}", page.title());
                logger.info("[DEBUG] Current frame: {}", currentFrameInfo);

                Allure.addAttachment(
                        "Debug information",
                        "text/plain",
                        """
                        Current URL: %s
                        Current title: %s
                        Current frame: %s
                        """.formatted(
                                page.url(),
                                page.title(),
                                currentFrameInfo
                        )
                );

            } catch (Exception e) {
                logger.error("[DEBUG] Debug step failed | reason: {}", e.getMessage(), e);
                throw e;
            }
        });
    }
}