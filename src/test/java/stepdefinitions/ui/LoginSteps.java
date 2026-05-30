package stepdefinitions.ui;

import config.ConfigReader;
import hooks.Hooks;
import io.cucumber.java.en.*;
import pages.HomePage;
import pages.LoginPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoginSteps {
    private static final Logger logger = LoggerFactory.getLogger(LoginSteps.class);

    private final HomePage homePage;
    private final LoginPage loginPage;

    public LoginSteps() {
        this.homePage = new HomePage(Hooks.getPage());
        this.loginPage = new LoginPage(Hooks.getPage());
    }

    @Given("log in to the application")
    public void navigate_to() {
        logger.info("Starting login navigation");
        logger.info("Navigating to base URL: {}", ConfigReader.get("base.url"));

        Hooks.getPage().navigate(ConfigReader.get("base.url"));

        logger.info("Navigation completed. Current URL: {}", Hooks.getPage().url());
    }

    @When("click on btn_my_account")
    public void click_on_btn_my_account() {
        logger.info("Clicking on button: btn_my_account");

        homePage.click_btn_my_account();

        logger.info("Clicked successfully on button: btn_my_account");
    }

    @And("write {word} in field txt_email")
    public void write_in_filed_txt_email(String email) {
        logger.info("Writing email in field: txt_email");
        logger.debug("Email value used: {}", email);

        loginPage.write_txt_email(email);


        logger.info("Email entered successfully in field: txt_email");
    }

    @And("write {word} in field txt_password")
    public void write_in_field_txt_password(String password) {
        logger.info("Writing password in field: txt_password");
        logger.debug("Password value is hidden for security reasons");

        loginPage.write_txt_password(password);

        logger.info("Password entered successfully in field: txt_password");
    }

    @And("click on btn_login")
    public void click_on_btn_login() {
        logger.info("Clicking on button: btn_login");

        loginPage.click_btn_login();

        logger.info("Clicked successfully on button: btn_login");
    }

    @Then("assert value of that element lnk_current_url contains {word} is {word}")
    public void assert_that_url_contains_is(String expectedUrlPart, String status) {
        logger.info("Starting URL assertion");
        logger.debug("Current URL: {} expected url is: {}", Hooks.getPage().url(), expectedUrlPart);
        loginPage.assertUrlLogin(expectedUrlPart, Boolean.parseBoolean(status));
        logger.info("URL assertion completed successfully");
    }
}
