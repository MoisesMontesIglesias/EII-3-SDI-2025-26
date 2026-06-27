package com.uniovi.sdi.reservationmanagement.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_LoginView extends PO_View {

    public static void goToLogin(WebDriver driver, String baseUrl) {
        driver.navigate().to(baseUrl + "/login");
    }

    public static void fillLoginForm(WebDriver driver, String dniValue, String passwordValue) {
        WebElement dni = driver.findElement(By.id("dni"));
        dni.click();
        dni.clear();
        dni.sendKeys(dniValue);

        WebElement password = driver.findElement(By.id("password"));
        password.click();
        password.clear();
        password.sendKeys(passwordValue);

        driver.findElement(By.id("btn-login")).click();
    }
}
