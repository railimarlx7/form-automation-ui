package br.jireh.questions;

import br.jireh.locators.HomePageLocators;
import br.jireh.utils.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePageQuestions {

    private final HomePageLocators page;
    private final WebDriver driver;
    private final Wait wait;

    public HomePageQuestions(HomePageLocators page, WebDriver driver) {
        this.page = page;
        this.driver = driver;
        this.wait = new Wait(driver);
    }

    public String getMessage()  { return wait.textOf(page.validationMessage); }
    public String getUsername() { return wait.textOf(page.usernameCell); }
    public String getPassword() { return wait.textOf(page.passwordCell); }
    public String getName()     { return wait.textOf(page.nameCell); }

    public boolean isEntryInTable(String username) {
        return driver.findElements(By.cssSelector(
                "body > section > section.wrapper > div > table tbody td"))
                .stream()
                .anyMatch(el -> username.equals(el.getText().trim()));
    }
}
