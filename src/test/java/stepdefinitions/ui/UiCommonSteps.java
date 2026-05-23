package stepdefinitions.ui;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import stepdefinitions.BaseSteps;

public class UiCommonSteps extends BaseSteps {

    @Given("log in to the application$")
    public void log_in_to_the_application() {
        actions().login();
    }

    @And("search locators in the (.+)$")
    public void search_locators_in_the(String fileName) {
        actions().searchLocatorsInFile(fileName);
    }

    @And("search locators in the (.+) file of common folder$")
    public void search_locators_in_the_file_of_common_folder(String fileName) {
        actions().searchLocatorsInFile(fileName);
    }

    @When("switch to frame (\\d+)$")
    public void switch_to_frame_index(int index) {
        actions().switchToFrameByIndex(index);
    }

    @When("switch to parent frame$")
    public void switch_to_parent_frame() {
        actions().switchToParentFrame();
    }

    @When("switch to frame name (.+)$")
    public void switch_to_frame_name(String frameName) {
        actions().switchToFrameByName(frameName);
    }

    @When("switch to frame id (.+)$")
    public void switch_to_frame_id(String frameId) {
        actions().switchToFrameById(frameId);
    }

    @When("click svg element (.+)$")
    public void click_svg_element(String elementName) {
        actions().clickSvgElement(elementName);
    }

    @When("click on (.+)$")
    public void click_on(String elementName) {
        actions().click(elementName);
    }

    @When("select index (\\d+) from (.+)$")
    public void select_index_from_dropdown(int index, String elementName) {
        actions().selectIndexFromDropdown(elementName, index);
    }

    @When("select text (.+) from (.+)$")
    public void select_text_from_dropdown(String text, String elementName) {
        actions().selectTextFromDropdown(elementName, text);
    }

    @When("write (.+) in field (.+)$")
    public void write_in_field(String value, String elementName) {
        actions().write(elementName, value);
    }

    @When("write value of key (.+) in (.+)$")
    public void write_value_of_key_in_element(String key, String elementName) {
        actions().writeValueOfKey(key, elementName);
    }

    @And("save value of (.+) for later in variable (.+)$")
    public void save_value_of_element_for_later(String elementName, String variableName) {
        actions().saveElementValue(elementName, variableName);
    }

    @Then("assert that element (.+) equals (.+)$")
    public void assert_that_element_equals(String elementName, String expectedValue) {
        actions().assertElementEquals(elementName, expectedValue);
    }

    @Then("assert value of that element (.+) equals (.+)$")
    public void assert_value_of_that_element_equals(String elementName, String expectedValue) {
        actions().assertElementValueEquals(elementName, expectedValue);
    }

    @Then("assert that element (.+) contains (.+)$")
    public void assert_that_element_contains(String elementName, String expectedValue) {
        actions().assertElementContains(elementName, expectedValue);
    }

    @Then("^assert value of that element (.+) contains (.+)$")
    public void assert_value_of_that_element_contains(String elementName, String expectedValue) {

        if (elementName.equalsIgnoreCase("lnk_current_url")) {
            actions().assertUrlContains(expectedValue);
        } else {
            actions().assertElementValueContains(elementName, expectedValue);
        }
    }

    @Then("assert that (.+) is selected in dropdown (.+)$")
    public void assert_that_value_is_selected_in_dropdown(String expectedValue,String elementName) {
        actions().assertDropdownSelectedValue(elementName, expectedValue);
    }

    @Then("assert that element (.+) is checked$")
    public void assert_that_element_is_checked(String elementName) {
        actions().assertElementChecked(elementName);
    }

    @Then("assert that element (.+) is enabled$")
    public void assert_that_element_is_enabled(String elementName) {
        actions().assertElementEnabled(elementName);
    }

    @Then("assert that element (.+) is disabled$")
    public void assert_that_element_is_disabled(String elementName) {
        actions().assertElementDisabled(elementName);
    }

    @When("press (.+) on (.+)$")
    public void press_key_on_element(String key, String elementName) {
        actions().pressKeyOnElement(key, elementName);
    }

    @When("scroll to top$")
    public void scroll_to_top() {
        actions().scrollToTop();
    }

    @When("scroll to bottom$")
    public void scroll_to_bottom() {
        actions().scrollToBottom();
    }

    @When("scroll to element (.+)$")
    public void scroll_to_element(String elementName) {
        actions().scrollToElement(elementName);
    }

    @When("wait for element (.+) to become visible$")
    public void wait_for_element_to_become_visible(String elementName) {
        actions().waitForElementVisible(elementName);
    }

    @When("wait (\\d+) seconds$")
    public void wait_seconds(int seconds) {
        actions().waitSeconds(seconds);
    }

    @When("debug$")
    public void debug() {
        actions().debug();
    }

    @And("save screenshot with name (.+)$")
    public void save_screenshot_with_name(String screenshotName) {
        actions().saveScreenshot(screenshotName);
    }
}
