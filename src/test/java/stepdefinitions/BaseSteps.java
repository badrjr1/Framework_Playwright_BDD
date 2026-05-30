package stepdefinitions;

import com.microsoft.playwright.Page;
import core.actions.job.IJobActions;
import core.actions.job.JobActions;
import hooks.Hooks;
import core.actions.ui.ElementActions;
import core.actions.ui.IElementActions;

public class BaseSteps {

    protected Page page() {
        return Hooks.getPage();
    }

    protected IElementActions actions() {
        return new ElementActions(page());
    }
    private IJobActions jobActions;

    protected IJobActions jobActions() {
        if (jobActions == null) {
            jobActions = new JobActions();
        }
        return jobActions;
    }
}