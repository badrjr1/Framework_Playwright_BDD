package hooks;

import com.microsoft.playwright.*;
import config.ConfigReader;
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

                String browserName = ConfigReader.get("browser.name");
                boolean headless = Boolean.parseBoolean(ConfigReader.get("browser.headless"));

                logger.info("Launching browser | name: {} | headless: {}", browserName, headless);

                BrowserType browserType;
                BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                        .setHeadless(headless);

                switch (browserName.toLowerCase()) {
                    case "chromium":
                        browserType = playwright.chromium();
                        break;

                    case "edge":
                    case "msedge":
                        browserType = playwright.chromium();
                        launchOptions.setChannel("msedge");
                        break;

                    case "chrome":
                        browserType = playwright.chromium();
                        launchOptions.setChannel("chrome");
                        break;

                    case "firefox":
                        browserType = playwright.firefox();
                        break;

                    case "webkit":
                        browserType = playwright.webkit();
                        break;

                    default:
                        logger.error("Unsupported browser name: {}", browserName);
                        throw new RuntimeException(
                                "Unsupported browser name: " + browserName
                                        + ". Supported values are: chromium, chrome, edge, msedge, firefox, webkit"
                        );
                }

                Browser browser = browserType.launch(launchOptions);

                logger.info("Browser launched successfully | name: {}", browserName);

                logger.info("Creating browser context");

                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

                if (Boolean.parseBoolean(ConfigReader.get("video.enabled"))) {
                    logger.info("Video recording enabled");

                    contextOptions
                            .setRecordVideoDir(Paths.get(ConfigReader.get("video.dir")))
                            .setRecordVideoSize(
                                    Integer.parseInt(ConfigReader.get("video.width")),
                                    Integer.parseInt(ConfigReader.get("video.height"))
                            );
                } else {
                    logger.info("Video recording disabled");
                }

                BrowserContext context = browser.newContext(contextOptions);

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
            logger.error("Page initialization failed for scenario: {}", scenario.getName(), e);
            throw new RuntimeException("Failed to initialize Playwright via Healenium: " + e.getMessage());
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

        attachFailureScreenshot(scenario);
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
            if (
                    Boolean.parseBoolean(ConfigReader.get("video.attach"))
                            && videoPath != null
                            && Files.exists(videoPath)
            ) {

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
            if (!Boolean.parseBoolean(ConfigReader.get("logs.attach"))) {
                logger.info("Log attachment is disabled");
                return;
            }

            Path logFile = Path.of(ConfigReader.get("logs.file"));

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

    private void attachFailureScreenshot(Scenario scenario) {

        if (!scenario.isFailed()) {
            return;
        }

        if (!Boolean.parseBoolean(ConfigReader.get("screenshot.on.failure"))) {
            logger.info("Screenshot on failure is disabled");
            return;
        }

        Page page = pageThread.get();

        if (page == null) {
            scenario.log("Screenshot not captured because page is null");
            logger.warn("Screenshot not captured because page is null for scenario: {}", scenario.getName());
            return;
        }

        try {
            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions()
                            .setFullPage(Boolean.parseBoolean(ConfigReader.get("screenshot.full.page")))
            );

            Allure.addAttachment(
                    "FAILED_SCREENSHOT_" + sanitizeFileName(scenario.getName()),
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );

            scenario.log("Failed at URL: " + page.url());
            logger.info("Failure screenshot attached for scenario: {}", scenario.getName());

        } catch (Exception e) {
            scenario.log("Unable to capture failed screenshot: " + e.getMessage());
            logger.error("Unable to capture failed screenshot for scenario: {}", scenario.getName(), e);
        }
    }
}
