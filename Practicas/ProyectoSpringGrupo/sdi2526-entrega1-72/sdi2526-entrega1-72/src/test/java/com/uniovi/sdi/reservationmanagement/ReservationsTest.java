package com.uniovi.sdi.reservationmanagement;

import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_LoginView;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_ReservationsView;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_SpacesView;
import com.uniovi.sdi.reservationmanagement.services.MaintenanceBlockService;
import com.uniovi.sdi.reservationmanagement.services.ReservationService;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import com.uniovi.sdi.reservationmanagement.entities.BlockStatus;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationsTest {

    private static final String USER_DNI = "10000001S";
    private static final String USER_PASSWORD = "Us3r@1-PASSW";
    private static final String USER_THREE_DNI = "10000003V";
    private static final String USER_THREE_PASSWORD = "Us3r@3-PASSW";

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private MaintenanceBlockService maintenanceBlockService;

    @Autowired
    private ReservationService reservationService;

    private WebDriver driver;
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        ensureGeckoDriverPath();
        ensureStandardUser();
        driver = new FirefoxDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(30)
    void prueba30_registrarReservaValida() {
        UserCredentials user = createFreshUser();
        loginAsUser(user.dni(), user.password());

        openCreateReservationFor("Sala Norte");
        LocalDate date = LocalDate.now().plusDays(10);
        PO_ReservationsView.createReservation(driver, date, "12:00", "13:00");

        Assertions.assertFalse(driver.findElements(By.id("btn-check-availability")).isEmpty());
    }

    @Test
    @Order(31)
    void prueba31_registrarReservaInvalidaInicioPosteriorFin() {
        loginAsStandardUser();

        openCreateReservationFor("Sala Norte");
        LocalDate date = LocalDate.now().plusDays(11);
        PO_ReservationsView.createReservation(driver, date, "14:00", "13:00");

        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String normalized = pageSource.toLowerCase();
        Assertions.assertTrue(
                normalized.contains("la fecha de fin debe ser posterior a la de inicio")
                        || normalized.contains("end date/time must be after the start date/time")
        );
    }

    @Test
    @Order(32)
    void prueba32_crearDosReservasSolapadas() {
        UserCredentials user = createFreshUser();
        loginAsUser(user.dni(), user.password());

        openCreateReservationFor("Aula 2.1");
        LocalDate date = LocalDate.now().plusDays(12);
        PO_ReservationsView.createReservation(driver, date, "09:00", "10:00");

        openCreateReservationFor("Aula 2.1");
        PO_ReservationsView.createReservation(driver, date, "09:30", "10:30");

        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String normalized = pageSource.toLowerCase();
        Assertions.assertTrue(
                normalized.contains("el espacio ya tiene una reserva activa en ese horario")
                        || normalized.contains("the space already has an active reservation for that time slot")
        );
    }

    @Test
    @Order(33)
    void prueba33_reservarDentroDeBloque() {
        loginAsStandardUser();

        openCreateReservationFor("Sala Norte");
        LocalDate date = LocalDate.now().plusDays(1);
        maintenanceBlockService.createIfMissing(
                "Sala Norte",
                date.atTime(15, 0),
                date.atTime(16, 0),
                BlockStatus.ACTIVE,
                "Bloqueo QA 33"
        );
        PO_ReservationsView.createReservation(driver, date, "15:00", "16:00");

        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String normalized = pageSource.toLowerCase();
        Assertions.assertTrue(
                normalized.contains("bloqueado por mantenimiento")
                        || normalized.contains("blocked for maintenance")
        );
    }

    @Test
    @Order(34)
    void prueba34_consultarListadoReservasPropias() {
        loginAsStandardUser();

        LocalDate date = LocalDate.now().plusDays(9);
        reservationService.createIfMissing(
                "Sala Norte",
                USER_DNI,
                date.atTime(9, 0),
                date.atTime(10, 0),
                ReservationStatus.ACTIVE,
                "Reserva QA 34"
        );

        driver.navigate().to("http://localhost:" + port + "/reservas/mis");
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> !webDriver.findElements(By.cssSelector("#my-reservations-table tbody tr")).isEmpty()
                        || !webDriver.findElements(By.cssSelector(".top-error")).isEmpty());
        Assertions.assertFalse(driver.findElements(By.cssSelector("#my-reservations-table tbody tr")).isEmpty());
    }

    @Test
    @Order(35)
    void prueba35_filtrarReservasPropiasCanceladas() {
        loginAsStandardUser();

        LocalDate date = LocalDate.now().plusDays(13);
        openCreateReservationFor("Cowork 05");
        PO_ReservationsView.createReservation(driver, date, "10:00", "11:00");

        PO_ReservationsView.goToMyReservations(driver, port);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> !PO_ReservationsView.getMyReservationRows(webDriver).isEmpty());
        PO_ReservationsView.cancelFirstActiveReservation(driver);

        driver.findElement(By.id("status")).sendKeys("CANCELLED");
        driver.findElement(By.cssSelector("#my-reservations-filter-form button.primary-btn")).click();

        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String normalized = pageSource.toLowerCase();
        Assertions.assertTrue(
                normalized.contains("cancelada") || normalized.contains("cancelled")
        );
    }

    @Test
    @Order(36)
    void prueba36_cancelarReservaPropiaYNoOcuparDisponibilidad() {
        loginAsStandardUser();

        LocalDate date = LocalDate.now().plusDays(14);
        openCreateReservationFor("Aula 2.1");
        driver.findElement(By.id("reason")).sendKeys("Reserva QA 36");
        PO_ReservationsView.createReservation(driver, date, "09:00", "10:00");

        PO_ReservationsView.goToMyReservations(driver, port);
        PO_ReservationsView.cancelFirstActiveReservation(driver);

        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openSpaceDetailByName(driver, "Aula 2.1");
        setDate("dateFrom", date.toString());
        setDate("dateTo", date.toString());
        driver.findElement(By.id("btn-check-availability")).click();

        Assertions.assertFalse(PO_SpacesView.occupiedSlotsContainText(driver, "Reserva QA 36"));
    }

    @Test
    @Order(42)
    void prueba42_registrarReservaSemanalRecurrente() {
        UserCredentials user = createFreshUser();
        loginAsUser(user.dni(), user.password());

        openCreateReservationFor("Aula 2.1");
        LocalDate date = LocalDate.now().plusDays(20);
        PO_ReservationsView.createRecurringReservation(driver, date, "09:00", "10:00", "3");

        PO_ReservationsView.goToMyReservations(driver, port);
        List<?> rows = PO_ReservationsView.getMyReservationRows(driver);
        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String page = pageSource.toLowerCase();
        String firstDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toLowerCase();
        String secondDate = date.plusWeeks(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toLowerCase();
        String thirdDate = date.plusWeeks(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toLowerCase();
        Assertions.assertTrue(rows.size() >= 3, "La reserva recurrente debe crear varias entradas");
        Assertions.assertTrue(page.contains(firstDate));
        Assertions.assertTrue(page.contains(secondDate));
        Assertions.assertTrue(page.contains(thirdDate));
    }

    @Test
    @Order(43)
    void prueba43_rechazarReservaRecurrenteConSolape() {
        loginAsUser(USER_THREE_DNI, USER_THREE_PASSWORD);

        PO_ReservationsView.goToMyReservations(driver, port);
        int beforeCount = PO_ReservationsView.getMyReservationRows(driver).size();

        openCreateReservationFor("Sala Norte");
        LocalDate date = LocalDate.now().plusDays(1);
        PO_ReservationsView.createRecurringReservation(driver, date, "15:00", "16:00", "3");

        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String normalized = pageSource.toLowerCase();
        Assertions.assertTrue(
                normalized.contains("bloqueado por mantenimiento")
                        || normalized.contains("blocked for maintenance")
        );

        PO_ReservationsView.goToMyReservations(driver, port);
        int afterCount = PO_ReservationsView.getMyReservationRows(driver).size();
        Assertions.assertEquals(
                beforeCount,
                afterCount,
                "Una reserva recurrente con solape no debe crear ninguna de sus ocurrencias"
        );
    }

    @Test
    @Order(44)
    void prueba44_crearReservasHastaAlcanzarLimite() {
        UserCredentials user = createFreshUser();
        loginAsUser(user.dni(), user.password());

        openCreateReservationFor("Aula 3.2");
        LocalDate date = LocalDate.now().plusDays(40);
        PO_ReservationsView.createRecurringReservation(driver, date, "09:00", "10:00", "4");

        PO_ReservationsView.goToMyReservations(driver, port);
        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String page = pageSource.toLowerCase();
        String lastCreatedDate = date.plusWeeks(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toLowerCase();
        Assertions.assertTrue(
                page.contains(lastCreatedDate),
                "Deben registrarse reservas hasta completar el limite permitido"
        );
    }

    @Test
    @Order(45)
    void prueba45_superarLimiteReservasActivas() {
        UserCredentials user = createFreshUser();
        loginAsUser(user.dni(), user.password());

        openCreateReservationFor("Cowork 12");
        LocalDate date = LocalDate.now().plusDays(200);
        PO_ReservationsView.createRecurringReservation(driver, date, "10:00", "11:00", "6");

        PO_ReservationsView.goToMyReservations(driver, port);
        String pageSource = driver.getPageSource();
        Assertions.assertNotNull(pageSource);
        String normalized = pageSource.toLowerCase();
        Assertions.assertTrue(
                normalized.contains("has alcanzado el límite máximo de 5 reservas activas")
                        || normalized.contains("you have reached the maximum limit of 5 active reservations"),
                "Debe mostrarse el aviso de limite de reservas activas."
        );
    }

    private void loginAsStandardUser() {
        PO_LoginView.goToLogin(driver, "http://localhost:" + port);
        PO_LoginView.fillLoginForm(driver, USER_DNI, USER_PASSWORD);
    }

    private void loginAsUser(String dni, String password) {
        PO_LoginView.goToLogin(driver, "http://localhost:" + port);
        PO_LoginView.fillLoginForm(driver, dni, password);
    }

    private void ensureStandardUser() {
        if (!userService.existsByDni(USER_DNI)) {
            userService.registerStandardUser(new User(USER_DNI, "User", "Selenium", USER_PASSWORD));
        }
    }

    private UserCredentials createFreshUser() {
        String dni = nextAvailableDni();
        String password = "Abcdefghij1!";
        userService.registerStandardUser(new User(dni, "Test", "User", password));
        return new UserCredentials(dni, password);
    }

    private String nextAvailableDni() {
        while (true) {
            int candidate = 10000000 + random.nextInt(90000000);
            String dni = buildDni(candidate);
            if (!userService.existsByDni(dni)) {
                return dni;
            }
        }
    }

    private String buildDni(int number) {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int index = number % 23;
        return String.format("%08d%c", number, letters.charAt(index));
    }

    private record UserCredentials(String dni, String password) {
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

    private void setDate(String elementId, String value) {
        var element = driver.findElement(By.id(elementId));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", element, value);
    }

    private void openCreateReservationFor(String spaceName) {
        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openSpaceDetailByName(driver, spaceName);
        driver.findElement(By.id("btn-create-reservation")).click();
    }

}
