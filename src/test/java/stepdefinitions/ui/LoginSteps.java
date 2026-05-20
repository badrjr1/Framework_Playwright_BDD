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


//    @Then("assert value of that element lnk_current_url contains {word} is {word}")
//    public void assert_that_url_contains_is(String expectedUrlPart, String status) {
//        logger.info("Starting URL assertion");
//        logger.debug("Current URL: {} expected url is: {}", Hooks.getPage().url(), expectedUrlPart);
//        loginPage.assertUrlLogin(expectedUrlPart, Boolean.parseBoolean(status));
//        logger.info("URL assertion completed successfully");
//    }
}
