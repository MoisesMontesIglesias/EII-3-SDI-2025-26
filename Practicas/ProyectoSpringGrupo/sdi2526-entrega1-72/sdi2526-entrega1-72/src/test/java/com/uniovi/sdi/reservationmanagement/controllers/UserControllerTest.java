package com.uniovi.sdi.reservationmanagement.controllers;

import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_LoginView;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_View;
import com.uniovi.sdi.reservationmanagement.repositories.UserRepository;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    private static final String PATH_FIREFOX = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
    private static final String GECKODRIVER = System.getenv().getOrDefault("GECKODRIVER_PATH", "");
    private static final int GECKODRIVER_SERVICE_PORT = resolveGeckodriverServicePort();

    private static final String ADMIN_DNI = "12345678Z";
    private static final String ADMIN_PASSWORD = "@Dm1n1str@D0r";

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userServiceReal;

    private static WebDriver driver;
    private final Random random = new Random();

    public static WebDriver getDriver(String pathFirefox, String geckodriver) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        options.addPreference("browser.translations.enable", false);
        options.addPreference("browser.translations.automaticallyPopup", false);

        if (pathFirefox != null && !pathFirefox.isBlank() && Files.exists(Path.of(pathFirefox))) {
            System.setProperty("webdriver.firefox.bin", pathFirefox);
        }

        GeckoDriverService.Builder geckoServiceBuilder = new GeckoDriverService.Builder()
                .usingPort(GECKODRIVER_SERVICE_PORT);
        if (geckodriver != null && !geckodriver.isBlank() && Files.exists(Path.of(geckodriver))) {
            geckoServiceBuilder.usingDriverExecutable(Path.of(geckodriver).toFile());
        }
        return new FirefoxDriver(geckoServiceBuilder.build(), options);
    }

    private static int resolveGeckodriverServicePort() {
        String configuredPort = System.getenv("GECKODRIVER_SERVICE_PORT");
        if (configuredPort == null || configuredPort.isBlank()) {
            return 4444;
        }
        try {
            return Integer.parseInt(configuredPort.trim());
        } catch (NumberFormatException ex) {
            return 4444;
        }
    }

    @BeforeAll
    static void begin() {
        try {
            driver = getDriver(PATH_FIREFOX, GECKODRIVER);
        } catch (Throwable e) {
            Assumptions.assumeTrue(false,
                    "No se puede inicializar Selenium/Firefox en este entorno: " + e.getMessage());
        }
    }

    @AfterAll
    static void end() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @BeforeEach
    void setUp() {
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.navigate().to(baseUrl() + "/login");
        ensureAdminExists();
    }

    @AfterEach
    void tearDown() {
        driver.manage().deleteAllCookies();
    }

    @Test
    @Order(1)
    void prueba1_registroConDatosValidos() {
        String dni = nextAvailableDni();
        goToSignup();
        fillSignupForm(dni, "Ana", "Perez", "Abcdefghij1!", "Abcdefghij1!");
        submitSignup();

        List<WebElement> result = PO_View.checkElementBy(driver, "id", "spaces-filter-form");
        Assertions.assertEquals("spaces-filter-form", result.getFirst().getAttribute("id"));
    }

    @Test
    @Order(2)
    void prueba2_registroConDatosInvalidosConfirmacionIncorrecta() {
        String dni = nextAvailableDni();
        goToSignup();
        fillSignupForm(dni, "Ana", "Perez", "Abcdefghij1!", "Abcdefghij2!");
        submitSignup();

        String expectedError = "Las contraseñas no coinciden. Deben ser exactamente iguales.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedError);
        Assertions.assertEquals(expectedError, result.getFirst().getText());
    }

    @Test
    @Order(3)
    void prueba3_registroConDniDuplicado() {
        String dni = nextAvailableDni();

        goToSignup();
        fillSignupForm(dni, "Ana", "Perez", "Abcdefghij1!", "Abcdefghij1!");
        submitSignup();
        logout();

        goToSignup();
        fillSignupForm(dni, "Ana", "Perez", "Abcdefghij1!", "Abcdefghij1!");
        submitSignup();

        String expectedError = "Esta cuenta ya existe.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedError);
        Assertions.assertEquals(expectedError, result.getFirst().getText());
    }

    @Test
    @Order(4)
    void prueba4_registroConContrasenaInvalida() {
        String dni = nextAvailableDni();
        goToSignup();
        fillSignupForm(dni, "Ana", "Perez", "abc", "abc");
        submitSignup();

        String expectedError = "La contraseña no es válida. Debe tener entre 12 y 20 caracteres, incluir mayúscula, minúscula, número, carácter especial y no contener espacios.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedError);
        Assertions.assertEquals(expectedError, result.getFirst().getText());
    }

    @Test
    @Order(5)
    void prueba5_loginValidoAdministrador() {
        login(ADMIN_DNI, ADMIN_PASSWORD);
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "global-reservations-table");
        Assertions.assertEquals("global-reservations-table", result.getFirst().getAttribute("id"));
    }

    @Test
    @Order(6)
    void prueba6_loginValidoUsuarioEstandar() {
        String dni = nextAvailableDni();
        signUpAndLogout(dni, "Luis", "Diaz", "Abcdefghij1!");

        login(dni, "Abcdefghij1!");
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "spaces-filter-form");
        Assertions.assertEquals("spaces-filter-form", result.getFirst().getAttribute("id"));
    }

    @Test
    @Order(7)
    void prueba7_loginInvalidoDniInexistente() {
        login(nextAvailableDni(), "Abcdefghij1!");
        String expectedText = "No se pudo iniciar sesión: revisa el DNI y la contraseña.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedText);
        Assertions.assertEquals(expectedText, result.getFirst().getText());
    }

    @Test
    @Order(8)
    void prueba8_loginInvalidoContrasenaIncorrecta() {
        String dni = nextAvailableDni();
        signUpAndLogout(dni, "Luis", "Diaz", "Abcdefghij1!");

        login(dni, "Incorrecta123!");
        String expectedText = "No se pudo iniciar sesión: revisa el DNI y la contraseña.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedText);
        Assertions.assertEquals(expectedText, result.getFirst().getText());
    }

    @Test
    @Order(9)
    void prueba9_logoutUsuarioEstandar() {
        String dni = nextAvailableDni();
        signUpAndLogout(dni, "Luis", "Diaz", "Abcdefghij1!");

        goToLogin();
        login(dni, "Abcdefghij1!");
        logout();

        driver.navigate().to(baseUrl() + "/espacios/disponibles");
        String expectedText = "Iniciar sesión";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedText);
        Assertions.assertEquals(expectedText, result.getFirst().getText());
    }

    @Test
    @Order(10)
    void prueba10_logoutAdministrador() {
        login(ADMIN_DNI, ADMIN_PASSWORD);
        logout();

        driver.navigate().to(baseUrl() + "/reservas/listado-global");
        String expectedText = "Iniciar sesión";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedText);
        Assertions.assertEquals(expectedText, result.getFirst().getText());
    }

    @Test
    @Order(37)
    void prueba37_modificarContrasenaDatosValidos() {
        String dni = nextAvailableDni();
        signUpAndLogout(dni, "Maria", "Lopez", "Abcdefghij1!");

        login(dni, "Abcdefghij1!");
        goToChangePassword();
        fillChangePasswordForm("Abcdefghij1!", "NuevaClave1!");

        String expectedText = "Contraseña actualizada.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedText);
        Assertions.assertEquals(expectedText, result.getFirst().getText());

        logout();
        login(dni, "NuevaClave1!");
        List<WebElement> panel = PO_View.checkElementBy(driver, "id", "spaces-filter-form");
        Assertions.assertEquals("spaces-filter-form", panel.getFirst().getAttribute("id"));
    }

    @Test
    @Order(38)
    void prueba38_modificarContrasenaDatosInvalidos() {
        String dni = nextAvailableDni();
        signUpAndLogout(dni, "Mario", "Suarez", "Abcdefghij1!");

        login(dni, "Abcdefghij1!");
        goToChangePassword();
        fillChangePasswordForm("Abcdefghij1!", "");

        String expectedText = "La contraseña nueva es obligatoria.";
        List<WebElement> result = PO_View.checkElementBy(driver, "text", expectedText);
        Assertions.assertEquals(expectedText, result.getFirst().getText());
    }

    private void login(String dni, String password) {
        goToLogin();
        PO_LoginView.fillLoginForm(driver, dni, password);
    }

    @SuppressWarnings("SameParameterValue")
    private void signUpAndLogout(String dni, String name, String lastName, String password) {
        goToSignup();
        fillSignupForm(dni, name, lastName, password, password);
        submitSignup();
        logout();
    }

    private void goToLogin() {
        PO_LoginView.goToLogin(driver, baseUrl());
    }

    private void goToSignup() {
        driver.navigate().to(baseUrl() + "/signup");
    }

    private void fillSignupForm(String dni, String name, String lastName, String password, String confirm) {
        setInputValue("dni", dni);
        setInputValue("name", name);
        setInputValue("lastName", lastName);
        setInputValue("password", password);
        setInputValue("passwordConfirm", confirm);
    }

    private void submitSignup() {
        driver.findElement(By.xpath("//form[contains(@action,'/signup')]//button[@type='submit']")).click();
    }

    private void goToChangePassword() {
        driver.navigate().to(baseUrl() + "/password/change");
    }

    @SuppressWarnings("SameParameterValue")
    private void fillChangePasswordForm(String currentPassword, String newPassword) {
        setInputValue("currentPassword", currentPassword);
        setInputValue("newPassword", newPassword);
        driver.findElement(By.xpath("//form[contains(@action,'/password/change')]//button[@type='submit']")).click();
    }

    private void logout() {
        List<WebElement> buttons = driver.findElements(By.cssSelector("form[action*='/logout'] button"));
        if (buttons.isEmpty()) {
            driver.navigate().to(baseUrl() + "/espacios/disponibles");
            buttons = driver.findElements(By.cssSelector("form[action*='/logout'] button"));
        }
        Assertions.assertFalse(buttons.isEmpty(), "No se encontro el boton de logout.");
        buttons.getFirst().click();
    }

    private void setInputValue(String id, String value) {
        WebElement input = driver.findElement(By.id(id));
        input.click();
        input.clear();
        input.sendKeys(value);
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private static String buildDni(int number) {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int index = number % 23;
        return String.format("%08d%c", number, letters.charAt(index));
    }

    private String nextAvailableDni() {
        while (true) {
            int candidate = 10000000 + random.nextInt(90000000);
            String dni = buildDni(candidate);
            if (userRepository.findByDni(dni).isEmpty()) {
                return dni;
            }
        }
    }

    private void ensureAdminExists() {
        if (userRepository.findByDni(ADMIN_DNI).isEmpty()) {
            userServiceReal.registerAdminIfAbsent(new User(ADMIN_DNI, "Admin", "Sistema", ADMIN_PASSWORD));
        }
    }

}
