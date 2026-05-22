package br.jireh.locators;

import br.jireh.core.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePageLocators extends BasePage {

    @FindBy(name = "form_usuario")
    public WebElement usernameField;

    @FindBy(name = "form_senha")
    public WebElement passwordField;

    @FindBy(name = "form_nome")
    public WebElement nameField;

    @FindBy(css = "input.btn.btn-info")
    public WebElement submitButton;

    @FindBy(css = "body > section > section.wrapper > div > form > table > tbody > tr:nth-child(7)")
    public WebElement validationMessage;

    @FindBy(css = "body > section > section.wrapper > div > table > tbody > tr:nth-child(2) > td:nth-child(2)")
    public WebElement nameCell;

    @FindBy(css = "body > section > section.wrapper > div > table > tbody > tr:nth-child(2) > td:nth-child(3)")
    public WebElement usernameCell;

    @FindBy(css = "body > section > section.wrapper > div > table > tbody > tr:nth-child(2) > td:nth-child(4)")
    public WebElement passwordCell;

    @FindBy(css = "body > section > section.wrapper > div > table > tbody > tr:nth-child(2) > td:nth-child(5) > a")
    public WebElement deleteButton;

    @FindBy(css = "body > section > section.wrapper > div > p:nth-child(3) > a")
    public WebElement refreshButton;

    public HomePageLocators(WebDriver driver) {
        super(driver);
    }
}
