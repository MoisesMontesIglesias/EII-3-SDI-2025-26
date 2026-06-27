package com.uniovi.sdi.gradeManager.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_LoginView extends PO_NavView {

    static public void fillLoginForm(WebDriver driver, String dni, String passwordp) {
        // Campo usuario (DNI)
        WebElement username = driver.findElement(By.name("username"));
        username.click();
        username.clear();
        username.sendKeys(dni);

        // Campo password
        WebElement password = driver.findElement(By.name("password"));
        password.click();
        password.clear();
        password.sendKeys(passwordp);

        // Pulsar botón Login
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }

    static public void login(WebDriver driver, String dni, String password) {
        PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
        fillLoginForm(driver, dni, password);
    }

    static public void loginAsStudent(WebDriver driver, String dni, String password) {
        login(driver, dni, password);
        PO_View.checkElementBy(driver, "text", "Notas del usuario");
    }

    static public void loginAsProfessor(WebDriver driver, String dni, String password) {
        login(driver, dni, password);
        PO_View.checkElementBy(driver, "text", dni);
    }
}
