package io.github.denofo.mobilee2e.pages.android;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class AndroidBasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    protected final AndroidDriver driver;
    private final WebDriverWait wait;

    protected AndroidBasePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    protected WebElement visible(By locator) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    protected WebElement visibleWithText(
            By locator,
            String expectedText
    ) {
        return wait.until(ignored -> driver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> expectedText.equals(element.getText()))
                .findFirst()
                .orElse(null));
    }

    protected WebElement clickable(By locator) {
        return wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected boolean isVisible(By locator) {
        try {
            visible(locator);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    protected boolean isVisibleWithText(
            By locator,
            String expectedText
    ) {
        try {
            visibleWithText(locator, expectedText);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
