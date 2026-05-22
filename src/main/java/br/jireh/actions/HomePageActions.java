package br.jireh.actions;

import br.jireh.domain.User;
import br.jireh.locators.HomePageLocators;
import br.jireh.utils.Wait;
import org.openqa.selenium.WebDriver;

public class HomePageActions {

    private final HomePageLocators page;
    private final Wait wait;

    public HomePageActions(HomePageLocators page, WebDriver driver) {
        this.page = page;
        this.wait = new Wait(driver);
    }

    public void fillForm(User user) {
        enterUsername(user.getUsername());
        enterPassword(user.getPassword());
        enterName(user.getName());
    }

    public void enterUsername(String username) {
        page.usernameField.clear();
        page.usernameField.sendKeys(username);
    }

    public void enterPassword(String password) {
        page.passwordField.clear();
        page.passwordField.sendKeys(password);
    }

    public void enterName(String name) {
        page.nameField.clear();
        page.nameField.sendKeys(name);
    }

    public void submit() {
        wait.untilClickable(page.submitButton).click();
    }

    public void deleteUser() {
        wait.untilClickable(page.deleteButton).click();
    }

    public void refreshPage() {
        wait.untilClickable(page.refreshButton).click();
    }
}
