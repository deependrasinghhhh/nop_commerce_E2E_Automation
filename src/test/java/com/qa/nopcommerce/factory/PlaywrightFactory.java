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
        log.info("Initializing Playwright session: browser={}, headless={}, thread={}",
                browserName, headless, Thread.currentThread().getName());

        Playwright playwright = tlPlaywright.get();
        if (playwright == null) {
            playwright = Playwright.create();
            tlPlaywright.set(playwright);
        }

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

        Browser browser = tlBrowser.get();
        if (browser == null || !browser.isConnected()) {
            String channel = ConfigReader.get("browserChannel", "chrome");

            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setSlowMo(ConfigReader.getSlowMo())
                    .setArgs(java.util.Arrays.asList(
                            "--no-sandbox",
                            "--disable-dev-shm-usage"
                    ));

            if (channel != null && !channel.trim().isEmpty() && !channel.equalsIgnoreCase("none")) {
                launchOptions.setChannel(channel);
            }

            if ("firefox".equalsIgnoreCase(browserName)) {
                browser = playwright.firefox().launch(launchOptions);
            } else if ("webkit".equalsIgnoreCase(browserName)) {
                browser = playwright.webkit().launch(launchOptions);
            } else {
                browser = playwright.chromium().launch(launchOptions);
            }
            tlBrowser.set(browser);
        }

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setIgnoreHTTPSErrors(true);

        BrowserContext context = browser.newContext(contextOptions);
        tlContext.set(context);

        if (ConfigReader.isRecordTrace()) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        Page page = context.newPage();
        page.setDefaultTimeout(ConfigReader.getDefaultTimeout());
        page.setDefaultNavigationTimeout(30000);
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

    public static void closeContext() {
        log.info("Closing Page and BrowserContext on thread: {}", Thread.currentThread().getName());
        try {
            Page page = tlPage.get();
            if (page != null) {
                try {
                    page.close();
                } catch (Exception ignored) {}
            }
            BrowserContext context = tlContext.get();
            if (context != null) {
                try {
                    context.close();
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("Error closing context: {}", e.getMessage());
        } finally {
            tlPage.remove();
            tlContext.remove();
        }
    }

    public static void quit() {
        log.info("Tearing down Playwright session on thread: {}", Thread.currentThread().getName());
        try {
            closeContext();
            String cdpUrl = ConfigReader.get("cdpUrl", "");
            boolean isCdp = cdpUrl != null && !cdpUrl.trim().isEmpty() && !"none".equalsIgnoreCase(cdpUrl);
            if (!isCdp) {
                Browser browser = tlBrowser.get();
                if (browser != null && browser.isConnected()) {
                    try {
                        browser.close();
                    } catch (Exception ignored) {}
                }
            }
            Playwright playwright = tlPlaywright.get();
            if (playwright != null) {
                try {
                    playwright.close();
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("Error during Playwright tearDown: {}", e.getMessage());
        } finally {
            tlBrowser.remove();
            tlPlaywright.remove();
        }
    }
}
