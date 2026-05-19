package core.heal.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import core.heal.dto.HealingResult;
import core.heal.locator.LocatorDefinition;
import core.heal.locator.LocatorFactory;
import core.heal.locator.LocatorParser;
import core.heal.validator.LocatorValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeuristicStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HeuristicStrategy.class);

    private final LocatorFactory locatorFactory;
    private final LocatorValidator locatorValidator;

    public HeuristicStrategy(Page page) {
        this.locatorFactory = new LocatorFactory(page);
        this.locatorValidator = new LocatorValidator(locatorFactory);

        logger.info("[HEURISTIC] HeuristicStrategy initialized");
    }

    public HealingResult heal(String elementName, LocatorDefinition failedLocator) {

        logger.info("[HEURISTIC] Start heuristic healing for element: {}", elementName);

        if (failedLocator != null) {
            logger.warn(
                    "[HEURISTIC] Failed locator received for {}: {}",
                    elementName,
                    failedLocator.toStorageFormat()
            );
        }

        if (elementName == null || elementName.trim().isEmpty()) {
            logger.error("[HEURISTIC] Element name is null or empty");
            return new HealingResult(false, elementName, (String) null, "HEURISTIC");
        }

        if (elementName.startsWith("txt_")) {
            return healTextField(elementName);
        }

        if (elementName.startsWith("cc_")) {
            return healCheckBox(elementName);
        }

        if (elementName.startsWith("rd_")) {
            return healRadioButton(elementName);
        }

        if (elementName.startsWith("lst_")) {
            return healChoiceList(elementName);
        }

        if (elementName.startsWith("btn_icon_")) {
            return healIconButton(elementName);
        }

        if (elementName.startsWith("btn_")) {
            return healButton(elementName);
        }

        if (elementName.startsWith("mn_")) {
            return healMenu(elementName);
        }

        if (elementName.startsWith("tab_")) {
            return healTab(elementName);
        }

        if (elementName.startsWith("exp_") || elementName.equals("exp")) {
            return healExpand(elementName);
        }

        if (elementName.startsWith("msg_")) {
            return healMessage(elementName);
        }

        if (elementName.startsWith("lbl_")) {
            return healLabel(elementName);
        }

        if (elementName.startsWith("lnk_")) {
            return healLink(elementName);
        }

        logger.warn(
                "[HEURISTIC] No heuristic rule found for element: {}. Supported prefixes: txt_, cc_, rd_, lst_, btn_, btn_icon_, mn_, tab_, exp_, msg_, lbl_, lnk_",
                elementName
        );

        return new HealingResult(false, elementName, (String) null, "HEURISTIC");
    }

    private HealingResult healTextField(String elementName) {
        String fieldName = elementName.replace("txt_", "");
        String normalizedName = normalizeName(fieldName);

        String[] candidates = {
                "css|input[name='" + fieldName + "']",
                "css|input[id*='" + fieldName + "']",
                "css|input[placeholder*='" + normalizedName + "']",
                "placeholder|" + normalizedName,
                "label|" + normalizedName,
                "role|textbox|" + normalizedName
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healCheckBox(String elementName) {
        String name = elementName.replace("cc_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "css|input[type='checkbox'][name*='" + name + "']",
                "css|input[type='checkbox'][id*='" + name + "']",
                "label|" + normalizedName,
                "role|checkbox|" + normalizedName
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healRadioButton(String elementName) {
        String name = elementName.replace("rd_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "css|input[type='radio'][name*='" + name + "']",
                "css|input[type='radio'][id*='" + name + "']",
                "label|" + normalizedName,
                "role|radio|" + normalizedName
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healChoiceList(String elementName) {
        String name = elementName.replace("lst_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "css|select[name*='" + name + "']",
                "css|select[id*='" + name + "']",
                "label|" + normalizedName,
                "role|combobox|" + normalizedName
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healButton(String elementName) {
        String buttonName = elementName
                .replace("btn_", "")
                .replace("_", " ");

        String normalizedButtonName = normalizeName(buttonName);

        String[] candidates = {
                "role|button|" + normalizedButtonName,
                "text|" + normalizedButtonName,
                "css|button:has-text('" + normalizedButtonName + "')",
                "css|input[value='" + normalizedButtonName + "']"
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healIconButton(String elementName) {
        String name = elementName
                .replace("btn_icon_", "")
                .replace("_", " ");

        String normalizedName = normalizeName(name);

        String[] candidates = {
                "css|button[aria-label*='" + normalizedName + "']",
                "css|button[title*='" + normalizedName + "']",
                "css|[aria-label*='" + normalizedName + "']",
                "role|button|" + normalizedName
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healMenu(String elementName) {
        String name = elementName.replace("mn_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "role|menuitem|" + normalizedName,
                "text|" + normalizedName,
                "css|[role='menuitem']:has-text('" + normalizedName + "')",
                "css|a:has-text('" + normalizedName + "')"
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healTab(String elementName) {
        String name = elementName.replace("tab_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "role|tab|" + normalizedName,
                "text|" + normalizedName,
                "css|[role='tab']:has-text('" + normalizedName + "')",
                "css|button:has-text('" + normalizedName + "')"
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healExpand(String elementName) {
        String name = elementName.replace("exp_", "").replace("exp", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "css|[aria-expanded]",
                "css|button[aria-expanded]",
                "css|[aria-label*='" + normalizedName + "']",
                "role|button|" + normalizedName
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healMessage(String elementName) {
        String name = elementName.replace("msg_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "text|" + normalizedName,
                "css|.message",
                "css|.alert",
                "css|.toast",
                "css|[role='alert']"
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healLabel(String elementName) {
        String name = elementName.replace("lbl_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "text|" + normalizedName,
                "css|label:has-text('" + normalizedName + "')",
                "css|span:has-text('" + normalizedName + "')",
                "css|div:has-text('" + normalizedName + "')"
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult healLink(String elementName) {
        String name = elementName.replace("lnk_", "");
        String normalizedName = normalizeName(name);

        String[] candidates = {
                "role|link|" + normalizedName,
                "text|" + normalizedName,
                "css|a:has-text('" + normalizedName + "')",
                "css|a[href*='" + name + "']"
        };

        return testCandidates(elementName, candidates);
    }

    private HealingResult testCandidates(String elementName, String[] candidates) {

        for (String candidate : candidates) {
            logger.info(
                    "[HEURISTIC] Testing candidate for {}: {}",
                    elementName,
                    candidate
            );

            if (isValid(candidate)) {
                logger.info(
                        "[HEURISTIC] Valid heuristic locator found for {}: {}",
                        elementName,
                        candidate
                );

                return new HealingResult(true, elementName, candidate, "HEURISTIC");
            }

            logger.debug(
                    "[HEURISTIC] Candidate KO for {}: {}",
                    elementName,
                    candidate
            );
        }

        logger.warn(
                "[HEURISTIC] No valid heuristic locator found for element: {}",
                elementName
        );

        return new HealingResult(false, elementName, (String) null, "HEURISTIC");
    }


    private boolean isValid(String rawLocator) {
        try {
            logger.debug("[HEURISTIC] Testing candidate locator: {}", rawLocator);

            LocatorDefinition definition = LocatorParser.parseOne(rawLocator);
            Locator locator = locatorFactory.create(definition);

            int count = locator.count();

            logger.debug(
                    "[HEURISTIC] Candidate locator count: {} -> {}",
                    rawLocator,
                    count
            );

            if (count > 0) {
                logger.info("[HEURISTIC] Candidate locator is valid: {}", rawLocator);
                return true;
            }

            logger.debug("[HEURISTIC] Candidate locator not found in DOM: {}", rawLocator);
            return false;

        } catch (Exception e) {
            logger.debug(
                    "[HEURISTIC] Candidate locator failed: {} | reason: {}",
                    rawLocator,
                    e.getMessage()
            );
            return false;
        }
    }

    private String normalizeName(String value) {
        if (value == null || value.isEmpty()) {
            logger.debug("[HEURISTIC] normalizeName received null or empty value");
            return value;
        }

        String[] parts = value.split("_| ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }

            result.append(part.substring(0, 1).toUpperCase())
                    .append(part.substring(1).toLowerCase())
                    .append(" ");
        }

        String normalizedValue = result.toString().trim();

        logger.debug(
                "[HEURISTIC] Normalized value: '{}' -> '{}'",
                value,
                normalizedValue
        );

        return normalizedValue;
    }
}