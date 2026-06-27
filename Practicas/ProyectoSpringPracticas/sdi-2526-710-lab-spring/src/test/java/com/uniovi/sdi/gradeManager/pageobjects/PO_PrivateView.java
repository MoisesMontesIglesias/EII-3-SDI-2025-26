package com.uniovi.sdi.gradeManager.pageobjects;
import com.uniovi.sdi.gradeManager.util.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class PO_PrivateView extends PO_NavView {
    static public void fillFormAddMark(WebDriver driver, int userOrder, String descriptionp, String scorep)
    {
        //Esperamos 5 segundo a que carge el DOM porque en algunos equipos falla
        SeleniumUtils.waitSeconds(driver, 5);
        //Seleccionamos el alumnos userOrder
        new Select(driver.findElement(By.id("user"))).selectByIndex(userOrder);
        //Rellenemos el campo de descripción
        WebElement description = driver.findElement(By.name("description"));
        description.clear();
        description.sendKeys(descriptionp);
        WebElement score = driver.findElement(By.name("score"));
        score.click();
        score.clear();
        score.sendKeys(scorep);
        By boton = By.className("btn");
        driver.findElement(boton).click();
    }

    static public void logoutToSignup(WebDriver driver) {
        String loginText = p.getString("signup.message", PO_Properties.getSPANISH());
        PO_PrivateView.clickOption(driver, "logout", "text", loginText);
    }

    static public int getMarksRowCount(WebDriver driver) {
        List<WebElement> marksList = SeleniumUtils.waitLoadElementsBy(driver, "free", "//tbody/tr", getTimeout());
        return marksList.size();
    }

    static public void openMarksMenu(WebDriver driver) {
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//*[@id='myNavbar']/ul[1]/li[2]");
        elements.getFirst().click();
    }

    static public void openAddMark(WebDriver driver) {
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//a[contains(@href, 'mark/add')]");
        elements.getFirst().click();
    }

    static public void openMarksList(WebDriver driver) {
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//a[contains(@href, 'mark/list')]");
        elements.getFirst().click();
    }

    static public void goToLastPage(WebDriver driver) {
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//a[contains(@class, 'page-link')]");
        elements.getLast().click();
    }

    static public void clickMarkDetailsByDescription(WebDriver driver, String description) {
        By enlace = By.xpath("//td[contains(text(), '" + description + "')]/following-sibling::*[2]");
        driver.findElement(enlace).click();
    }

    static public void deleteMarkByDescription(WebDriver driver, String description) {
        List<WebElement> elements = PO_View.checkElementBy(driver, "free", "//td[contains(text(), '" + description + "')]/following-sibling::*/a[contains(@href, 'mark/delete')]");
        elements.getFirst().click();
    }
}