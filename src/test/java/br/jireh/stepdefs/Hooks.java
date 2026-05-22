package br.jireh.stepdefs;

import br.jireh.core.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class Hooks {

    @After
    public void cleanupUsers() {
        WebDriver driver = DriverFactory.getDriver();
        List<String> deleteUrls = driver
                .findElements(By.cssSelector("div > table tbody a[href*='del=']"))
                .stream()
                .map(el -> el.getAttribute("href"))
                .collect(Collectors.toList());

        for (String url : deleteUrls) {
            driver.get(url);
        }
    }

    @AfterAll
    public static void closeBrowser() {
        DriverFactory.closeDriver();
    }
}
