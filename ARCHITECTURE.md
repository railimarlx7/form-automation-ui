# UI Test Automation Architecture Guide
> Java 21 · Selenium 4 · Cucumber 7 · JUnit 5 · Allure Report

---

## Stack

| Tool | Version | Role |
|---|---|---|
| Java | 21 | Language |
| Selenium | 4.x | Browser automation |
| Cucumber | 7.x | BDD / Gherkin engine |
| JUnit Platform | 5.x | Test runner |
| Allure | 2.x | Reporting |
| AspectJ | 1.9.x | Allure instrumentation agent |
| Maven | 3.x | Build and dependency management |

---

## Project Structure

```
src/
├── main/java/br/{groupId}/
│   ├── core/
│   │   ├── BasePage.java          # PageFactory initializer
│   │   └── DriverFactory.java     # Browser lifecycle (ThreadLocal)
│   ├── domain/
│   │   └── User.java              # Data model
│   ├── locators/
│   │   └── HomePageLocators.java  # Element declarations (@FindBy)
│   ├── actions/
│   │   └── HomePageActions.java   # User interactions
│   ├── questions/
│   │   └── HomePageQuestions.java # State reads / assertions support
│   └── utils/
│       └── Wait.java              # Explicit wait helpers
│
└── test/java/br/{groupId}/
    ├── runners/
    │   └── RunnerTest.java        # Suite entry point
    └── stepdefs/
        ├── Hooks.java             # @Before / @After / @AfterAll
        └── {Feature}StepDefs.java # Cucumber step bindings

src/test/resources/
├── {feature}.feature              # Gherkin scenarios
└── junit-platform.properties      # Cucumber + Allure config
```

---

## Architecture Layers

The project follows a 3-layer Page Object Model variant:
**Locators → Actions → Questions**

Each layer has a single responsibility. Step definitions only talk to Actions and Questions — never to Locators or the driver directly.

```
StepDefs
  ├── calls → Actions   (do things)
  └── calls → Questions (check things)
                ↓
           Locators      (element map)
                ↓
           WebDriver     (browser)
```

---

## Layer 1 — core

### BasePage

All Locator classes extend `BasePage`. It initializes `@FindBy` annotations via `PageFactory`.

```java
package br.{groupId}.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class BasePage {

    protected final WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
}
```

### DriverFactory

Manages one browser per thread using `ThreadLocal`. Opens the target URL on initialization.

```java
package br.{groupId}.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

public class DriverFactory {

    private static final String PAGE_URL = "https://your-app-url.com";

    private static final ThreadLocal<WebDriver> driverThread =
            ThreadLocal.withInitial(DriverFactory::initDriver);

    public static WebDriver getDriver() {
        return driverThread.get();
    }

    public static void closeDriver() {
        driverThread.get().quit();
        driverThread.remove();
    }

    private static WebDriver initDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.get(PAGE_URL);
        return driver;
    }
}
```

Rules:
- Never call `new ChromeDriver()` outside `DriverFactory`.
- `closeDriver()` must be called once per thread in `@AfterAll`.
- Selenium 4.x includes Selenium Manager — no `WebDriverManager` dependency needed.

---

## Layer 2 — domain

Plain Java objects representing the entities the tests manipulate. No Selenium, no logic.

```java
package br.{groupId}.domain;

public class User {

    private final String username;
    private final String password;
    private final String name;

    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name     = name;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName()     { return name; }
}
```

Rules:
- Use `final` fields — domain objects are immutable.
- One class per entity.

---

## Layer 3 — locators

Declares page elements using `@FindBy`. No interaction logic here.

```java
package br.{groupId}.locators;

import br.{groupId}.core.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePageLocators extends BasePage {

    @FindBy(name = "form_username")
    public WebElement usernameField;

    @FindBy(name = "form_password")
    public WebElement passwordField;

    @FindBy(css = "input.btn-submit")
    public WebElement submitButton;

    @FindBy(css = "table tbody tr:nth-child(2) td:nth-child(3)")
    public WebElement usernameCell;

    public HomePageLocators(WebDriver driver) {
        super(driver);
    }
}
```

Rules:
- One Locators class per page.
- Fields are `public` — Actions and Questions read them directly.
- No methods in this class.
- Prefer `@FindBy(name = ...)` or `@FindBy(id = ...)` over long CSS when available.

---

## Layer 4 — actions

Implements user interactions. Receives the Locators instance and a `Wait` helper.

```java
package br.{groupId}.actions;

import br.{groupId}.domain.User;
import br.{groupId}.locators.HomePageLocators;
import br.{groupId}.utils.Wait;
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
    }

    public void enterUsername(String username) {
        page.usernameField.clear();
        page.usernameField.sendKeys(username);
    }

    public void submit() {
        wait.untilClickable(page.submitButton).click();
    }
}
```

