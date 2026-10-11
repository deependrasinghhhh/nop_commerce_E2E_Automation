package com.qa.nopcommerce.tests.api;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.config.ConfigReader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("Integration & HTTP Surface")
@Feature("Storefront & Admin HTTP Endpoint Probing")
public class ApiEndpointTest {

    private Playwright playwright;
    private APIRequestContext requestContext;
    private String baseUrl;

    @BeforeClass(alwaysRun = true)
    public void setupApiContext() {
        playwright = Playwright.create();
        baseUrl = ConfigReader.getBaseUrl();
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        requestContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(baseUrl)
                .setExtraHTTPHeaders(headers)
                .setIgnoreHTTPSErrors(true));
    }

    @AfterClass(alwaysRun = true)
    public void tearDownApiContext() {
        if (requestContext != null) {
            requestContext.dispose();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test(groups = {"api", "smoke"})
    @TestCase("TC-API-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify storefront root HTTP endpoint responds with valid HTTP status (200 OK or 403 WAF challenge)")
    public void testStorefrontRootEndpoint() {
        APIResponse response = requestContext.get("/");
        int status = response.status();
        Assert.assertTrue(status == 200 || status == 403,
                "Expected status 200 (OK) or 403 (WAF Challenge), but received: " + status);
    }

    @Test(groups = {"api"})
    @TestCase("TC-API-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer login HTTP route returns HTML content type")
    public void testCustomerLoginEndpoint() {
        APIResponse response = requestContext.get("/login");
        int status = response.status();
        Assert.assertTrue(status == 200 || status == 403,
                "Expected status 200 or 403, but received: " + status);

        if (status == 200) {
            String contentType = response.headers().get("content-type");
            Assert.assertNotNull(contentType);
            Assert.assertTrue(contentType.contains("text/html"), "Expected text/html content type");
        }
    }

    @Test(groups = {"api"})
    @TestCase("TC-API-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registration HTTP route returns valid HTML response")
    public void testCustomerRegisterEndpoint() {
        APIResponse response = requestContext.get("/register");
        int status = response.status();
        Assert.assertTrue(status == 200 || status == 403,
                "Expected status 200 or 403, but received: " + status);
    }

    @Test(groups = {"api"})
    @TestCase("TC-API-004")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify administration portal root responds with redirect, login prompt, or challenge")
    public void testAdminPortalEndpoint() {
        APIResponse response = requestContext.get("/admin");
        int status = response.status();
        Assert.assertTrue(status == 200 || status == 302 || status == 403,
                "Expected status 200, 302, or 403 for admin portal, but received: " + status);
    }

    @Test(groups = {"api"})
    @TestCase("TC-API-005")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify non-existent route returns 404 Not Found or redirect")
    public void testNonExistentRouteHandling() {
        APIResponse response = requestContext.get("/non-existent-page-qa-probe-404");
        int status = response.status();
        Assert.assertTrue(status == 404 || status == 302 || status == 403,
                "Expected 404 Not Found or 403 WAF response, but received: " + status);
    }

    @Test(groups = {"api"})
    @TestCase("TC-API-006")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify state-modifying POST request without anti-forgery token is rejected (400, 403, or redirect)")
    public void testPostWithoutAntiForgeryTokenRejection() {
        com.microsoft.playwright.options.FormData formData = com.microsoft.playwright.options.FormData.create()
                .set("Email", "unauthorized@example.com")
                .set("Password", "InvalidPassword123");

        APIResponse response = requestContext.post("/login", com.microsoft.playwright.options.RequestOptions.create()
                .setForm(formData));

        int status = response.status();
        Assert.assertTrue(status == 400 || status == 403 || status == 405 || status == 302,
                "Expected POST rejection status (400/403/405/302), received: " + status);
    }
}
