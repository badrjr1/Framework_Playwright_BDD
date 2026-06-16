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
        return Double.parseDouble(properties.getProperty("timeout.default", "10000"));
    }

    public static double shortTimeout() {
        return Double.parseDouble(properties.getProperty("timeout.short", "3000"));
    }

    public static double longTimeout() {
        return Double.parseDouble(properties.getProperty("timeout.long", "30000"));
    }
}
