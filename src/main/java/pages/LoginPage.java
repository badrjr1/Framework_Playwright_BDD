package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import config.LocatorReader;

public class LoginPage {
    private Page page;

    private final Locator emailLocator ;
    private final Locator passwordLocator ;
    private final Locator loginBtn ;


    public LoginPage(Page page) {
        this.page = page;
        this.loginBtn = page.locator(LocatorReader.get("btn_login"));
        this.passwordLocator = page.getByLabel(LocatorReader.get("txt_password"));
        this.emailLocator = page.getByLabel(LocatorReader.get("txt_email"));
    }

    public void entreEmail(String email) {
        emailLocator.fill(email);
    }

    public void entrePassword(String password) {
        passwordLocator.fill(password);
    }

    public void clickLogin() {
        loginBtn.click();
    }

}
