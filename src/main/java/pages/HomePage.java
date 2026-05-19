package pages;

import com.microsoft.playwright.Page;
import pages.actions.ElementActions;
import pages.actions.IElementActions;

public class HomePage {

    private final IElementActions elementActions;

    public HomePage(Page page) {

        this.elementActions = new ElementActions(page);
    }

    public void click_btn_my_account() {
        elementActions.click("btn_my_account");
    }

}
