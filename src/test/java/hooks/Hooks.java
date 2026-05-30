package hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.*;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Hooks {
    // Logs
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    // ThreadLocal assure que chaque thread a sa propre instance
    private static final ThreadLocal<Playwright> playwrightThread = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThread = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThread = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThread = new ThreadLocal<>();

    @Before
    public void initialization(Scenario scenario) {
        try {
            logger.info("Starting scenario: {}", scenario.getName());
            logger.info("Scenario tags: {}", scenario.getSourceTagNames());
            if (scenario.getSourceTagNames().contains("@ui") || scenario.getSourceTagNames().contains("@hybrid")){

                logger.info("UI/Hybrid scenario detected. Starting Playwright initialization");
                Playwright playwright = Playwright.create();

                logger.info("Launching Chromium browser");
                Browser browser = playwright.chromium().launch(
                        new BrowserType
                                .LaunchOptions()
                                .setHeadless(true)
                );

                logger.info("Creating browser context with video recording enabled");
                BrowserContext context = browser.newContext(
                        new Browser.NewContextOptions()
                                .setRecordVideoDir(Paths.get("target/videos/"))
                                .setRecordVideoSize(1280, 720)
                );

                logger.info("Creating new Playwright page");
                Page page = context.newPage();

                if (page == null) {
                    throw new RuntimeException("Page initialization failed (null)");
                }

                playwrightThread.set(playwright);
                browserThread.set(browser);
                contextThread.set(context);
                pageThread.set(page);

                logger.info("Page initialization completed successfully");
            } else  {
                logger.info("Non UI scenario detected. Playwright initialization skipped");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Page initialization failed for scenario: {}", scenario.getName(), e);
            throw new RuntimeException("Failed to initialize Playwright via Healenium: " + e.getMessage());
        }
    }

    @BeforeStep
    public void logScenarioStart(Scenario scenario) {
        logger.info("Starting step execution for scenario: {}", scenario.getName());
    }

    @AfterStep
    public void handleFailure(Scenario scenario) {

        if (!scenario.isFailed()) {
            return;
        }

        logger.error("Step failed in scenario: {}", scenario.getName());

        Page page = pageThread.get();

        if (page == null) {
            scenario.log("Screenshot not captured because page is null");
            logger.warn("Screenshot not captured because page is null for scenario: {}", scenario.getName());
            return;
        }

        try {
            logger.info("Capturing screenshot for failed scenario: {}", scenario.getName());

            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions()
                            .setFullPage(true)
            );

            scenario.attach(
                    screenshot,
                    "image/png",
                    "FAILED_SCREENSHOT_" + sanitizeFileName(scenario.getName())
            );

            logger.info("Screenshot attached to Cucumber report for scenario: {}", scenario.getName());

            Allure.addAttachment(
                    "FAILED_SCREENSHOT_" + scenario.getName(),
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );

            logger.info("Screenshot attached to Allure report for scenario: {}", scenario.getName());

            String currentUrl = page.url();

            scenario.log("Failed at URL: " + currentUrl);
            logger.info("Failed at URL: {}", currentUrl);

        } catch (Exception e) {
            scenario.log("Unable to capture failed screenshot: " + e.getMessage());
            logger.error("Unable to capture failed screenshot for scenario: {}", scenario.getName(), e);
        }
    }

    @After
    public void tearDown(Scenario scenario) {

        String scenarioName = scenario.getName();
        String safeScenarioName = sanitizeFileName(scenarioName);
        String status = scenario.isFailed() ? "FAILED" : "PASSED";

        Page page = pageThread.get();
        BrowserContext context = contextThread.get();
        Browser browser = browserThread.get();
        Playwright playwright = playwrightThread.get();

        logger.info("Starting teardown for scenario: {}", scenarioName);
        logger.info("Scenario final status: {}", status);

        attachLogFileToAllure();

        Path videoPath = null;

        try {
            logger.info("Closing Playwright page and browser context for scenario: {}", scenarioName);

            if (page != null && page.video() != null) {

                logger.info("Video detected for scenario: {}", scenarioName);

                Video video = page.video();

                page.close();
                logger.info("Page closed for scenario: {}", scenarioName);

                if (context != null) {
                    context.close();
                    logger.info("Browser context closed for scenario: {}", scenarioName);
                }

                videoPath = video.path();
                logger.info("Video path resolved for scenario {}: {}", scenarioName, videoPath);

            } else {
                logger.warn("No video object found for scenario: {}", scenarioName);

                if (page != null) {
                    page.close();
                    logger.info("Page closed for scenario: {}", scenarioName);
                }

                if (context != null) {
                    context.close();
                    logger.info("Browser context closed for scenario: {}", scenarioName);
                }
            }

        } catch (Exception e) {
            scenario.log("Unable to close page/context or get video path: " + e.getMessage());
            logger.error("Unable to close page/context or get video path for scenario: {}", scenarioName, e);
        }

        try {
            if (videoPath != null && Files.exists(videoPath)) {

                logger.info("Attaching {} video to Allure for scenario: {}", status, scenarioName);

                Allure.addAttachment(
                        status + "_VIDEO_" + safeScenarioName,
                        "video/webm",
                        Files.newInputStream(videoPath),
                        ".webm"
                );

                scenario.log(status + " video attached: " + videoPath);
                logger.info("{} video attached successfully for scenario: {}", status, scenarioName);

            } else {
                scenario.log("Video not found for scenario: " + scenarioName);
                logger.warn("Video not found for scenario: {}", scenarioName);
            }

        } catch (Exception e) {
            scenario.log("Unable to attach video: " + e.getMessage());
            logger.error("Unable to attach video for scenario: {}", scenarioName, e);
        }

        try {
            if (browser != null) {
                logger.info("Closing browser for scenario: {}", scenarioName);
                browser.close();
                logger.info("Browser closed successfully for scenario: {}", scenarioName);
            }
        } catch (Exception e) {
            logger.warn("Unable to close browser for scenario: {}", scenarioName, e);
        }

        try {
            if (playwright != null) {
                logger.info("Closing Playwright for scenario: {}", scenarioName);
                playwright.close();
                logger.info("Playwright closed successfully for scenario: {}", scenarioName);
            }
        } catch (Exception e) {
            logger.warn("Unable to close Playwright for scenario: {}", scenarioName, e);
        }

        pageThread.remove();
        contextThread.remove();
        browserThread.remove();
        playwrightThread.remove();

        logger.info("ThreadLocal resources cleaned for scenario: {}", scenarioName);
        logger.info("Teardown completed for scenario: {}", scenarioName);
    }

    // Méthode utilitaire pour que vos Step Definitions accèdent à la page
    public static Page getPage() {
        return pageThread.get();
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private void attachLogFileToAllure() {
        try {
            Path logFile = Path.of("target/logs/framework-test.log");

            if (Files.exists(logFile)) {
                Allure.addAttachment(
                        "Framework execution logs",
                        "text/plain",
                        Files.readString(logFile)
                );

                logger.info("Framework log file attached to Allure");
            } else {
                logger.warn("Framework log file not found: {}", logFile);
            }

        } catch (Exception e) {
            logger.warn("Unable to attach framework log file to Allure: {}", e.getMessage());
        }
    }
}
