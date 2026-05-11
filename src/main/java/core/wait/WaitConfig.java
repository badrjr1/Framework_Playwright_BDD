package core.wait;

import java.io.InputStream;
import java.util.Properties;

public class WaitConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = WaitConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                throw new RuntimeException("config.properties introuvable");
            }

            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement config.properties", e);
        }
    }

    public static double defaultTimeout() {
        return Double.parseDouble(properties.getProperty("default.timeout", "10000"));
    }

    public static double shortTimeout() {
        return Double.parseDouble(properties.getProperty("short.timeout", "3000"));
    }

    public static double longTimeout() {
        return Double.parseDouble(properties.getProperty("long.timeout", "30000"));
    }
}
