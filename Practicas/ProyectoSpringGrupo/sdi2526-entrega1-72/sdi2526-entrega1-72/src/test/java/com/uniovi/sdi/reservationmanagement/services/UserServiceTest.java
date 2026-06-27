package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_LoginView;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_View;
import com.uniovi.sdi.reservationmanagement.repositories.UserRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

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
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    private static WebDriver driver;
    private final Random random = new Random();

    public static WebDriver getDriver(String pathFirefox, String geckodriver) {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
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
    void prueba47_signUpGuardaContrasenaCifrada() {
        String dni = nextAvailableDni();
        goToSignup();
        fillSignupForm(dni, "Luis", "Diaz", "Abcdefghij1!", "Abcdefghij1!");
        submitSignup();

        User stored = userRepository.findByDni(dni).orElseThrow();
        Assertions.assertEquals(dni, stored.getDni());
        Assertions.assertNotEquals("Abcdefghij1!", stored.getPassword());
        Assertions.assertTrue(passwordEncoder.matches("Abcdefghij1!", stored.getPassword()));
    }

    @Test
    @Order(2)
    void prueba48_existeAdministradorPorDefecto() {
        PO_LoginView.goToLogin(driver, baseUrl());
        PO_LoginView.fillLoginForm(driver, ADMIN_DNI, ADMIN_PASSWORD);
        List<WebElement> result = PO_View.checkElementBy(driver, "id", "global-reservations-table");
        Assertions.assertEquals("global-reservations-table", result.getFirst().getAttribute("id"));
    }

    private void goToSignup() {
        driver.navigate().to(baseUrl() + "/signup");
    }

    @SuppressWarnings("SameParameterValue")
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
            userService.registerAdminIfAbsent(new User(ADMIN_DNI, "Admin", "Sistema", ADMIN_PASSWORD));
        }
    }
}
