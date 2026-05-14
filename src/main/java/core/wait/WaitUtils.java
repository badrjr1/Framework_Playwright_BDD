package core.wait;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class WaitUtils {

    /**
     * À utiliser quand on veut vérifier qu'un élément est visible.
     * Playwright attend automatiquement jusqu'au timeout.
     */
    public static void waitForVisible(Locator locator) {
        assertThat(locator).isVisible();
    }

    /**
     * Version simple avec Locator.waitFor().
     */
    public static void waitUntilVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(WaitConfig.defaultTimeout()));
    }

    /**
     * Attendre qu'un élément soit attaché au DOM.
     * Utile avant self-healing ou avant vérification technique.
     */
    public static void waitUntilAttached(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(WaitConfig.defaultTimeout()));
    }

    /**
     * Attendre qu'un élément disparaisse.
     */
    public static void waitUntilHidden(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(WaitConfig.defaultTimeout()));
    }

    /**
     * Attendre le chargement DOM.
     */
    public static void waitForDomLoaded(Page page) {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED,
                new Page.WaitForLoadStateOptions()
                        .setTimeout(WaitConfig.defaultTimeout()));
    }

    /**
     * Attendre chargement réseau raisonnable.
     * À utiliser avec prudence, pas partout.
     */
    public static void waitForNetworkIdle(Page page) {
        page.waitForLoadState(LoadState.NETWORKIDLE,
                new Page.WaitForLoadStateOptions()
                        .setTimeout(WaitConfig.longTimeout()));
    }

    /**
     * Attendre une URL spécifique ou partielle.
     */
    public static void waitForUrlContains(Page page, String expectedPart) {
        page.waitForURL(url -> url.contains(expectedPart),
                new Page.WaitForURLOptions()
                        .setTimeout(WaitConfig.defaultTimeout()));
    }

    /**
     * Méthode interdite volontairement.
     * Elle sert à éviter l'utilisation abusive de Thread.sleep().
     */
    public static void hardWait(long milliseconds) {
        throw new UnsupportedOperationException(
                "Thread.sleep est interdit dans le framework. Utiliser WaitUtils ou les assertions Playwright."
        );
    }
}