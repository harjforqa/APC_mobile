package TestCases;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class PushNotificationTests {
    
    WebDriver driver;

    //get current working directory
    static File currentDir = new File(System.getProperty("user.dir"));
    String appPath;

    int timeOut = 15; //driver timeout in seconds

    //Appium stuff
    DesiredCapabilities capabilities;
    final int port = 4723;
    final String AppiumIP = "0.0.0.0";
    final String AppiumHostPort = "http://" + AppiumIP + ":" + port + "/wd/hub";

    //AUT variables
    String yourAPIKey, titlePushNotif, contentPushNotif;

    //boolean flag
    boolean bool;

    @BeforeSuite
    public synchronized void setupDriver() throws IOException {
        capabilities = new DesiredCapabilities();

        //get apk path
        appPath = chooseBuild();
        File app = new File(appPath);

        //load required Appium capabilities
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, false);
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "emulator-5554");
        capabilities.setCapability("os_version", 12.0);
        capabilities.setCapability("unicodeKeyboard", true);
        capabilities.setCapability("resetKeyboard", true);
        capabilities.setCapability(MobileCapabilityType.APP, app.getAbsolutePath());
        capabilities.setCapability("adbExecTimeout", 40000);
        capabilities.setCapability("newCommandTimeout", 40);
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
        driver = new AndroidDriver<>(new URL(AppiumHostPort), capabilities);
    }

    //quite the driver at the end of execution
    @AfterSuite
    public void tearDown() {
        driver.quit();
    }

    @Test(priority = 1)
    public void TC_001_SendPushNotif() {
        //wait for loading spinner to disappear
        waitForInvisibility(By.id("net.xdroid.pn:id/progress_text"));

        //wait for home screen to appear
        waitForVisibility(By.id("net.xdroid.pn:id/api_key"));

        //get and store API KEY/TOKEN
        Assert.assertNotNull(getAPItoken());

        //get and store API URL
        Assert.assertNotNull(getAPIurl());

        //get notification title to be sent
        MobileElement titlePushNotifObj = (MobileElement) driver.findElement(By.id("net.xdroid.pn:id/title"));
        titlePushNotif = titlePushNotifObj.getText();
        System.out.println("push notification title is>" + titlePushNotif);

        //get notification content to be sent
        MobileElement contentPushNotifObj = (MobileElement) driver.findElement(By.id("net.xdroid.pn:id/contents"));
        contentPushNotif = contentPushNotifObj.getText();
        System.out.println("push notification content is>" + contentPushNotif);

        //click submit button to trigger the push notification
        driver.findElement(By.id("net.xdroid.pn:id/btn_send")).click();
    }

    @Test(priority = 2)
    public void TC_002_VerifyPushNotifDetails() throws InterruptedException {
        //Match the content of push notification w.r.t. Test Submission Form
        verifyPushNotificationTitle(titlePushNotif);
        verifyPushNotifContent(contentPushNotif);
    }

    @Test(priority = 3)
    public void TC_003_Refresh_Token() throws InterruptedException {
        //get and store current API key/token
        yourAPIKey = getAPItoken();

        //change API key/token by clicking refresh button
        driver.findElement(By.id("net.xdroid.pn:id/refresh")).click();

        //click OK to confirm
        waitForVisibility(By.id("android:id/message"));
        MobileElement successMessageObj = (MobileElement) driver.findElement(By.id("android:id/message"));
        Assert.assertEquals(successMessageObj.getText(), "Are you sure you can't use your existing old API Key?");
        driver.findElement(By.id("android:id/button1")).click();
        Thread.sleep(2000); //intentional sleep to refresh the token value

        //match the previous and new token values
        Assert.assertNotNull(yourAPIKey, getAPItoken());
    }

    //method to get apk path from current working directory
    public String chooseBuild() {
        appPath = (currentDir + "/src/main/resources/Push_Notification_API.apk").replace("/", "\\");
        System.out.println("appPath>" + appPath);
        return appPath;
    }

    //selenium wrapper to wait for an element to be visible.
    public void waitForVisibility(By targetElement) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeOut);
            wait.until(ExpectedConditions.visibilityOfElementLocated(targetElement));
        } catch (TimeoutException e) {
            System.out.println("Element is not visible: " + targetElement);
            throw e;
        }
    }

    //selenium wrapper to wait for an element to be invisible.
    public void waitForInvisibility(By targetElement) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeOut);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(targetElement));
        } catch (TimeoutException e) {
            System.out.println("Element is still visible: " + targetElement);
            System.out.println(e.getMessage());
            throw e;
        }
    }

    //return API_token
    public String getAPItoken() {
        MobileElement APIKey = (MobileElement) driver.findElement(By.id("net.xdroid.pn:id/api_key"));
        System.out.println("API Key/token is>" + APIKey.getText());
        return APIKey.getText();
    }

    //return API URL
    public String getAPIurl() {
        MobileElement APIURLObj = (MobileElement) driver.findElement(By.id("net.xdroid.pn:id/api_sample"));
        System.out.println("API URL is>" + APIURLObj.getText());
        return APIURLObj.getText();
    }

    //open notification bar
    public void openNotifications() {
        ((AndroidDriver<?>) driver).openNotifications();
    }

    //close notification bar
    public void closeNotifications() {
        ((AndroidDriver<?>) driver).pressKey(new KeyEvent().withKey(AndroidKey.BACK));
    }


    //verify push notification title
    public void verifyPushNotificationTitle(String pushNotifTitle) throws InterruptedException {
        bool = false;
        openNotifications();
        Thread.sleep(15000); //due to performance issue of the app, this is intentional sleep to handle delay in receiving push notification
        waitForVisibility(By.id("android:id/title"));
        List<WebElement> TitleList = driver.findElements(By.id("android:id/title"));
        //traverse and match all the push notifications available in the action center.
        for (WebElement webElement : TitleList) {
            String textPN = webElement.getText();
            System.out.println("notification title is: " + textPN);
            if (textPN.equals(pushNotifTitle)) {
                bool = true;
                closeNotifications();
                break;
            }
        }
        //fail the test case if notification is not received.
        if (!bool) {
            closeNotifications();
            assert false;
        }
    }

    public void verifyPushNotifContent(String pushNotifContent) {
        openNotifications();
        waitForVisibility(By.id("android:id/text"));
        List<WebElement> ContentList = driver.findElements(By.id("android:id/text"));
        for (int i = 0; i < ContentList.size(); i++) {
            System.out.println("'Description' under notification bar is: " + ContentList.get(i).getText());
            if (ContentList.get(i).getText().equals(pushNotifContent)) {
                bool = true;
                break;
            } else {
                bool = false;
                System.out.println(pushNotifContent + " : content is not found at " + i + " locator");
            }
        }
        if (bool) {
            System.out.println(pushNotifContent + " : Content found under push notifications bar.");
        } else {
            System.out.println(pushNotifContent + " : Content is not found under push notifications bar.");

            assert false;
        }
        closeNotifications();
    }

}