Rules:
- One Actions class per page.
- Each method represents one user gesture (click, fill, clear).
- Compound interactions (e.g. `fillForm`) call the atomic methods.
- Never assert in Actions — that is Questions' job.

---

## Layer 5 — questions

Reads page state to support assertions in step definitions.

```java
package br.{groupId}.questions;

import br.{groupId}.locators.HomePageLocators;
import br.{groupId}.utils.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePageQuestions {

    private final HomePageLocators page;
    private final WebDriver driver;
    private final Wait wait;

    public HomePageQuestions(HomePageLocators page, WebDriver driver) {
        this.page   = page;
        this.driver = driver;
        this.wait   = new Wait(driver);
    }

    public String getUsernameCell() {
        return wait.textOf(page.usernameCell);
    }

    // Use findElements (returns empty list) instead of findElement (throws if absent)
    public boolean isEntryInTable(String username) {
        return driver.findElements(By.cssSelector("table tbody td"))
                .stream()
                .anyMatch(el -> username.equals(el.getText().trim()));
    }
}
```

Rules:
- One Questions class per page.
- Methods return primitives or strings — the assertion lives in the step definition.
- Use `findElements()` (not `findElement()`) when the element may not exist — it returns an empty list instead of throwing `NoSuchElementException`.

---

## Layer 6 — utils / Wait

Wraps `WebDriverWait` to avoid repeating boilerplate explicit waits.

```java
package br.{groupId}.utils;

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
```

Rules:
- Always use explicit waits (`Wait`) on elements that require network or JS rendering.
- The `implicitlyWait` in `DriverFactory` is a baseline — `Wait` handles dynamic content.
- Do not mix implicit and explicit waits on the same element.

---

## Test Layer — stepdefs

### Hooks

```java
package br.{groupId}.stepdefs;

import br.{groupId}.core.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.util.List;
import java.util.stream.Collectors;

public class Hooks {

    // Runs after each scenario — cleans up any data inserted during the scenario
    @After
    public void cleanupUsers() {
        WebDriver driver = DriverFactory.getDriver();
        List<String> deleteUrls = driver
                .findElements(By.cssSelector("table tbody a[href*='del=']"))
                .stream()
                .map(el -> el.getAttribute("href"))
                .collect(Collectors.toList());
        for (String url : deleteUrls) {
            driver.get(url);
        }
    }

    // Runs once after all scenarios — closes the browser
    @AfterAll
    public static void closeBrowser() {
        DriverFactory.closeDriver();
    }
}
```

### Step Definitions

```java
package br.{groupId}.stepdefs;

import br.{groupId}.actions.HomePageActions;
import br.{groupId}.core.DriverFactory;
import br.{groupId}.locators.HomePageLocators;
import br.{groupId}.questions.HomePageQuestions;
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

    @Given("I submit the registration form with username {string}, password {string} and name {string}")
    public void iSubmitTheRegistrationForm(String username, String password, String name) {
        actions.enterUsername(username);
        actions.enterPassword(password);
        actions.enterName(name);
        actions.submit();
    }

    @Then("the table should display username {string}, password {string} and name {string}")
    public void theTableShouldDisplay(String username, String password, String name) {
        assertEquals(username, questions.getUsername());
        assertEquals(password, questions.getPassword());
        assertEquals(name, questions.getName());
    }

    @Then("the table should no longer contain username {string}")
    public void theTableShouldNoLongerContain(String username) {
        assertFalse(questions.isEntryInTable(username));
    }
}
```

Rules:
- `@Before` initializes Actions and Questions using the driver from `DriverFactory`.
- Step definitions call Actions (to act) or Questions (to read state), then assert.
- Assertions use JUnit 5 (`org.junit.jupiter.api.Assertions`).
- Never access `WebDriver` directly in step definitions — go through Actions or Questions.

### Runner

```java
package br.{groupId}.runners;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource(".")
public class RunnerTest {}
```

This class has no logic — it is the JUnit Platform entry point that activates the Cucumber engine. Configuration lives entirely in `junit-platform.properties`.

---

## Gherkin — Feature Files

### Location

```
src/test/resources/{feature-name}.feature
```

### Style: Declarative (not Imperative)

Write **what** the user is doing, not **how**. Avoid browser steps (click, type, select) in Gherkin.

```gherkin
# Wrong — imperative
Given I click on the username field
And I type "jrlima"
And I click on the password field
And I type "123"
And I click the Submit button

# Correct — declarative
Given I submit the registration form with username "jrlima", password "123" and name "Junior"
```

### Tags

| Tag | Purpose |
|---|---|
| `@automation` | Applied to the Feature — selects all scenarios for the test run |
| `@validation` | Scenarios testing validation rules |
| `@registration` | Scenarios testing data creation |
| `@delete` | Scenarios testing data removal |
| `@refresh` | Scenarios testing persistence across page refresh |

