package com.qa.nopcommerce.factory;

import com.microsoft.playwright.*;
import com.qa.nopcommerce.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * Thread-safe Playwright Factory ensuring strict instance isolation across TestNG threads.
 *
 * <p><strong>Lifecycle contract:</strong></p>
 * <ul>
 *   <li>{@link #initBrowser} — called once per test method ({@code @BeforeMethod}).
 *       Creates a fresh {@code BrowserContext} and {@code Page} each time.
 *       Lazily creates {@code Playwright} and {@code Browser} on first use per thread.</li>
 *   <li>{@link #closeContext} — called once per test method ({@code @AfterMethod}).
 *       Tears down only the context-scoped resources (Page, BrowserContext).</li>
 *   <li>{@link #quit} — called once per test class ({@code @AfterClass}).
 *       Tears down thread-scoped resources (Browser, Playwright) and removes ThreadLocals.</li>
 * </ul>
 *
 * <p>Each TestNG worker thread owns its own Playwright → Browser → BrowserContext → Page chain.
 * Resources are never shared across threads.</p>
 */
public class PlaywrightFactory {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightFactory.class);

    private static final ThreadLocal<Playwright> tlPlaywright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> tlBrowser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> tlContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> tlPage = new ThreadLocal<>();

    /**
     * Initialises a fresh BrowserContext and Page for the calling thread.
     * Reuses the thread's Playwright and Browser instances if they are still alive;
     * otherwise creates new ones.
     *
     * @param browserName one of {@code chromium}, {@code firefox}, {@code webkit}
     * @param headless    {@code true} to run headless
     * @return the newly created {@link Page}
     */
    public static Page initBrowser(String browserName, boolean headless) {
        log.info("Initializing Playwright session: browser={}, headless={}, thread={}",
                browserName, headless, Thread.currentThread().getName());

        // --- 1. Ensure Playwright instance for this thread ---
        Playwright playwright = tlPlaywright.get();
        if (playwright == null) {
            playwright = Playwright.create();
            tlPlaywright.set(playwright);
        }

        // --- 2. CDP remote connection (optional) ---
        String cdpUrl = ConfigReader.get("cdpUrl", "");
        if (cdpUrl != null && !cdpUrl.trim().isEmpty() && !"none".equalsIgnoreCase(cdpUrl)) {
            try {
                log.info("Connecting to existing browser session via CDP: {}", cdpUrl);
                Browser browser = playwright.chromium().connectOverCDP(cdpUrl);
                tlBrowser.set(browser);

                BrowserContext context = browser.contexts().isEmpty()
                        ? browser.newContext()
                        : browser.contexts().get(0);
                tlContext.set(context);

                startTracingIfEnabled(context);

                Page page = context.newPage();
                applyPageDefaults(page);
                tlPage.set(page);
                return page;
            } catch (Exception e) {
                log.warn("CDP connection to {} failed ({}), falling back to direct browser launch",
                        cdpUrl, e.getMessage());
            }
        }

        // --- 3. Ensure Browser instance for this thread ---
        Browser browser = tlBrowser.get();
        if (browser == null || !browser.isConnected()) {
            String channel = ConfigReader.get("browserChannel", "none");

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

            browser = switch (browserName.toLowerCase()) {
                case "firefox" -> playwright.firefox().launch(launchOptions);
                case "webkit" -> playwright.webkit().launch(launchOptions);
                default -> playwright.chromium().launch(launchOptions);
            };
            tlBrowser.set(browser);
        }

        // --- 4. Always create a fresh BrowserContext + Page per test ---
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1366, 768)
                .setIgnoreHTTPSErrors(true);

        BrowserContext context = browser.newContext(contextOptions);
        tlContext.set(context);

        startTracingIfEnabled(context);

        Page page = context.newPage();
        applyPageDefaults(page);
        tlPage.set(page);

        return page;
    }

    // ------------------------------------------------------------------ //
    //  Accessors                                                          //
    // ------------------------------------------------------------------ //

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

    // ------------------------------------------------------------------ //
    //  Tracing                                                            //
    // ------------------------------------------------------------------ //

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

    // ------------------------------------------------------------------ //
    //  Teardown — context-scoped (per test method)                        //
    // ------------------------------------------------------------------ //

    /**
     * Closes the current Page and BrowserContext owned by the calling thread.
     * Idempotent — safe to call even if resources were already closed or never created.
     */
    public static void closeContext() {
        log.info("Closing Page and BrowserContext on thread: {}", Thread.currentThread().getName());
        try {
            Page page = tlPage.get();
            if (page != null && !page.isClosed()) {
                try {
                    page.close();
                } catch (Exception ignored) { /* already closed or stale */ }
            }
            BrowserContext context = tlContext.get();
            if (context != null) {
                try {
                    context.close();
                } catch (Exception ignored) { /* already closed or stale */ }
            }
        } finally {
            tlPage.remove();
            tlContext.remove();
        }
    }

    // ------------------------------------------------------------------ //
    //  Teardown — thread-scoped (per test class)                          //
    // ------------------------------------------------------------------ //

    /**
     * Tears down all Playwright resources owned by the calling thread:
     * Page → BrowserContext → Browser → Playwright, then removes all ThreadLocals.
     * Idempotent.
     */
    public static void quit() {
        log.info("Tearing down Playwright session on thread: {}", Thread.currentThread().getName());
        try {
            closeContext(); // Page + BrowserContext first

            String cdpUrl = ConfigReader.get("cdpUrl", "");
            boolean isCdp = cdpUrl != null && !cdpUrl.trim().isEmpty() && !"none".equalsIgnoreCase(cdpUrl);
            if (!isCdp) {
                Browser browser = tlBrowser.get();
                if (browser != null && browser.isConnected()) {
                    try {
                        browser.close();
                    } catch (Exception ignored) { /* already closed */ }
                }
            }
            Playwright playwright = tlPlaywright.get();
            if (playwright != null) {
                try {
                    playwright.close();
                } catch (Exception ignored) { /* already closed */ }
            }
        } finally {
            tlBrowser.remove();
            tlPlaywright.remove();
        }
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    private static void applyPageDefaults(Page page) {
        double timeout = ConfigReader.getDefaultTimeout();
        page.setDefaultTimeout(timeout);
        page.setDefaultNavigationTimeout(Math.max(timeout, 45_000.0));
    }

    private static void startTracingIfEnabled(BrowserContext context) {
        if (ConfigReader.isRecordTrace()) {
            try {
                context.tracing().start(new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true));
            } catch (Exception e) {
                log.warn("Could not start tracing: {}", e.getMessage());
            }
        }
    }
}
