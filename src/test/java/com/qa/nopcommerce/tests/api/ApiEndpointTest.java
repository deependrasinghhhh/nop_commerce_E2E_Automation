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
        baseUrl = ConfigReader.getBaseUrl();
        playwright = Playwright.create();
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "NopCommerce-API-Probe/1.0");
        requestContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(baseUrl)
                .setIgnoreHTTPSErrors(ConfigReader.isIgnoreHttpsErrors())
                .setExtraHTTPHeaders(headers));
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

    @Test(groups = {"api"})
    @TestCase(value = "TC-API-001", requirementId = "REQ-API-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify storefront root HTTP endpoint returns 200 OK and nopCommerce HTML payload")
    public void testStorefrontRootEndpoint() {
        APIResponse response = requestContext.get("/");
        int status = response.status();
        Assert.assertEquals(status, 200, "Expected HTTP 200 OK from isolated storefront root, received: " + status);
        Assert.assertTrue(response.text().contains("nopCommerce"), "Root HTML body must contain nopCommerce branding");
    }

    @Test(groups = {"api"})
    @TestCase(value = "TC-API-002", requirementId = "REQ-API-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registration HTTP route returns 200 OK and valid registration form HTML")
    public void testCustomerRegisterEndpoint() {
        APIResponse response = requestContext.get("/register");
        int status = response.status();
        Assert.assertEquals(status, 200, "Expected HTTP 200 OK on /register, received: " + status);
        Assert.assertTrue(response.text().contains("Register"), "Expected registration page content");
    }

    @Test(groups = {"api"})
    @TestCase(value = "TC-API-003", requirementId = "REQ-API-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer login HTTP route returns 200 OK and text/html content type")
    public void testCustomerLoginEndpoint() {
        APIResponse response = requestContext.get("/login");
        int status = response.status();
        Assert.assertEquals(status, 200, "Expected HTTP 200 OK on /login, received: " + status);

        String contentType = response.headers().get("content-type");
        Assert.assertNotNull(contentType);
        Assert.assertTrue(contentType.contains("text/html"), "Expected text/html content type");
    }

    @Test(groups = {"api"})
    @TestCase(value = "TC-API-005", requirementId = "REQ-API-001")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify non-existent route returns 404 Not Found or redirect to page-not-found")
    public void testNonExistentRouteHandling() {
        APIResponse response = requestContext.get("/non-existent-page-qa-probe-404");
        int status = response.status();
        Assert.assertTrue(status == 404 || status == 302,
                "Expected 404 Not Found or 302 redirect on unknown route, received: " + status);
    }

    @Test(groups = {"api"})
    @TestCase(value = "TC-API-007", requirementId = "REQ-API-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify protected administration portal root responds with login prompt or redirect")
    public void testAdminPortalEndpoint() {
        APIResponse response = requestContext.get("/Admin");
        int status = response.status();
        Assert.assertTrue(status == 200 || status == 302,
                "Expected status 200 (after redirect to login) or 302 for /Admin, received: " + status);
    }

    @Test(groups = {"api"})
    @TestCase(value = "TC-API-009", requirementId = "REQ-API-004")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify state-modifying POST request without anti-forgery token is rejected with HTTP 400")
    public void testPostWithoutAntiForgeryTokenRejection() {
        com.microsoft.playwright.options.FormData formData = com.microsoft.playwright.options.FormData.create()
                .set("Email", "unauthorized@example.com")
                .set("Password", "InvalidProbePass");

        APIResponse response = requestContext.post("/login", com.microsoft.playwright.options.RequestOptions.create()
                .setForm(formData));

        int status = response.status();
        Assert.assertEquals(status, 400,
                "Expected HTTP 400 anti-forgery rejection, received: " + status);
    }
}
