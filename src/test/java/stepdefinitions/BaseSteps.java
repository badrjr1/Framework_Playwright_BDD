package stepdefinitions;

import com.microsoft.playwright.Page;
import core.actions.job.IJobActions;
import core.actions.job.JobActions;
import core.actions.websocket.IWebSocketActions;
import core.actions.websocket.WebSocketActions;
import hooks.Hooks;
import core.actions.ui.ElementActions;
import core.actions.ui.IElementActions;

public class BaseSteps {

    private IElementActions elementActions;
    private IJobActions jobActions;
    private IWebSocketActions webSocketActions;

    protected Page page() {
        return Hooks.getPage();
    }

    protected IElementActions actions() {
        if (elementActions == null){
            elementActions = new ElementActions(page());
        }
        return elementActions;
    }

    protected IJobActions jobActions() {
        if (jobActions == null) {
            jobActions = new JobActions();
        }
        return jobActions;
    }

    protected IWebSocketActions webSocketActions() {
        if (webSocketActions == null) {
            webSocketActions = new WebSocketActions();
        }
        return webSocketActions;
    }
}