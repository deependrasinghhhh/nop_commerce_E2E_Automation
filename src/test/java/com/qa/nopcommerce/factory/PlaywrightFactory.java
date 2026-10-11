package com.qa.nopcommerce.factory;

import com.microsoft.playwright.*;
import com.qa.nopcommerce.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * Thread-safe Playwright Factory ensuring strict instance isolation across TestNG threads.
 * Uses ThreadLocal for Playwright, Browser, BrowserContext, and Page instances.
 */
public class PlaywrightFactory {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightFactory.class);

    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

    public static Page initBrowser(String browserName, boolean headless) {
        log.info("Initializing Playwright browser: {} (headless: {}) on thread: {}",
                browserName, headless, Thread.currentThread().getName());

        Playwright playwright = Playwright.create();
        tlPlaywright.set(playwright);

        String cdpUrl = ConfigReader.get("cdpUrl", "");
        if (cdpUrl != null && !cdpUrl.trim().isEmpty() && !"none".equalsIgnoreCase(cdpUrl)) {
            try {
                log.info("Connecting to existing browser session via CDP: {}", cdpUrl);
                Browser browser = playwright.chromium().connectOverCDP(cdpUrl);
                tlBrowser.set(browser);

                BrowserContext context = browser.contexts().isEmpty() ? browser.newContext() : browser.contexts().get(0);
                tlContext.set(context);

                if (ConfigReader.isRecordTrace()) {
                    try {
                        context.tracing().start(new Tracing.StartOptions()
                                .setScreenshots(true)
                                .setSnapshots(true)
                                .setSources(true));
                    } catch (Exception ignored) {}
                }

                Page page = context.newPage();
                page.setDefaultTimeout(ConfigReader.getDefaultTimeout());
                tlPage.set(page);
                return page;
            } catch (Exception e) {
                log.warn("CDP connection to {} failed ({}), falling back to direct browser launch", cdpUrl, e.getMessage());
            }
        }

        String channel = ConfigReader.get("browserChannel", "chrome");

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(ConfigReader.getSlowMo())
                .setIgnoreDefaultArgs(java.util.Arrays.asList("--enable-automation"))
                .setArgs(java.util.Arrays.asList(
                        "--disable-blink-features=AutomationControlled",
                        "--disable-features=IsolateOrigins,site-per-process",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-infobars",
                        "--no-first-run",
                        "--no-default-browser-check",
                        "--window-size=1366,768"
                ));

        if (channel != null && !channel.trim().isEmpty() && !channel.equalsIgnoreCase("none")) {
            launchOptions.setChannel(channel);
        }

        Browser browser;
        if ("firefox".equalsIgnoreCase(browserName)) {
            browser = playwright.firefox().launch(launchOptions);
        } else if ("webkit".equalsIgnoreCase(browserName)) {
            browser = playwright.webkit().launch(launchOptions);
        } else {
            browser = playwright.chromium().launch(launchOptions);
        }
        tlBrowser.set(browser);

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setIgnoreHTTPSErrors(true);

        BrowserContext context = browser.newContext(contextOptions);
        tlContext.set(context);

        // Inject stealth anti-detection script across all pages and frames in this context
        context.addInitScript(
                "// 1. Mask webdriver\n" +
                "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });\n" +
                "// 2. Mock plugins\n" +
                "Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });\n" +
                "// 3. Mock languages\n" +
                "Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });\n" +
                "// 4. Mock chrome object\n" +
                "window.chrome = window.chrome || { runtime: {}, app: { isInstalled: false } };\n" +
                "// 5. Mock permissions\n" +
                "if (navigator.permissions && navigator.permissions.query) {\n" +
                "    const orig = navigator.permissions.query;\n" +
                "    navigator.permissions.query = (p) => p.name === 'notifications' ? Promise.resolve({ state: Notification.permission }) : orig(p);\n" +
                "}\n"
        );

        if (ConfigReader.isRecordTrace()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        Page page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getDefaultTimeout());
        tlPage.set(page);

        return page;
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

    public static void stopTracing(String traceFilePath) {
        BrowserContext context = tlContext.get();
        if (context != null && ConfigReader.isRecordTrace()) {
            try {
                context.tracing().stop(new Tracing.StopOptions()
                        .setPath(Paths.get(traceFilePath)));
                log.info("Saved Playwright execution trace to {}", traceFilePath);
            } catch (Exception e) {
                log.warn("Could not save trace: {}", e.getMessage());
            }
        }
    }

    public static void quit() {
        log.info("Tearing down Playwright session on thread: {}", Thread.currentThread().getName());
        try {
            Page page = tlPage.get();
            if (page != null) {
                try {
                    page.close();
                } catch (Exception ignored) {}
            }
            String cdpUrl = ConfigReader.get("cdpUrl", "");
            boolean isCdp = cdpUrl != null && !cdpUrl.trim().isEmpty() && !"none".equalsIgnoreCase(cdpUrl);
            if (!isCdp) {
                BrowserContext context = tlContext.get();
                if (context != null) {
                    context.close();
                }
                Browser browser = tlBrowser.get();
                if (browser != null) {
                    browser.close();
                }
            }
            Playwright playwright = tlPlaywright.get();
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception e) {
            log.warn("Error during Playwright tearDown: {}", e.getMessage());
        } finally {
            tlPage.remove();
            tlContext.remove();
            tlBrowser.remove();
            tlPlaywright.remove();
        }
    }
}
