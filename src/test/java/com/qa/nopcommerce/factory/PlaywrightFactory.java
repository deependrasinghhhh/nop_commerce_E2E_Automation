package com.qa.nopcommerce.factory;

import com.microsoft.playwright.*;
import com.qa.nopcommerce.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe Playwright Factory ensuring strict instance isolation across TestNG threads.
 *
 * <p><strong>Isolation & Security Guarantees:</strong></p>
 * <ul>
 *   <li>Even when connecting via CDP ({@code cdpUrl}), a brand-new isolated {@link BrowserContext}
 *       is created per test method—never reusing {@code browser.contexts().get(0)}.</li>
 *   <li>HTTPS certificate errors are ignored ONLY when explicitly enabled via
 *       {@code ignoreHttpsErrors=true} / {@code IGNORE_HTTPS_ERRORS=true}.</li>
 *   <li>Browser launch arguments are scoped to the specific browser engine family
 *       (Chromium/Chrome/Edge vs. Firefox vs. WebKit).</li>
 * </ul>
 */
public class PlaywrightFactory {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightFactory.class);

    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> tlCdpSession = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> tlTraceStopped = ThreadLocal.withInitial(() -> false);

    private PlaywrightFactory() {
        // Utility class
    }

    /**
     * Initialises a fresh, isolated {@link BrowserContext} and {@link Page} for the calling thread.
     */
    public static Page initBrowser(String browserName, boolean headless) {
        boolean ignoreHttpsErrors = ConfigReader.isIgnoreHttpsErrors();
        log.info("Initializing Playwright session: browser={}, headless={}, ignoreHttpsErrors={}, thread={}",
                browserName, headless, ignoreHttpsErrors, Thread.currentThread().getName());

        Playwright playwright = tlPlaywright.get();
        if (playwright == null) {
            playwright = Playwright.create();
            tlPlaywright.set(playwright);
        }

        tlTraceStopped.set(false);

        // --- Optional CDP remote connection (always creates an isolated context) ---
        String cdpUrl = ConfigReader.getCdpUrl();
        if (cdpUrl != null && !cdpUrl.trim().isEmpty() && !"none".equalsIgnoreCase(cdpUrl)) {
            log.info("Connecting to browser via CDP endpoint: {}", cdpUrl);
            Browser browser = tlBrowser.get();
            if (browser == null || !browser.isConnected()) {
                browser = playwright.chromium().connectOverCDP(cdpUrl);
                tlBrowser.set(browser);
                tlCdpSession.set(true);
            }

            // NEVER reuse browser.contexts().get(0); always create a fresh isolated context
            BrowserContext context = browser.newContext(buildContextOptions(ignoreHttpsErrors));
            tlContext.set(context);
            startTracingIfEnabled(context);

            Page page = context.newPage();
            page.setDefaultTimeout(ConfigReader.getDefaultTimeout());
            tlPage.set(page);
            return page;
        }

        // --- Standard per-thread Browser instance ---
        Browser browser = tlBrowser.get();
        if (browser == null || !browser.isConnected()) {
            browser = createBrowserInstance(playwright, browserName, headless);
            tlBrowser.set(browser);
            tlCdpSession.set(false);
        }

        // --- Fresh isolated BrowserContext per test method ---
        BrowserContext context = browser.newContext(buildContextOptions(ignoreHttpsErrors));
        tlContext.set(context);
        startTracingIfEnabled(context);

        Page page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getDefaultTimeout());
        tlPage.set(page);

        return page;
    }

    private static Browser.NewContextOptions buildContextOptions(boolean ignoreHttpsErrors) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setIgnoreHTTPSErrors(ignoreHttpsErrors);

        if (ConfigReader.isRecordVideo()) {
            options.setRecordVideoDir(Paths.get("evidence/videos/"));
        }
        return options;
    }

    private static Browser createBrowserInstance(Playwright playwright, String browserName, boolean headless) {
        String normalizedBrowser = browserName == null ? "chromium" : browserName.toLowerCase().trim();
        String channel = ConfigReader.getBrowserChannel();
        boolean useChannel = channel != null && !channel.isBlank() && !"none".equalsIgnoreCase(channel);
        double slowMo = ConfigReader.getSlowMo();
        List<String> customArgs = ConfigReader.getBrowserArgs();

        switch (normalizedBrowser) {
            case "firefox": {
                List<String> ffArgs = new ArrayList<>();
                if (headless) {
                    ffArgs.add("-width=1920");
                    ffArgs.add("-height=1080");
                }
                ffArgs.addAll(customArgs);
                log.info("Launching Firefox (headless={}, args={})", headless, ffArgs);
                return playwright.firefox().launch(new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(slowMo)
                        .setArgs(ffArgs));
            }

            case "webkit":
            case "safari": {
                List<String> wkArgs = new ArrayList<>(customArgs);
                log.info("Launching WebKit (headless={}, args={})", headless, wkArgs);
                return playwright.webkit().launch(new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(slowMo)
                        .setArgs(wkArgs));
            }

            case "chrome": {
                List<String> chromeArgs = buildChromiumArgs(customArgs);
                log.info("Launching Google Chrome (channel=chrome, headless={}, args={})", headless, chromeArgs);
                return playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setChannel("chrome")
                        .setHeadless(headless)
                        .setSlowMo(slowMo)
                        .setArgs(chromeArgs));
            }

            case "msedge":
            case "edge": {
                List<String> edgeArgs = buildChromiumArgs(customArgs);
                log.info("Launching Microsoft Edge (channel=msedge, headless={}, args={})", headless, edgeArgs);
                return playwright.chromium().launch(new BrowserType.LaunchOptions()
                        .setChannel("msedge")
                        .setHeadless(headless)
                        .setSlowMo(slowMo)
                        .setArgs(edgeArgs));
            }

            case "chromium":
            default: {
                List<String> chromiumArgs = buildChromiumArgs(customArgs);
                log.info("Launching Chromium (channel={}, headless={}, args={})",
                        useChannel ? channel : "default", headless, chromiumArgs);
                BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(slowMo)
                        .setArgs(chromiumArgs);
                if (useChannel) {
                    options.setChannel(channel);
                }
                return playwright.chromium().launch(options);
            }
        }
    }

    private static List<String> buildChromiumArgs(List<String> customArgs) {
        List<String> args = new ArrayList<>();
        args.add("--disable-dev-shm-usage");
        args.add("--no-sandbox");
        args.add("--disable-gpu");
        args.add("--disable-extensions");
        args.add("--window-size=1920,1080");
        if (customArgs != null) {
            args.addAll(customArgs);
        }
        return args;
    }

    private static void startTracingIfEnabled(BrowserContext context) {
        if (ConfigReader.isRecordTrace()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }
    }

    /**
     * Stops active Playwright tracing and exports the zip archive to {@code tracePath}.
     */
    public static void stopTracing(String tracePath) {
        BrowserContext context = tlContext.get();
        if (context != null && ConfigReader.isRecordTrace() && !Boolean.TRUE.equals(tlTraceStopped.get())) {
            try {
                context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(tracePath)));
                tlTraceStopped.set(true);
                log.info("Saved Playwright execution trace to {}", tracePath);
            } catch (Exception e) {
                log.warn("Could not save execution trace to {}: {}", tracePath, e.getMessage());
            }
        }
    }

    public static Playwright getPlaywright() {
        return tlPlaywright.get();
    }

    public static Browser getBrowser() {
        return tlBrowser.get();
    }

    public static BrowserContext getContext() {
        return tlContext.get();
    }

    public static Page getPage() {
        return tlPage.get();
    }

    /**
     * Closes current test's Page and isolated BrowserContext.
     */
    public static void closeContext() {
        closeContext("test");
    }

    public static void closeContext(String testName) {
        Page page = tlPage.get();
        if (page != null) {
            try {
                if (!page.isClosed()) {
                    page.close();
                }
            } catch (Exception e) {
                log.warn("Error closing Page: {}", e.getMessage());
            } finally {
                tlPage.remove();
            }
        }

        BrowserContext context = tlContext.get();
        if (context != null) {
            try {
                if (ConfigReader.isRecordTrace() && !Boolean.TRUE.equals(tlTraceStopped.get())) {
                    String tracePath = "evidence/traces/" + sanitizeFileName(testName) + "-trace.zip";
                    context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get(tracePath)));
                    tlTraceStopped.set(true);
                }
            } catch (Exception e) {
                log.debug("Trace already stopped or unavailable for {}: {}", testName, e.getMessage());
            } finally {
                try {
                    context.close();
                } catch (Exception e) {
                    log.warn("Error closing BrowserContext: {}", e.getMessage());
                }
                tlContext.remove();
                tlTraceStopped.remove();
            }
        }
    }

    /**
     * Tears down Browser and Playwright for the current thread.
     */
    public static void quit() {
        closeContext();

        Browser browser = tlBrowser.get();
        if (browser != null) {
            try {
                if (!Boolean.TRUE.equals(tlCdpSession.get())) {
                    browser.close();
                }
            } catch (Exception e) {
                log.warn("Error closing Browser: {}", e.getMessage());
            } finally {
                tlBrowser.remove();
                tlCdpSession.remove();
            }
        }

        Playwright playwright = tlPlaywright.get();
        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception e) {
                log.warn("Error closing Playwright: {}", e.getMessage());
            } finally {
                tlPlaywright.remove();
            }
        }
        log.info("Playwright session shut down for thread {}", Thread.currentThread().getName());
    }

    private static String sanitizeFileName(String name) {
        return name == null ? "test" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
