package com.uniovi.sdi.reservationmanagement.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

public class PO_GlobalReservationsView extends PO_View {

    public static List<WebElement> getRows(WebDriver driver) {
        return driver.findElements(By.cssSelector("#global-reservations-table tbody tr.global-reservation-row"));
    }

    public static List<String> getRowTexts(WebDriver driver) {
        return getRows(driver).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public static String getPaginationText(WebDriver driver) {
        return driver.findElement(By.cssSelector(".pagination-bar .hint-text")).getText();
    }

    public static void filterBySpaceVisibleText(WebDriver driver, String spaceName) {
        Select select = new Select(driver.findElement(By.id("spaceId")));
        select.selectByVisibleText(spaceName);
        clickWithFallback(driver, driver.findElement(By.id("btn-filter-global-reservations")));
    }

    public static void filterByDateRange(WebDriver driver, String from, String to) {
        WebElement fromInput = driver.findElement(By.id("dateFromFilter"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                fromInput
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", fromInput, from);

        WebElement toInput = driver.findElement(By.id("dateToFilter"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", toInput, to);

        clickWithFallback(driver, driver.findElement(By.id("btn-filter-global-reservations")));
    }

    public static void goToNextPage(WebDriver driver) {
        clickWithFallback(driver, driver.findElement(By.id("next-page")));
    }

    public static void exportCsv(WebDriver driver) {
        clickWithFallback(driver, driver.findElement(By.id("btn-export-csv")));
    }

    private static void clickWithFallback(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                element
        );
        try {
            element.click();
        } catch (ElementNotInteractableException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}
