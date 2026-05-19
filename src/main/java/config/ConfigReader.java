package config;

import java.nio.file.Paths;

public class ConfigReader extends ConfigLoader {

    private static final  ConfigReader INSTANCE = new ConfigReader();

    ConfigReader() {
        super(Paths.get("src/main/resources/config.properties"));
    }

    public static String get(String key) {
        return INSTANCE.getProperty(key);
    }

//    public static int getInt(String key) {
//        return Integer.parseInt(properties.getProperty(key));
//    }

}
