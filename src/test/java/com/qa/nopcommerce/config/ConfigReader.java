package com.qa.nopcommerce.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Thread-safe configuration manager that cascades properties from:
 * 1. System Properties (highest precedence)
 * 2. Environment Variables (.env / System.getenv)
 * 3. config.properties (baseline defaults)
 */
public class ConfigReader {
    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final Properties properties = new Properties();
    private static Dotenv dotenv;

    static {
        loadProperties();
        loadDotenv();
    }

    private static void loadProperties() {
        String configPath = "src/test/resources/config/config.properties";
        try (InputStream is = new FileInputStream(configPath)) {
            properties.load(is);
            log.info("Loaded configuration properties from {}", configPath);
        } catch (Exception e) {
            log.warn("Could not load config.properties from file system, attempting classpath load: {}", e.getMessage());
            try (InputStream cpIs = ConfigReader.class.getClassLoader().getResourceAsStream("config/config.properties")) {
                if (cpIs != null) {
                    properties.load(cpIs);
                }
            } catch (Exception ex) {
                log.error("Failed to load config.properties: {}", ex.getMessage());
            }
        }
    }

    private static void loadDotenv() {
        try {
            File envFile = new File(".env");
            if (envFile.exists()) {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
                log.info("Loaded .env file for local environment overrides");
            }
        } catch (Exception e) {
            log.warn("No .env file loaded: {}", e.getMessage());
        }
    }

    public static String get(String key) {
        // 1. Check System Property (-Dkey=value)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }

        // 2. Check Dotenv / Environment Variable (KEY or UPPERCASE_WITH_UNDERSCORES)
        String envKey = key.toUpperCase().replace(".", "_");
        if (dotenv != null) {
            String dotEnvVal = dotenv.get(envKey);
            if (dotEnvVal != null && !dotEnvVal.trim().isEmpty()) {
                return dotEnvVal.trim();
            }
        }
        String sysEnv = System.getenv(envKey);
        if (sysEnv != null && !sysEnv.trim().isEmpty()) {
            return sysEnv.trim();
        }

        // 3. Fallback to properties file
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
    }

    public static String getBaseUrl() {
        return get("baseUrl", "https://demo.nopcommerce.com");
    }

    public static String getAdminUrl() {
        return get("adminUrl", "https://admin-demo.nopcommerce.com/admin/");
    }

    public static String getBrowser() {
        return get("browser", "chromium").toLowerCase();
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static double getSlowMo() {
        return Double.parseDouble(get("slowMo", "0"));
    }

    public static double getDefaultTimeout() {
        return Double.parseDouble(get("defaultTimeout", "15000"));
    }

    public static String getAdminEmail() {
        return get("adminEmail", "admin@yourstore.com");
    }

    public static String getAdminPassword() {
        return get("adminPassword", "admin");
    }

    public static String getDbHost() {
        return get("dbHost", "localhost");
    }

    public static int getDbPort() {
        return Integer.parseInt(get("dbPort", "1433"));
    }

    public static String getDbName() {
        return get("dbName", "nopcommerce_db");
    }

    public static String getDbUser() {
        return get("dbUser", "sa");
    }

    public static String getDbPassword() {
        return get("dbPassword", "NopCommerce@2026!");
    }

    public static boolean isCaptureScreenshotOnFailure() {
        return Boolean.parseBoolean(get("captureScreenshotOnFailure", "true"));
    }

    public static boolean isRecordTrace() {
        return Boolean.parseBoolean(get("recordTrace", "true"));
    }
}
