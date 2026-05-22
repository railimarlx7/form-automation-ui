package br.jireh.utils;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Wait {

    private final WebDriverWait wait;

    public Wait(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public WebElement untilVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement untilClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public String textOf(WebElement element) {
        try {
            return wait.until(d -> {
                String text = element.getText();
                return (text != null && !text.isEmpty()) ? text : null;
            });
        } catch (TimeoutException e) {
            return element.getText();
        }
    }
}
