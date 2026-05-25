package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LocatorReader extends ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(LocatorReader.class);

    private static final Path LOCATORS_DIR =
            Paths.get(ConfigReader.get("locators.root"));

    private static final Map<String, Path> keyFileMap = new HashMap<>();

    private static final ThreadLocal<Properties> scopedProperties =
            ThreadLocal.withInitial(Properties::new);

    private static final ThreadLocal<Path> scopedFile =
            new ThreadLocal<>();

    private static final LocatorReader INSTANCE = new LocatorReader();

    private LocatorReader() {
        super();
        loadAllLocatorFiles();
    }

    public static String get(String key) {
        Properties scoped = scopedProperties.get();

        if (scoped != null && !scoped.isEmpty()) {
            String scopedValue = scoped.getProperty(key);

            if (scopedValue == null || scopedValue.trim().isEmpty()) {
                logger.error(
                        "[LOCATOR-READER] Locator key not found in scoped file | key: {} | file: {}",
                        key,
                        scopedFile.get()
                );

                throw new RuntimeException(
                        "Clé locator introuvable dans le fichier sélectionné : "
                                + key
                                + " | fichier : "
                                + scopedFile.get()
                );
            }

            logger.debug(
                    "[LOCATOR-READER] Scoped locator found | key: {} | value: {} | file: {}",
                    key,
                    scopedValue,
                    scopedFile.get()
            );

            return scopedValue.trim();
        }

        String value = INSTANCE.properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            logger.error("[LOCATOR-READER] Locator key not found globally: {}", key);
            throw new RuntimeException("Clé locator introuvable : " + key);
        }

        logger.debug(
                "[LOCATOR-READER] Global locator found | key: {} | value: {}",
                key,
                value
        );

        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean containsKey(String key) {
        Properties scoped = scopedProperties.get();

        if (scoped != null && !scoped.isEmpty()) {
            return scoped.containsKey(key);
        }

        return INSTANCE.properties.containsKey(key);
    }

    public static Path getFileOfKey(String key) {
        Properties scoped = scopedProperties.get();

        if (scoped != null && scoped.containsKey(key)) {
            return scopedFile.get();
        }

        return keyFileMap.get(key);
    }

    public static void reload() {
        logger.info("[LOCATOR-READER] Reloading all locator files");
        INSTANCE.loadAllLocatorFiles();
    }

    public static void useLocatorFile(String fileName) {
        useLocatorFile(fileName, null);
    }

    public static void useLocatorFile(String fileName, String folderName) {
        try {
            Path locatorPath = buildLocatorPath(fileName, folderName);

            if (!Files.exists(locatorPath)) {
                logger.error("[LOCATOR-READER] Scoped locator file not found: {}", locatorPath);
                throw new RuntimeException("Locator file not found: " + locatorPath);
            }

            Properties scoped = new Properties();

            try (InputStream inputStream = Files.newInputStream(locatorPath)) {
                scoped.load(inputStream);
            }

            scopedProperties.set(scoped);
            scopedFile.set(locatorPath);

            logger.info(
                    "[LOCATOR-READER] Scoped locator file loaded | path: {} | keys: {}",
                    locatorPath,
                    scoped.size()
            );

        } catch (Exception e) {
            logger.error(
                    "[LOCATOR-READER] Failed to load scoped locator file | file: {} | folder: {}",
                    fileName,
                    folderName,
                    e
            );

            throw new RuntimeException(
                    "Erreur lors du chargement du fichier locator : " + fileName,
                    e
            );
        }
    }

    public static void clearLocatorScope() {
        scopedProperties.remove();
        scopedFile.remove();

        logger.info("[LOCATOR-READER] Scoped locator file cleared");
    }

    private static Path buildLocatorPath(String fileName, String folderName) {
        String normalizedFileName = fileName.endsWith(".properties")
                ? fileName
                : fileName + ".properties";

        if (folderName == null || folderName.trim().isEmpty()) {
            return LOCATORS_DIR.resolve(normalizedFileName);
        }

        return LOCATORS_DIR
                .resolve(folderName)
                .resolve(normalizedFileName);
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
                    "[LOCATOR-READER] Locator loading completed | files: {} | keys: {}",
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
                            "[LOCATOR-READER] Invalid line ignored | file: {} | line: {}",
                            file,
                            line
                    );
                    continue;
                }

                String key = trimmedLine.substring(0, trimmedLine.indexOf("=")).trim();
                String value = trimmedLine.substring(trimmedLine.indexOf("=") + 1).trim();

                if (key.isEmpty() || value.isEmpty()) {
                    logger.warn(
                            "[LOCATOR-READER] Empty key/value ignored | file: {} | line: {}",
                            file,
                            line
                    );
                    continue;
                }

                if (properties.containsKey(key)) {
                    Path existingFile = keyFileMap.get(key);

                    logger.error(
                            "[LOCATOR-READER] Duplicate locator key detected | key: {} | first file: {} | second file: {}",
                            key,
                            existingFile,
                            file
                    );

                    throw new RuntimeException(
                            "Clé locator dupliquée : "
                                    + key
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
            if (elementName == null || elementName.trim().isEmpty()) {
                throw new RuntimeException("Le nom de l'élément est vide");
            }

            if (newPrimaryLocator == null || newPrimaryLocator.trim().isEmpty()) {
                throw new RuntimeException("Le nouveau locator est vide pour : " + elementName);
            }

            Path targetFile = scopedFile.get();

            if (targetFile == null) {
                targetFile = keyFileMap.get(elementName);
            }

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
                        "La clé est introuvable dans le fichier cible : "
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

            if (scopedFile.get() != null) {
                reloadScopedFileAfterUpdate(targetFile);
            }

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

    private static void reloadScopedFileAfterUpdate(Path targetFile) {
        try {
            Properties scoped = new Properties();

            try (InputStream inputStream = Files.newInputStream(targetFile)) {
                scoped.load(inputStream);
            }

            scopedProperties.set(scoped);
            scopedFile.set(targetFile);

            logger.info(
                    "[LOCATOR-READER] Scoped locator file reloaded after update | file: {} | keys: {}",
                    targetFile,
                    scoped.size()
            );

        } catch (Exception e) {
            logger.error(
                    "[LOCATOR-READER] Failed to reload scoped locator file after update: {}",
                    targetFile,
                    e
            );

            throw new RuntimeException(
                    "Erreur lors du rechargement du fichier scoped locator : " + targetFile,
                    e
            );
        }
    }
}