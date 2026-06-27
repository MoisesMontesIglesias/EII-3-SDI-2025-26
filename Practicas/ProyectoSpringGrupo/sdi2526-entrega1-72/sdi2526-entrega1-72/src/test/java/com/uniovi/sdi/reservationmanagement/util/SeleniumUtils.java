package com.uniovi.sdi.reservationmanagement.util;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SeleniumUtils {

    public static List<WebElement> waitLoadElementsBy(WebDriver driver, String criterion, String text, int timeout) {
        String searchCriterion = switch (criterion) {
            case "id" -> "//*[contains(@id,'" + text + "')]";
            case "class" -> "//*[contains(@class,'" + text + "')]";
            case "text" -> "//*[contains(text(),'" + text + "')]";
            case "free" -> text;
            default -> "//*[contains(" + criterion + ",'" + text + "')]";
        };
        return waitLoadElementsByXpath(driver, searchCriterion, timeout);
    }

    public static List<WebElement> waitLoadElementsByXpath(WebDriver driver, String xpath, int timeout) {
        WebElement result = (new WebDriverWait(driver, Duration.ofSeconds(timeout)))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        Assertions.assertNotNull(result);
        return driver.findElements(By.xpath(xpath));
    }
}
