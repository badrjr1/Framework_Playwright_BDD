package pages;

import com.microsoft.playwright.Page;
import pages.actions.ElementActions;
import pages.actions.IElementActions;

public class LoginPage {

    private final IElementActions elementActions;

    public LoginPage(Page page) {

        this.elementActions = new ElementActions(page);
    }

    public void entreEmail(String email) {
        elementActions.write("txt_email", email);

    }

    public void entrePassword(String password) {
        elementActions.write("txt_password", password);
    }

    public void clickLogin() {
        elementActions.click("btn_login");
    }

    public void assertUrlLogin(boolean expected) {
        elementActions.assertUrlContains("lnk_current_url", expected);
    }

}
