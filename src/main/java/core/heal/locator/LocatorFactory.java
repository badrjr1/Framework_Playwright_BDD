package core.heal.locator;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocatorFactory {

    private static final Logger logger = LoggerFactory.getLogger(LocatorFactory.class);

    private final Page page;

    public LocatorFactory(Page page) {
        this.page = page;
        logger.debug("[LOCATOR-FACTORY] LocatorFactory initialized");
    }

    public Locator create(LocatorDefinition definition) {
        if (definition == null) {
            logger.error("[LOCATOR-FACTORY] LocatorDefinition is null");
            throw new IllegalArgumentException("LocatorDefinition ne doit pas être null");
        }

        String type = definition.getType();
        String value = definition.getValue();
        String option = definition.getOption();

        if (type == null || type.trim().isEmpty()) {
            logger.error("[LOCATOR-FACTORY] Locator type is null or empty for definition: {}", definition);
            throw new IllegalArgumentException("Le type du locator est obligatoire");
        }

        if (value == null || value.trim().isEmpty()) {
            logger.error("[LOCATOR-FACTORY] Locator value is null or empty for type: {}", type);
            throw new IllegalArgumentException("La valeur du locator est obligatoire pour le type : " + type);
        }

        String normalizedType = type.toLowerCase().trim();

        logger.debug(
                "[LOCATOR-FACTORY] Creating Playwright locator | type: {} | value: {} | option: {}",
                normalizedType,
                value,
                option
        );

        switch (normalizedType) {
            case "css":
                logger.debug("[LOCATOR-FACTORY] Using CSS locator: {}", value);
                return page.locator(value);

            case "xpath":
                logger.debug("[LOCATOR-FACTORY] Using XPath locator: {}", value);
                return page.locator(value);

            case "text":
                logger.debug("[LOCATOR-FACTORY] Using text locator: {}", value);
                return page.getByText(value);

            case "label":
                logger.debug("[LOCATOR-FACTORY] Using label locator: {}", value);
                return page.getByLabel(value);

            case "placeholder":
                logger.debug("[LOCATOR-FACTORY] Using placeholder locator: {}", value);
                return page.getByPlaceholder(value);

            case "alttext":
                logger.debug("[LOCATOR-FACTORY] Using alttext locator: {}", value);
                return page.getByAltText(value);

            case "title":
                logger.debug("[LOCATOR-FACTORY] Using title locator: {}", value);
                return page.getByTitle(value);

            case "testid":
                logger.debug("[LOCATOR-FACTORY] Using testid locator: {}", value);
                return page.getByTestId(value);

            case "role":
                return createRoleLocator(value, option);

            default:
                logger.error("[LOCATOR-FACTORY] Unsupported locator type: {}", normalizedType);
                throw new RuntimeException("Type de locator non supporté : " + normalizedType);
        }
    }

    private Locator createRoleLocator(String roleValue, String option) {
        if (option == null || option.trim().isEmpty()) {
            logger.error(
                    "[LOCATOR-FACTORY] Invalid role locator. Expected format: role|button|Login, but option is empty"
            );
            throw new RuntimeException("Le locator role doit avoir le format role|button|Login");
        }

        try {
            AriaRole role = AriaRole.valueOf(roleValue.toUpperCase().trim());

            logger.debug(
                    "[LOCATOR-FACTORY] Using role locator | role: {} | name: {}",
                    role,
                    option
            );

            return page.getByRole(
                    role,
                    new Page.GetByRoleOptions().setName(option)
            );

        } catch (IllegalArgumentException e) {
            logger.error(
                    "[LOCATOR-FACTORY] Invalid AriaRole value: {}. Example valid values: BUTTON, TEXTBOX, LINK",
                    roleValue,
                    e
            );
            throw new RuntimeException("Role Playwright invalide : " + roleValue, e);
        }
    }
}