package Tasks.WebTasks;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test for UPS web App.
 */
public class AppTest 
{
	@Test
	public void webApplicationTask() throws InterruptedException
	{

		WebDriverManager.chromedriver().setup();
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--remote-allow-origins=*");
		WebDriver driver = new ChromeDriver(chromeOptions);

		// Maximize the window
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

		// Opening webpage
		driver.get("https://www.ups.com/us/en/Home.page");

		// Validate Logo exists
		WebElement logoElement = driver.findElement(By.xpath("//img[parent::a[@id='ups-header_logo']]"));
		if (logoElement.isDisplayed()) {
			System.out.println("Logo exists on the web page.");
		} else {
			System.out.println("Logo does not exist on the web page.");
		}
		
		// Scroll to bottom of the page
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy(0,document.body.scrollHeight)", "");

		// Navigate to the footer and count the total number of links
		WebElement footerElement = driver.findElement(By.tagName("footer"));
		List<WebElement> linkElements = footerElement.findElements(By.tagName("a"));
		int linkCount = linkElements.size();
		System.out.println("Total number of links in the footer: " + linkCount);

		// Validate all the footer links are working
		for (WebElement linkElement : linkElements) {
			String linkUrl = linkElement.getAttribute("href");
			validateLink(linkUrl);
		}

		// Click on each link and make sure a different window opens
		String mainWindowHandle = driver.getWindowHandle();
		List<WebElement> javascriptElement = new ArrayList<WebElement>();

		for (WebElement urlElement : linkElements) {
			String url = urlElement.getAttribute("href");

			// Check if the URL contains JavaScript code
			if (url.startsWith("javascript:")) {
				javascriptElement.add(urlElement);
				continue;
			} else {
				// Click on the URL to open in a new window
				String selectLinkOpeninNewTab = Keys.chord(Keys.CONTROL, Keys.RETURN);
				urlElement.sendKeys(selectLinkOpeninNewTab);
				
				// Get all window handles
				Set<String> windowHandles = driver.getWindowHandles();
				
				// Switch to each window handle
				for (String handle : windowHandles) {
					if (!handle.equals(mainWindowHandle)) {
						driver.switchTo().window(handle);							
						captureScreenshots(urlElement,driver);
						System.out.println(driver.getCurrentUrl() + " : Clicked successfully and a new window opened.");
						// Close the new window
						driver.close();
						break;
					}						
				}

				// Switch back to the main window
				driver.switchTo().window(mainWindowHandle);
			}
		}

		for (WebElement urlElement : javascriptElement) {
			String url = urlElement.getAttribute("href");
			String selectLinkOpeninNewTab = Keys.chord(Keys.CONTROL, Keys.RETURN);
			urlElement.sendKeys(selectLinkOpeninNewTab);	
			captureScreenshots(urlElement,driver);
			System.out.println(url + " : Clicked successfully and a new window opened.");
		}

		// Close the browser
		driver.quit();

	}
	// Method to validate a given link URL
	public static void validateLink(String linkUrl) {
		try {
			URL url = new URL(linkUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			connection.connect();

			if (connection.getResponseCode() == 200) {
				System.out.println(linkUrl + " - Link is valid.");
			} else {
				System.out.println(linkUrl + " - Link is broken. Response code: " + connection.getResponseCode());
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println(linkUrl + " - Link is broken or has javascript code. Exception: " + e.getMessage());
		}	
	}
	
	// Method to capture screenshots
		public static void captureScreenshots(WebElement webElement, WebDriver driver) throws InterruptedException {
			// Capture screenshot
		     Thread.sleep(2000);
			 File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			 Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			 File destFile = new File("./target/screenshots/"+timestamp.getTime()+".png");
				try {
					FileUtils.copyFile(srcFile, destFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
}
