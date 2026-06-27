package com.uniovi.sdi.reservationmanagement;

import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_LoginView;
import com.uniovi.sdi.reservationmanagement.repositories.ReservationRepository;
import com.uniovi.sdi.reservationmanagement.services.ReservationService;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecurityTest {

    private static final String USER_DNI = "10000001S";
    private static final String USER_PASSWORD = "Us3r@1-PASSW";
    private static final String OTHER_USER_DNI = "10000002Q";
    private static final String OTHER_USER_PASSWORD = "Us3r@2-PASSW";

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        ensureGeckoDriverPath();
        ensureStandardUser();
        ensureOtherUser();
        driver = new FirefoxDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(40)
    void prueba40_accesoDenegadoUsuarioEstandarRecursosAdmin() {
        loginAsStandardUser();

        driver.navigate().to("http://localhost:" + port + "/reservas/listado-global");
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> {
                    String pageSource = safeLowerPageSource(webDriver);
                    return pageSource.contains("403")
                            || pageSource.contains("forbidden")
                            || pageSource.contains("access denied");
                });
        String pageSource = safeLowerPageSource(driver);
        Assertions.assertTrue(
                pageSource.contains("403") || pageSource.contains("forbidden") || pageSource.contains("access denied"),
                "Debe mostrarse acceso denegado al usuario estandar."
        );

        driver.navigate().to("http://localhost:" + port + "/espacios/nuevo");
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> {
                    String updatedPageSource = safeLowerPageSource(webDriver);
                    return updatedPageSource.contains("403")
                            || updatedPageSource.contains("forbidden")
                            || updatedPageSource.contains("access denied");
                });
        pageSource = safeLowerPageSource(driver);
        Assertions.assertTrue(
                pageSource.contains("403") || pageSource.contains("forbidden") || pageSource.contains("access denied"),
                "Debe mostrarse acceso denegado al intentar alta de espacios."
        );
    }

    @Test
    @Order(41)
    void prueba41_intentoCancelarReservaAjena() {
        loginAsStandardUser();

        Long otherReservationId = getOtherUserActiveReservationId();
        submitPost("http://localhost:" + port + "/reservas/" + otherReservationId + "/cancelar");

        new WebDriverWait(driver, Duration.ofSeconds(5)).until(webDriver ->
                "complete".equals(((JavascriptExecutor) webDriver).executeScript("return document.readyState"))
        );

        var otherReservation = reservationRepository.findById(otherReservationId).orElseThrow();
        Assertions.assertEquals(ReservationStatus.ACTIVE, otherReservation.getStatus());
    }

    private void loginAsStandardUser() {
        PO_LoginView.goToLogin(driver, "http://localhost:" + port);
        PO_LoginView.fillLoginForm(driver, USER_DNI, USER_PASSWORD);
    }

    private static String safeLowerPageSource(WebDriver driver) {
        String pageSource = driver.getPageSource();
        if (pageSource == null) {
            return "";
        }
        return pageSource.toLowerCase();
    }

    private void ensureStandardUser() {
        if (!userService.existsByDni(USER_DNI)) {
            userService.registerStandardUser(new User(USER_DNI, "User", "Selenium", USER_PASSWORD));
        }
    }

    private void ensureOtherUser() {
        if (!userService.existsByDni(OTHER_USER_DNI)) {
            userService.registerStandardUser(new User(OTHER_USER_DNI, "User", "Other", OTHER_USER_PASSWORD));
        }
    }

    private void ensureGeckoDriverPath() {
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

    private Long getOtherUserActiveReservationId() {
        var activeReservation = reservationRepository.findByUserDniOrderByStartDateTimeDesc(OTHER_USER_DNI).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACTIVE)
                .findFirst();
        if (activeReservation.isPresent()) {
            return activeReservation.get().getId();
        }

        LocalDateTime start = LocalDate.now().plusDays(20).atTime(10, 0);
        LocalDateTime end = start.plusHours(1);
        reservationService.createIfMissing(
                "Sala Norte",
                OTHER_USER_DNI,
                start,
                end,
                ReservationStatus.ACTIVE,
                "Reserva seguridad"
        );

        return reservationRepository.findByUserDniOrderByStartDateTimeDesc(OTHER_USER_DNI).stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se pudo crear una reserva ajena para la prueba."))
                .getId();
    }

    private void submitPost(String actionUrl) {
        ((JavascriptExecutor) driver).executeScript(
                "var form=document.createElement('form');" +
                        "form.method='POST';" +
                        "form.action=arguments[0];" +
                        "document.body.appendChild(form);" +
                        "form.submit();",
                actionUrl
        );
    }
}
