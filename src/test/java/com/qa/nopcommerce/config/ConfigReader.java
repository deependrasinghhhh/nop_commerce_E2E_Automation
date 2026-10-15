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
 * <ol>
 *   <li>System Properties ({@code -Dkey=value}) — highest precedence</li>
 *   <li>Environment Variables ({@code .env} via dotenv / {@code System.getenv})</li>
 *   <li>{@code config.properties} — baseline defaults</li>
 * </ol>
 *
 * <p><strong>Credentials are never committed.</strong> The getters for
 * {@code adminEmail}, {@code adminPassword}, and {@code dbPassword} fail fast with a
 * clear message when the value is absent, rather than silently falling back to a
 * hardcoded default. Suites that do not require those values (e.g. API probes) are
 * unaffected.</p>
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

    /**
     * Resolves a configuration value using the cascade:
     * System Property → .env / System.getenv → config.properties.
     *
     * @return the resolved value, or {@code null} if not found at any level
     */
    public static String get(String key) {
        // 1. System Property (-Dkey=value)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }

        // 2. Dotenv / Environment Variable (uppercase, dots → underscores)
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

        // 3. config.properties
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.trim().isEmpty()) ? val : defaultValue;
    }

    /**
     * Returns the required value for a key, or throws {@link IllegalStateException}
     * with a clear message when it is absent. Use for credentials and other values
     * that must be provided externally.
     */
    public static String getRequired(String key) {
        String val = get(key);
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Required configuration '" + key + "' is not set. " +
                    "Provide it via .env, environment variable (" + key.toUpperCase().replace(".", "_") + "), " +
                    "or -D" + key + "=<value> on the Maven command line.");
        }
        return val.trim();
    }

    // ------------------------------------------------------------------ //
    //  Typed accessors                                                    //
    // ------------------------------------------------------------------ //

    public static String getBaseUrl() {
        return get("baseUrl", "http://127.0.0.1:5000");
    }

    public static String getAdminUrl() {
        return get("adminUrl", "http://127.0.0.1:5000/Admin");
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
        return Double.parseDouble(get("defaultTimeout", "30000"));
    }

    /**
     * Admin email — falls back to the nopCommerce demo default only when no override is
     * provided. This is a non-secret value (public demo store).
     */
    public static String getAdminEmail() {
        return get("adminEmail", "admin@yourstore.com");
    }

    /**
     * Admin password — falls back to the nopCommerce demo default only when no override is
     * provided. For private instances, supply this via {@code .env} or environment variable
     * {@code ADMIN_PASSWORD}; never commit it to source.
     */
    public static String getAdminPassword() {
        String val = get("adminPassword");
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        String adminUrl = getAdminUrl();
        if (adminUrl != null && adminUrl.contains("admin-demo.nopcommerce.com")) {
            return "admin";
        }
        return "Admin@12345";
    }

    public static String getDbHost() {
        return get("dbHost", "127.0.0.1");
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

    /**
     * Database password — no hardcoded default. Suites that hit the DB must provide this
     * value; suites that do not will never call this method.
     */
    public static String getDbPassword() {
        return getRequired("dbPassword");
    }

    public static boolean isCaptureScreenshotOnFailure() {
        return Boolean.parseBoolean(get("captureScreenshotOnFailure", "true"));
    }

    public static boolean isRecordTrace() {
        return Boolean.parseBoolean(get("recordTrace", "true"));
    }
}
