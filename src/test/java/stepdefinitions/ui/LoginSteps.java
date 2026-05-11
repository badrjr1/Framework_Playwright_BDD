package stepdefinitions.ui;

import core.wait.WaitUtils;
import hooks.Hooks;
import io.cucumber.java.en.*;
import io.cucumber.java.en_scouse.An;
import pages.HomePage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginSteps {

    private final HomePage homePage;
    private final LoginPage loginPage;

    public LoginSteps() {
        this.homePage = new HomePage(Hooks.getPage());
        this.loginPage = new LoginPage(Hooks.getPage());
    }

    @Given("navigate to {string}")
    public void navigate_to(String string) {
        Hooks.getPage().navigate(string);
    }
    @When("click on btn_my_account")
    public void click_on_btn_my_account() {
        homePage.clickMyAccount();
    }
    @And("write {string} in field txt_email")
    public void write_in_filed_txt_email(String string) {
        loginPage.entreEmail(string);
    }
    @And("write {string} in field txt_password")
    public void write_in_field_txt_password(String string) {
        loginPage.entrePassword(string);
    }
    @And("click on btn_login")
    public void click_on_btn_login() {
        loginPage.clickLogin();
    }
    @Then("assert that url contains {string} is {string}")
    public void assert_that_url_contains_is(String string, String string2) {
        boolean expectedStatus = Boolean.parseBoolean(string2);
        boolean actualStatus = Hooks.getPage().url().contains(string);

        assertEquals(expectedStatus, actualStatus,
                "URL assertion failed. Expected URL contains '"
                        + string
                        + "' to be: "
                        + expectedStatus
                        + " but actual URL was: "
                        + Hooks.getPage().url());
    }
}
