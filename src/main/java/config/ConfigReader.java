package config;

public class ConfigReader extends ConfigLoader {

    private static final  ConfigReader INSTANCE = new ConfigReader();

    ConfigReader() {
        super("config.properties");
    }

    public static String get(String key) {
        return INSTANCE.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

}
