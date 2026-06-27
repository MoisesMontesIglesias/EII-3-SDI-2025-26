package com.uniovi.sdi.reservationmanagement.pageobjects;

import com.uniovi.sdi.reservationmanagement.util.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_View {

    private static final int TIMEOUT = 10;

    public static List<WebElement> checkElementBy(WebDriver driver, String type, String text) {
        return SeleniumUtils.waitLoadElementsBy(driver, type, text, TIMEOUT);
    }
}
