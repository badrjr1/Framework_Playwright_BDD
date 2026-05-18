package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.LocatorReader;
import core.factory.LocatorFactory;
import core.heal.actions.ElementActions;

public class HomePage {

    private final ElementActions elementActions;

    public HomePage(Page page) {
        this.elementActions = new ElementActions(page);
    }

    public void clickMyAccount() {
        elementActions.click("btn_my_account");
    }

}
