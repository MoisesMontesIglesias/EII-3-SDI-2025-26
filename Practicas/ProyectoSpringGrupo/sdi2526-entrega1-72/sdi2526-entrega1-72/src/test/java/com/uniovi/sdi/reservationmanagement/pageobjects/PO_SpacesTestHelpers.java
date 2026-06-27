package com.uniovi.sdi.reservationmanagement.pageobjects;

import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;

public final class PO_SpacesTestHelpers {

    private static final String ADMIN_DNI = "12345678Z";
    private static final String ADMIN_PASSWORD = "@Dm1n1str@D0r";
    private static final String USER_DNI = "10000001S";
    private static final String USER_PASSWORD = "Us3r@1-PASSW";

    private PO_SpacesTestHelpers() {
    }

    public static void loginAsStandardUser(WebDriver driver, int port) {
        PO_LoginView.goToLogin(driver, "http://localhost:" + port);
        PO_LoginView.fillLoginForm(driver, USER_DNI, USER_PASSWORD);
    }

    public static void loginAsStandardUserFresh(WebDriver driver, int port) {
        driver.manage().deleteAllCookies();
        loginAsStandardUser(driver, port);
    }

    public static void loginAsAdmin(WebDriver driver, int port) {
        PO_LoginView.goToLogin(driver, "http://localhost:" + port);
        PO_LoginView.fillLoginForm(driver, ADMIN_DNI, ADMIN_PASSWORD);
    }

    public static void openCreateSpaceForm(WebDriver driver, int port) {
        driver.navigate().to("http://localhost:" + port + "/espacios/nuevo");
    }

    public static void disableHtmlValidation(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('form').setAttribute('novalidate','true');"
        );
    }

    public static void ensureStandardUser(UserService userService) {
        if (!userService.existsByDni(USER_DNI)) {
            userService.registerStandardUser(new User(USER_DNI, "User", "Selenium", USER_PASSWORD));
        }
    }

    public static void ensureGeckoDriverPath() {
        String configuredPath = System.getProperty("webdriver.gecko.driver");
        if (configuredPath != null && !configuredPath.isBlank()) {
            return;
        }
        String envPath = System.getenv("GECKODRIVER_PATH");
        if (envPath != null && !envPath.isBlank() && Files.exists(Path.of(envPath))) {
            System.setProperty("webdriver.gecko.driver", envPath);
            return;
        }
        String macDefaultPath = "/opt/homebrew/bin/geckodriver";
        if (Files.exists(Path.of(macDefaultPath))) {
            System.setProperty("webdriver.gecko.driver", macDefaultPath);
        }
    }
}
