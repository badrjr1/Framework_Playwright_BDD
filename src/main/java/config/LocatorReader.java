package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LocatorReader {

    private static final Logger logger = LoggerFactory.getLogger(LocatorReader.class);

    private static final Path LOCATORS_DIR =
            Paths.get("src/main/resources/locators");

    private static final Properties properties = new Properties();

    /**
     * Permet de savoir dans quel fichier se trouve chaque locator.
     * Exemple :
     * txt_email -> src/main/resources/locators/login.properties
     */
    private static final Map<String, Path> keyFileMap = new HashMap<>();

    private static final LocatorReader INSTANCE = new LocatorReader();

    private LocatorReader() {
        loadAllLocatorFiles();
    }

    public static String get(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            logger.error("[LOCATOR-READER] Locator key not found: {}", key);
            throw new RuntimeException("Clé locator introuvable : " + key);
        }

        logger.debug(
                "[LOCATOR-READER] Locator found | key: {} | value: {}",
                key,
                value
        );

        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static void reload() {
        logger.info("[LOCATOR-READER] Reloading all locator files");
        INSTANCE.loadAllLocatorFiles();
    }

    private void loadAllLocatorFiles() {
        try {
            properties.clear();
            keyFileMap.clear();

            if (!Files.exists(LOCATORS_DIR)) {
                throw new RuntimeException("Dossier locators introuvable : " + LOCATORS_DIR);
            }

            logger.info("[LOCATOR-READER] Loading locator files from directory: {}", LOCATORS_DIR);

            List<Path> files = Files.walk(LOCATORS_DIR)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".properties"))
                    .toList();

            if (files.isEmpty()) {
                logger.warn("[LOCATOR-READER] No .properties files found in: {}", LOCATORS_DIR);
            }

            for (Path file : files) {
                loadSingleFile(file);
            }

            logger.info(
                    "[LOCATOR-READER] Locator loading completed. Files: {}, Keys: {}",
                    files.size(),
                    properties.size()
            );

        } catch (Exception e) {
            logger.error("[LOCATOR-READER] Failed to load locator files", e);
            throw new RuntimeException("Erreur lors du chargement des fichiers locators", e);
        }
    }

    private void loadSingleFile(Path file) {
        try {
            logger.info("[LOCATOR-READER] Loading locator file: {}", file);

            List<String> lines = Files.readAllLines(file);

            for (String line : lines) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                if (!trimmedLine.contains("=")) {
                    logger.warn(
                            "[LOCATOR-READER] Invalid line ignored in file {}: {}",
                            file,
                            line
                    );
                    continue;
                }

                String key = trimmedLine.substring(0, trimmedLine.indexOf("=")).trim();
                String value = trimmedLine.substring(trimmedLine.indexOf("=") + 1).trim();

                if (key.isEmpty() || value.isEmpty()) {
                    logger.warn(
                            "[LOCATOR-READER] Empty key/value ignored in file {}: {}",
                            file,
                            line
                    );
                    continue;
                }

                if (properties.containsKey(key)) {
                    Path existingFile = keyFileMap.get(key);

                    logger.error(
                            "[LOCATOR-READER] Duplicate locator key detected: {} | first file: {} | second file: {}",
                            key,
                            existingFile,
                            file
                    );

                    throw new RuntimeException(
                            "Clé locator dupliquée : " + key
                                    + " dans les fichiers : "
                                    + existingFile
                                    + " et "
                                    + file
                    );
                }

                properties.setProperty(key, value);
                keyFileMap.put(key, file);

                logger.debug(
                        "[LOCATOR-READER] Locator loaded | key: {} | file: {}",
                        key,
                        file
                );
            }

        } catch (Exception e) {
            logger.error("[LOCATOR-READER] Failed to load locator file: {}", file, e);
            throw new RuntimeException("Erreur lors du chargement du fichier : " + file, e);
        }
    }

    public static void updatePrimaryLocator(String elementName, String newPrimaryLocator) {
        try {
            if (newPrimaryLocator == null || newPrimaryLocator.trim().isEmpty()) {
                throw new RuntimeException("Le nouveau locator est vide pour : " + elementName);
            }

            Path targetFile = keyFileMap.get(elementName);

            if (targetFile == null) {
                throw new RuntimeException(
                        "Impossible de modifier le locator. Clé introuvable : " + elementName
                );
            }

            logger.info(
                    "[LOCATOR-READER] Updating primary locator | element: {} | file: {} | new primary: {}",
                    elementName,
                    targetFile,
                    newPrimaryLocator
            );

            List<String> lines = Files.readAllLines(targetFile);
            List<String> updatedLines = new ArrayList<>();

            boolean updated = false;

            for (String line : lines) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    updatedLines.add(line);
                    continue;
                }

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
                        "La clé existe en mémoire mais la ligne est introuvable dans le fichier : "
                                + elementName
                                + " | fichier : "
                                + targetFile
                );
            }

            Files.write(targetFile, updatedLines);

            logger.info(
                    "[LOCATOR-READER] Locator file saved successfully: {}",
                    targetFile
            );

            reload();

        } catch (Exception e) {
            logger.error(
                    "[LOCATOR-READER] Failed to update primary locator for element: {}",
                    elementName,
                    e
            );

            throw new RuntimeException(
                    "Erreur lors de la modification du locator : " + elementName,
                    e
            );
        }
    }

    public static boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public static Path getFileOfKey(String key) {
        return keyFileMap.get(key);
    }
}