### Scenario Isolation

Every scenario must be self-contained. If a scenario needs data to exist, it creates it in its own `Given`. Never rely on a previous scenario having left data behind.

```gherkin
# Wrong — @delete depends on @registration having run first
Scenario: Deleting an entry removes it from the table
  When I delete the entry
  Then the table should no longer contain username "jrlima"

# Correct — @delete creates its own data
Scenario: Deleting an entry removes it from the table
  Given I submit the registration form with username "jrlima2", password "1234" and name "Raimundo"
  When I delete the entry
  Then the table should no longer contain username "jrlima2"
```

### Parameterization with Scenario Outline

```gherkin
@registration
Scenario Outline: Submitting complete data stores the entry in the table
  Given I submit the registration form with username "<username>", password "<password>" and name "<name>"
  Then the table should display username "<username>", password "<password>" and name "<name>"

  Examples:
    | username | password | name     |
    | jrlima   | 123      | Junior   |
    | jrlima2  | 1234     | Raimundo |
```

---

## Configuration

### `src/test/resources/junit-platform.properties`

```properties
cucumber.glue=br.{groupId}.stepdefs
cucumber.filter.tags=@automation
cucumber.plugin=pretty,html:target/report-html.html,json:target/report.json,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm
```

| Property | Purpose |
|---|---|
| `cucumber.glue` | Package where Cucumber scans for step definitions and hooks |
| `cucumber.filter.tags` | Runs only scenarios tagged with this expression |
| `cucumber.plugin` | Output formatters — `pretty` (console), `html`, `json`, Allure |

This file replaces the `@CucumberOptions` annotation that was used with the old JUnit 4 runner.

### `pom.xml` — Key Sections

#### Dependencies

```xml
<properties>
    <selenium.version>4.x.x</selenium.version>
    <cucumber.version>7.x.x</cucumber.version>
    <junit.version>5.x.x</junit.version>
    <junit.platform.version>1.x.x</junit.platform.version>
    <allure.version>2.x.x</allure.version>
    <aspectj.version>1.9.x</aspectj.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>${selenium.version}</version>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java</artifactId>
        <version>${cucumber.version}</version>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-junit-platform-engine</artifactId>
        <version>${cucumber.version}</version>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>${junit.version}</version>
    </dependency>
    <dependency>
        <groupId>org.junit.platform</groupId>
        <artifactId>junit-platform-suite</artifactId>
        <version>${junit.platform.version}</version>
    </dependency>
    <dependency>
        <groupId>org.junit.platform</groupId>
        <artifactId>junit-platform-launcher</artifactId>
        <version>${junit.platform.version}</version>
    </dependency>

    <!-- Allure — exclude gherkin to avoid version conflict with Cucumber -->
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-cucumber7-jvm</artifactId>
        <version>${allure.version}</version>
        <exclusions>
            <exclusion>
                <groupId>io.cucumber</groupId>
                <artifactId>gherkin</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjweaver</artifactId>
        <version>${aspectj.version}</version>
    </dependency>
</dependencies>
```

#### Surefire Plugin

The AspectJ javaagent is required for Allure to capture test lifecycle events.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.x.x</version>
    <configuration>
        <argLine>
            -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"
        </argLine>
        <systemPropertyVariables>
            <allure.results.directory>${project.build.directory}/allure-results</allure.results.directory>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

`${settings.localRepository}` resolves to `~/.m2/repository` — points to the local Maven cache.
`${project.build.directory}` resolves to `target/` — where Surefire runs and Allure reads from.

#### Allure Maven Plugin

```xml
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.14.0</version>
    <configuration>
        <reportVersion>${allure.version}</reportVersion>
    </configuration>
</plugin>
```

---

## Running Tests

```bash
# Run all tests tagged with @automation
mvn clean test

# Run only a specific tag
mvn clean test -Dcucumber.filter.tags="@registration"

# Generate and open Allure report in the browser
mvn allure:serve

# Generate static Allure HTML report in target/site/allure-maven-plugin/
mvn allure:report
```

---

## Adding a New Page

1. **Locators** — create `{Page}Locators.java` in `locators/`, extend `BasePage`, declare `@FindBy` fields.
2. **Actions** — create `{Page}Actions.java` in `actions/`, receive `{Page}Locators` and `WebDriver` in the constructor.
3. **Questions** — create `{Page}Questions.java` in `questions/`, receive `{Page}Locators` and `WebDriver`.
4. **Feature** — create `{page}.feature` in `src/test/resources/`.
5. **StepDefs** — create `{Page}StepDefs.java` in `stepdefs/`, initialize Locators / Actions / Questions in `@Before`.

No changes to `RunnerTest` or `Hooks` are needed unless cleanup logic changes.
