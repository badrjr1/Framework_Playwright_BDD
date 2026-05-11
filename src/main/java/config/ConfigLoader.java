package config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    protected static final Properties properties = new Properties();

    protected ConfigLoader(String path){
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (input == null) {
                throw new RuntimeException("Fichier config.properties introuvable");
            }

            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement du fichier properties", e);
        }
    }

    protected String getProperty(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Clé introuvable dans le fichier properties : " + key);
        }

        return value;
    }

    protected int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }
}
