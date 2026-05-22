# form-automation-ui

UI test automation project built with Java 21, Selenium 4, Cucumber 7, JUnit 5 and Allure Report.

---

## Requirements

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.8+ |
| Google Chrome | latest |

> Selenium 4 includes **Selenium Manager** — no manual ChromeDriver setup needed.

---

## Running the tests

**Run all scenarios:**
```bash
mvn clean test
```

**Run a specific tag:**
```bash
mvn clean test -Dcucumber.filter.tags="@registration"
```

Available tags: `@validation`, `@registration`, `@delete`, `@refresh`

---

## Allure Report

### View report online

The report is automatically published to GitHub Pages after every CI run (including failures):

**https://railimarlx7.github.io/form-automation-ui/**

### Generate report locally

```bash
# Run tests + open report in the browser
mvn clean test
mvn allure:serve

# Or generate static HTML in target/site/allure-maven-plugin/
mvn allure:report
```

---

## CI/CD

Every push to a branch with an open PR triggers the pipeline automatically:

- Tests run in headless Chrome on Ubuntu
- Allure report is published to GitHub Pages regardless of test result
- PRs are auto-merged when all tests pass
- Branches are deleted after merge

---

## Project structure

```
src/
├── main/java/br/jireh/
│   ├── core/          # DriverFactory, BasePage
│   ├── domain/        # User model
│   ├── locators/      # @FindBy element declarations
│   ├── actions/       # Browser interactions
│   ├── questions/     # State reads for assertions
│   └── utils/         # Wait helpers
└── test/
    ├── java/br/jireh/
    │   ├── runners/   # JUnit Platform Suite entry point
    │   └── stepdefs/  # Cucumber step definitions and hooks
    └── resources/
        ├── *.feature
        └── junit-platform.properties
```
