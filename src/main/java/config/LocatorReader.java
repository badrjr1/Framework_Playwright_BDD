package config;

public class LocatorReader extends ConfigLoader {

    private static final LocatorReader INSTANCE = new LocatorReader();

     LocatorReader() {
        super("locators.properties");
    }

    public static String get(String key) {
        return INSTANCE.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}
