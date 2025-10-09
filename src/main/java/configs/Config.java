package configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "config.properties"
        )) {
            if (inputStream == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        // Priority 1 - system value baseAPIUrl = ...
        String systemValue = System.getProperty(key);

        if (systemValue != null) {
            return systemValue;
        }

        // Priority 2 0 environment variable baseAPIUrl - BASEAPIURL
        // admin.username ->ADMIN_USERNAME
        String envKey = key.toUpperCase().replace(".","_");

        String envValue = System.getenv(envKey);
        if(envValue != null) {
            return envValue;
        }
        // Priority 3 - config.properties
        return INSTANCE.properties.getProperty(key);
    }
}

