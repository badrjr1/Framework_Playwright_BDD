package pages;

import com.microsoft.playwright.Page;
import core.heal.actions.ElementActions;

public class LoginPage {

    private final ElementActions elementActions;

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

}
