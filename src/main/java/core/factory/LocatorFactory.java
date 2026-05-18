package core.factory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import config.LocatorReader;
import core.wait.WaitUtils;

public class LocatorFactory {

    private LocatorFactory() {
    }

    public static Locator getLocator(Page page, String locatorKey) {
        String locatorDefinitions = LocatorReader.get(locatorKey);

        String[] alternatives = locatorDefinitions.split(";");

        RuntimeException lastException = null;

        for (String definition : alternatives) {
            try {

                Locator locator = buildLocator(page, definition.trim());

                WaitUtils.waitUntilVisible(locator);
                if (locator.count() > 0) {
                    return locator;
                }

            } catch (RuntimeException e) {
                lastException = e;
            }
        }

        throw new RuntimeException(
                "Aucun locator valide trouvé pour la clé : " + locatorKey
                        + " avec les valeurs : " + locatorDefinitions,
                lastException
        );
    }

    private static Locator buildLocator(Page page, String locatorDefinition) {
        String[] parts = locatorDefinition.split("\\|");

        if (parts.length < 2) {
            throw new RuntimeException("Format locator invalide : " + locatorDefinition);
        }

        String type = parts[0].trim().toLowerCase();

        return switch (type) {
            case "css" -> page.locator(parts[1].trim());

            case "xpath" -> page.locator(parts[1].trim());

            case "text" -> page.getByText(parts[1].trim());

            case "label" -> page.getByLabel(parts[1].trim());

            case "placeholder" -> page.getByPlaceholder(parts[1].trim());

            case "role" -> {
                if (parts.length < 3) {
                    throw new RuntimeException(
                            "Format role invalide. Format attendu : role|button|Login"
                    );
                }

                AriaRole role = mapRole(parts[1].trim());

                yield page.getByRole(
                        role,
                        new Page.GetByRoleOptions()
                                .setName(parts[2].trim())
                );
            }

            default -> throw new RuntimeException(
                    "Type de locator non supporté : " + type
            );
        };
    }

    private static AriaRole mapRole(String roleName) {
        return switch (roleName.toLowerCase()) {
            case "button" -> AriaRole.BUTTON;
            case "link" -> AriaRole.LINK;
            case "textbox" -> AriaRole.TEXTBOX;
            case "checkbox" -> AriaRole.CHECKBOX;
            case "radio" -> AriaRole.RADIO;
            case "combobox" -> AriaRole.COMBOBOX;
            case "heading" -> AriaRole.HEADING;
            case "tab" -> AriaRole.TAB;
            case "option" -> AriaRole.OPTION;
            default -> throw new RuntimeException("Role non supporté : " + roleName);
        };
    }

    public static boolean isUrlLocator(String locatorKey) {
        String locatorDefinition = LocatorReader.get(locatorKey);
        return locatorDefinition.startsWith("url|");
    }
}