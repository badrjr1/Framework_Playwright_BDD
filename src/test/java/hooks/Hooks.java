package hooks;

import com.microsoft.playwright.*;
import io.cucumber.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {
    //
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    // ThreadLocal assure que chaque thread a sa propre instance
    private static ThreadLocal<Playwright> playwrightThread = new ThreadLocal<>();
    private static ThreadLocal<Browser> browserThread = new ThreadLocal<>();
    private static ThreadLocal<Page> pageThread = new ThreadLocal<>();
    private static ThreadLocal<BrowserContext> contextThread = new ThreadLocal<>();

    @Before
    public void initialization(Scenario scenario) {
        try {
            if (!scenario.getSourceTagNames().contains("@api")){
                playwrightThread.set(Playwright.create());
                Browser browser = playwrightThread.get().chromium().launch(
                        new BrowserType.LaunchOptions().setHeadless(false)
                );
                browserThread.set(browser);

                BrowserContext context = browser.newContext();
                Page page = context.newPage();

                if (page == null) {
                    throw new RuntimeException("Page initialization failed (null)");
                }

                contextThread.set(context);
                pageThread.set(page);

                logger.info("Page initialization started");
            } else  {
                logger.info("Test API détecté, Playwright non initialisé.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Page initialization failed");
            throw new RuntimeException("Failed to initialize Playwright via Healenium: " + e.getMessage());
        }
    }

    @BeforeStep
    public void logScenarioStart(Scenario scenario) {
        // Utile pour tracer quel scénario tourne sur quel thread dans les logs CI
        System.out.println("Sarting Scenario: " + scenario.getName());
        logger.info("Sarting Scenario: " + scenario.getName());
    }

    @AfterStep
    public void handleFailure(Scenario scenario) {
        if (scenario.isFailed()) {
            Page page = pageThread.get();
            // Capture d'écran Pleine Page pour Allure
            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions()
                            .setFullPage(true)
            );
            // Attachement automatique au rapport Allure via Cucumber
            scenario.attach(screenshot, "image/png", "FAILED_SCREENSHOT");

            // Log de l'URL actuelle pour aider au debug
            scenario.log("Failed at URL: " + pageThread.get().url());

            logger.info("Failed at URL: " + pageThread.get().url());
        }
    }

    @After
    public void tearDown() {
        // Fermeture propre pour libérer les ressources Docker
        if (pageThread.get() != null) pageThread.get().close();
        if (browserThread.get() != null) browserThread.get().close();
        if (playwrightThread.get() != null) playwrightThread.get().close();

        // Nettoyage du thread
        pageThread.remove();
        browserThread.remove();
        playwrightThread.remove();

        logger.info("Browser closed");
        logger.info("Playwright closed");
        logger.info("Page closed");
    }

    // Méthode utilitaire pour que vos Step Definitions accèdent à la page
    public static Page getPage() {
        return pageThread.get();
    }

}
