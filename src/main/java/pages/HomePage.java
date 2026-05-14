package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.LocatorReader;

public class HomePage {

    private final Page page;

    private final Locator myAccount;

    public HomePage(Page page) {
        this.page = page;

        this.myAccount = page.locator(LocatorReader.get("btn_my_account"));
    }

    public void clickMyAccount() {
        myAccount.click();
    }

}
