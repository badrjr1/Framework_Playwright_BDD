package config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigLoader {

    protected final Properties properties = new Properties();
    protected final Path filePath;

    protected ConfigLoader(Path filePath) {
        this.filePath = filePath;
        load();
    }

    protected void load() {
        try (InputStream input = new FileInputStream(filePath.toFile())) {

            properties.clear();
            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur lors du chargement du fichier properties : " + filePath,
                    e
            );
        }
    }

    protected String getProperty(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                    "Clé introuvable dans le fichier properties : " + key
            );
        }

        return value.trim();
    }

    protected int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }
}