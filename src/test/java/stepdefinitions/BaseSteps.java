package stepdefinitions;

import com.microsoft.playwright.Page;
import hooks.Hooks;
import core.actions.ElementActions;
import core.actions.IElementActions;

public class BaseSteps {

    protected Page page() {
        return Hooks.getPage();
    }

    protected IElementActions actions() {
        return new ElementActions(page());
    }
}