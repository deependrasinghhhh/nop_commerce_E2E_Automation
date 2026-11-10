package com.qa.nopcommerce.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Thread-safe configuration manager that cascades properties from:
 * <ol>
 *   <li>System Properties ({@code -Dkey=value} or {@code -DENV_KEY=value}) – highest precedence</li>
 *   <li>Environment Variables ({@code System.getenv})</li>
 *   <li>Local {@code .env} file (via dotenv-java, if present)</li>
 *   <li>{@code config.properties} – baseline non-secret defaults</li>
 * </ol>
 *
 * <p><strong>Zero Hard-Coded Credentials Policy:</strong> All admin and database credentials
 * ({@code ADMIN_EMAIL}, {@code ADMIN_PASSWORD}, {@code DB_USER}, {@code DB_PASSWORD}) must be
 * supplied via environment variables, CI secret stores, or a local {@code .env} file. Missing
 * secrets fail fast with a clear {@link IllegalStateException}.</p>
 */
public class ConfigReader {
    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final Properties properties = new Properties();
    private static Dotenv dotenv;

    static {
        loadProperties();
        loadDotenv();
    }

    private ConfigReader() {
        // Utility class
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
     * Converts camelCase or dot.separated keys to UPPER_SNAKE_CASE environment variable format.
     */
    private static String toEnvKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return key.replace('.', '_')
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toUpperCase();
    }

    /**
     * Resolves a non-secret configuration value using the cascade:
     * System Property -> Env Var -> .env file -> config.properties -> defaultVal.
     */
    public static String get(String key, String defaultValue) {
        String envKey = toEnvKey(key);

        String sysVal = System.getProperty(key);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal.trim();
        }
        String sysEnvVal = System.getProperty(envKey);
        if (sysEnvVal != null && !sysEnvVal.isBlank()) {
            return sysEnvVal.trim();
        }

        String envVal = System.getenv(key);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }
        String snakeEnvVal = System.getenv(envKey);
        if (snakeEnvVal != null && !snakeEnvVal.isBlank()) {
            return snakeEnvVal.trim();
        }

        if (dotenv != null) {
            String dotenvVal = dotenv.get(envKey);
            if (dotenvVal == null || dotenvVal.isBlank()) {
                dotenvVal = dotenv.get(key);
            }
            if (dotenvVal != null && !dotenvVal.isBlank()) {
                return dotenvVal.trim();
            }
        }

        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.isBlank()) {
            return propVal.trim();
        }

        return defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Resolves a required secret strictly from System Properties, Environment Variables, or .env.
     * Ignores config.properties and throws a descriptive {@link IllegalStateException} when missing.
     */
    private static String requireSecret(String propKey, String envKey) {
        String sysVal = System.getProperty(propKey);
        if (sysVal == null || sysVal.isBlank()) {
            sysVal = System.getProperty(envKey);
        }
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal.trim();
        }

        String envVal = System.getenv(envKey);
        if (envVal == null || envVal.isBlank()) {
            envVal = System.getenv(propKey);
        }
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }

        if (dotenv != null) {
            String dotenvVal = dotenv.get(envKey);
            if (dotenvVal == null || dotenvVal.isBlank()) {
                dotenvVal = dotenv.get(propKey);
            }
            if (dotenvVal != null && !dotenvVal.isBlank()) {
                return dotenvVal.trim();
            }
        }

        throw new IllegalStateException(String.format(
                "Missing required secret '%s' (or system property '-D%s'). " +
                "Configure it via environment variables, GitHub/Jenkins secret stores, or a local .env file.",
                envKey, propKey
        ));
    }

    public static String getBaseUrl() {
        String url = get("baseUrl", "http://127.0.0.1:5000");
        if (url.contains("demo.nopcommerce.com")) {
            throw new IllegalStateException(
                    "Refusing to target shared public demo site (" + url + "). " +
                    "Point BASE_URL / baseUrl to an isolated nopCommerce test instance."
            );
        }
        return url.replaceAll("/+$", "");
    }

    public static String getAppUrl() {
        return getBaseUrl();
    }

    public static String getAdminUrl() {
        String url = get("adminUrl", getBaseUrl() + "/Admin");
        if (url.contains("demo.nopcommerce.com")) {
            throw new IllegalStateException(
                    "Refusing to target shared public admin demo site (" + url + "). " +
                    "Point ADMIN_URL / adminUrl to an isolated nopCommerce test instance."
            );
        }
        return url;
    }

    public static String getBrowser() {
        return get("browserName", get("browser", "chromium"));
    }

    public static String getBrowserChannel() {
        return get("browserChannel", "none");
    }

    public static List<String> getBrowserArgs() {
        String rawArgs = get("browserArgs", "");
        if (rawArgs == null || rawArgs.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(rawArgs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static String getCdpUrl() {
        return get("cdpUrl", "none");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static boolean isIgnoreHttpsErrors() {
        return Boolean.parseBoolean(get("ignoreHttpsErrors", "false"));
    }

    public static double getSlowMo() {
        return Double.parseDouble(get("slowMo", "0"));
    }

    public static double getDefaultTimeout() {
        return Double.parseDouble(get("defaultTimeout", "45000"));
    }

    /**
     * Returns the admin email configured for authentication/admin tests.
     * Fails fast if {@code ADMIN_EMAIL} / {@code adminEmail} is missing.
     */
    public static String getAdminEmail() {
        return requireSecret("adminEmail", "ADMIN_EMAIL");
    }

    /**
     * Returns the admin password configured for authentication/admin tests.
     * Fails fast if {@code ADMIN_PASSWORD} / {@code adminPassword} is missing.
     */
    public static String getAdminPassword() {
        return requireSecret("adminPassword", "ADMIN_PASSWORD");
    }

    // --- Database Configuration ---

    public static String getDbHost() {
        return get("dbHost", "127.0.0.1");
    }

    public static int getDbPort() {
        return Integer.parseInt(get("dbPort", "1433"));
    }

    public static String getDbName() {
        return get("dbName", "nopcommerce_db");
    }

    /**
     * Returns the database username for SQL integration tests.
     * Fails fast if {@code DB_USER} / {@code dbUser} is missing.
     */
    public static String getDbUser() {
        return requireSecret("dbUser", "DB_USER");
    }

    /**
     * Returns the database password for SQL integration tests.
     * Fails fast if {@code DB_PASSWORD} / {@code dbPassword} is missing.
     */
    public static String getDbPassword() {
        return requireSecret("dbPassword", "DB_PASSWORD");
    }

    public static boolean isDbEncrypt() {
        return Boolean.parseBoolean(get("dbEncrypt", "false"));
    }

    public static boolean isDbTrustServerCertificate() {
        return Boolean.parseBoolean(get("dbTrustServerCertificate", "true"));
    }

    public static boolean isRecordTrace() {
        return Boolean.parseBoolean(get("recordTrace", "true"));
    }

    public static boolean isRecordVideo() {
        return Boolean.parseBoolean(get("recordVideo", "false"));
    }

    public static boolean isCaptureScreenshotOnFailure() {
        return Boolean.parseBoolean(get("captureScreenshotOnFailure", "true"));
    }

    public static String getTestEnvName() {
        return get("testEnvName", "isolated-docker");
    }
}
