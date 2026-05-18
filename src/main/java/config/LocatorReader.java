package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LocatorReader extends ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(LocatorReader.class);

    private static final Path LOCATORS_PATH =
            Paths.get("src/main/resources/locators.properties");

    private static final LocatorReader INSTANCE = new LocatorReader();

    private LocatorReader() {
        super(LOCATORS_PATH);
        logger.info("[LOCATOR-READER] locators.properties loaded from: {}", LOCATORS_PATH);
    }

    public static String get(String key) {
        String value = INSTANCE.getProperty(key);

        logger.debug(
                "[LOCATOR-READER] Locator found | key: {} | value: {}",
                key,
                value
        );

        return value;
    }

    public static int getInt(String key) {
        return INSTANCE.getIntProperty(key);
    }

    public static void updatePrimaryLocator(String elementName, String newPrimaryLocator) {
        try {
            List<String> lines = Files.readAllLines(LOCATORS_PATH);
            List<String> updatedLines = new ArrayList<>();

            boolean updated = false;

            for (String line : lines) {
                String trimmedLine = line.trim();

                // Garder les commentaires et lignes vides
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    updatedLines.add(line);
                    continue;
                }

                // Modifier uniquement la ligne de l'élément demandé
                if (trimmedLine.startsWith(elementName + "=")) {
                    String oldValue = trimmedLine.substring((elementName + "=").length());

                    String[] locators = oldValue.split(";");

                    StringBuilder newValue = new StringBuilder();
                    newValue.append(newPrimaryLocator.trim());

                    for (String locator : locators) {
                        String oldLocator = locator.trim();

                        if (!oldLocator.isEmpty()
                                && !oldLocator.equals(newPrimaryLocator.trim())) {
                            newValue.append(";").append(oldLocator);
                        }
                    }

                    updatedLines.add(elementName + "=" + newValue);
                    updated = true;

                    logger.info(
                            "[LOCATOR-READER] Locator line updated | {}={}",
                            elementName,
                            newValue
                    );

                } else {
                    updatedLines.add(line);
                }
            }

            if (!updated) {
                throw new RuntimeException(
                        "Impossible de modifier le locator. Clé introuvable : " + elementName
                );
            }

            Files.write(LOCATORS_PATH, updatedLines);

            // Recharger les properties en mémoire après modification
            reload();

        } catch (Exception e) {
            logger.error(
                    "[LOCATOR-READER] Failed to update locator line for element: {}",
                    elementName,
                    e
            );

            throw new RuntimeException(
                    "Erreur lors de la modification de la ligne du locator : " + elementName,
                    e
            );
        }
    }

    private static void save() {
        try (FileOutputStream output =
                     new FileOutputStream(LOCATORS_PATH.toFile())) {

            INSTANCE.properties.store(output, "Updated locators by self-healing");

            logger.info(
                    "[LOCATOR-READER] locators.properties saved successfully: {}",
                    LOCATORS_PATH
            );

        } catch (Exception e) {
            logger.error(
                    "[LOCATOR-READER] Failed to save locators.properties: {}",
                    LOCATORS_PATH,
                    e
            );

            throw new RuntimeException(
                    "Erreur lors de la sauvegarde de locators.properties : " + LOCATORS_PATH,
                    e
            );
        }
    }

    public static void reload() {
        logger.info("[LOCATOR-READER] Reloading locators.properties");
        INSTANCE.load();
    }
}