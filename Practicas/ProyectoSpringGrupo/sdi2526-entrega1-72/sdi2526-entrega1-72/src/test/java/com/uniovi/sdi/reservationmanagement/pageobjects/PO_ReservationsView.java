package com.uniovi.sdi.reservationmanagement.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.List;

public class PO_ReservationsView extends PO_View {

    public static void createReservation(WebDriver driver, LocalDate date, String startHour, String endHour) {
        setDateTime(driver, "startDateTime", date + "T" + startHour);
        setDateTime(driver, "endDateTime", date + "T" + endHour);
        driver.findElement(By.cssSelector("button.primary-btn")).click();
    }

    public static void createRecurringReservation(
            WebDriver driver,
            LocalDate date,
            String startHour,
            String endHour,
            String recurrenceWeeks
    ) {
        setDateTime(driver, "startDateTime", date + "T" + startHour);
        setDateTime(driver, "endDateTime", date + "T" + endHour);
        driver.findElement(By.id("recurringWeekly")).click();
        WebElement weeks = driver.findElement(By.id("recurrenceWeeks"));
        weeks.clear();
        weeks.sendKeys(recurrenceWeeks);
        driver.findElement(By.cssSelector("button.primary-btn")).click();
    }

    public static List<WebElement> getMyReservationRows(WebDriver driver) {
        return driver.findElements(By.cssSelector("#my-reservations-table tbody tr"));
    }

    public static void cancelFirstActiveReservation(WebDriver driver) {
        var buttons = driver.findElements(By.cssSelector("form[action*='/reservas/'][action*='/cancelar'] button"));
        if (buttons.isEmpty()) {
            throw new IllegalStateException("No hay reservas activas para cancelar.");
        }
        buttons.getFirst().click();
    }

    public static void goToMyReservations(WebDriver driver, int port) {
        driver.navigate().to("http://localhost:" + port + "/reservas/mis");
    }

    private static void setDateTime(WebDriver driver, String elementId, String value) {
        var element = driver.findElement(By.id(elementId));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", element, value);
    }
}
