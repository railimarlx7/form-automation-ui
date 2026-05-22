package br.jireh.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class DriverFactory {

	private static final String PAGE_URL = "http://www.aprendendotestar.com.br/treinar-automacao.php";

	private static final ThreadLocal<WebDriver> driverThread = ThreadLocal.withInitial(DriverFactory::initDriver);

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
