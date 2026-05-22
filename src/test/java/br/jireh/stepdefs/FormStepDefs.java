package br.jireh.stepdefs;

import br.jireh.actions.HomePageActions;
import br.jireh.core.DriverFactory;
import br.jireh.locators.HomePageLocators;
import br.jireh.questions.HomePageQuestions;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FormStepDefs {

    private HomePageActions actions;
    private HomePageQuestions questions;

    @Before
    public void setUp() {
        WebDriver driver = DriverFactory.getDriver();
        HomePageLocators locators = new HomePageLocators(driver);
        actions   = new HomePageActions(locators, driver);
        questions = new HomePageQuestions(locators, driver);
    }

    @Given("^I submit the registration form with username \"([^\"]*)\", password \"([^\"]*)\" and name \"([^\"]*)\"$")
    public void iSubmitTheRegistrationForm(String username, String password, String name) {
        actions.enterUsername(username);
        actions.enterPassword(password);
        actions.enterName(name);
        actions.submit();
    }

    @Then("^I should see the message \"([^\"]*)\"$")
    public void iShouldSeeTheMessage(String message) {
        assertEquals(message, questions.getMessage());
    }

    @Then("^the table should display username \"([^\"]*)\", password \"([^\"]*)\" and name \"([^\"]*)\"$")
    public void theTableShouldDisplay(String username, String password, String name) {
        assertEquals(username, questions.getUsername());
        assertEquals(password, questions.getPassword());
        assertEquals(name, questions.getName());
    }

    @When("^I delete the entry$")
    public void iDeleteTheEntry() {
        actions.deleteUser();
    }

    @Then("^the table should no longer display username \"([^\"]*)\", password \"([^\"]*)\" and name \"([^\"]*)\"$")
    public void theTableShouldNoLongerDisplay(String username, String password, String name) {
        assertFalse(questions.isEntryInTable(username),
                "Entry with username '" + username + "' should have been removed from the table");
    }

    @When("^I refresh the page$")
    public void iRefreshThePage() {
        actions.refreshPage();
    }
}
