package pages;

import com.microsoft.playwright.Page;
import core.actions.ElementActions;
import core.actions.IElementActions;

public class LoginPage {

    private final IElementActions elementActions;

    public LoginPage(Page page) {

        this.elementActions = new ElementActions(page);
    }

    public void write_txt_email(String email) {
        elementActions.write("txt_email", email);

    }

    public void write_txt_password(String password) {
        elementActions.write("txt_password", password);
    }

    public void click_btn_login() {
        elementActions.click("btn_login");
    }

    public void assertUrlLogin(String elementName, boolean expected) {
        elementActions.assertUrlContains(elementName, expected);
    }

}
