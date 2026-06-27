package com.uniovi.sdi.reservationmanagement;

import com.uniovi.sdi.reservationmanagement.pageobjects.PO_GlobalReservationsView;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_SpacesView;
import com.uniovi.sdi.reservationmanagement.pageobjects.PO_SpacesTestHelpers;
import com.uniovi.sdi.reservationmanagement.services.ReservationService;
import com.uniovi.sdi.reservationmanagement.services.SpaceService;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import com.uniovi.sdi.reservationmanagement.entities.ReservationStatus;
import com.uniovi.sdi.reservationmanagement.entities.SpaceStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpacesSeleniumFunctionalTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private SpaceService spaceService;

    private WebDriver driver;
    private Path downloadDir;

    @BeforeEach
    void setUp() {
        PO_SpacesTestHelpers.ensureGeckoDriverPath();
        PO_SpacesTestHelpers.ensureStandardUser(userService);
        downloadDir = createDownloadDir();
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("browser.download.dir", downloadDir.toAbsolutePath().toString());
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv,application/csv,application/octet-stream");
        profile.setPreference("pdfjs.disabled", true);
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        driver = new FirefoxDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        cleanupDownloadDir();
    }

    @Test
    @Order(11)
    void prueba11_registrarNuevoEspacioDatosValidos_admin() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        PO_SpacesTestHelpers.openCreateSpaceForm(driver, port);

        String newSpaceName = "Sala Selenium 01";
        PO_SpacesView.submitCreateSpaceForm(
                driver,
                newSpaceName,
                "SALA",
                "Edificio QA - Planta 1",
                "6",
                "Espacio para pruebas funcionales.",
                "ACTIVE"
        );

        PO_SpacesView.checkElementBy(driver, "text", "Listado de espacios registrados");
        Assertions.assertTrue(PO_SpacesView.isSpaceNamePresentInPagedList(driver, newSpaceName));
    }

    @Test
    @Order(12)
    void prueba12_registrarNuevoEspacioDatosInvalidos_nombreVacio_admin() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        PO_SpacesTestHelpers.openCreateSpaceForm(driver, port);
        PO_SpacesTestHelpers.disableHtmlValidation(driver);

        PO_SpacesView.submitCreateSpaceForm(
                driver,
                "   ",
                "SALA",
                "Edificio QA - Planta 2",
                "4",
                "",
                "ACTIVE"
        );

        PO_SpacesView.checkElementBy(driver, "text", "El nombre es obligatorio.");
    }

    @Test
    @Order(13)
    void prueba13_registrarNuevoEspacioDatosInvalidos_capacidadMenorUno_admin() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        PO_SpacesTestHelpers.openCreateSpaceForm(driver, port);
        PO_SpacesTestHelpers.disableHtmlValidation(driver);

        PO_SpacesView.submitCreateSpaceForm(
                driver,
                "Sala Capacidad 0",
                "SALA",
                "Edificio QA - Planta 3",
                "0",
                "Espacio invalido por capacidad.",
                "ACTIVE"
        );

        PO_SpacesView.checkElementBy(driver, "text", "Numero de plazas incorrecto.");
    }

    @Test
    @Order(14)
    void prueba14_registrarEspacioNombreDuplicado_activo_admin() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        PO_SpacesTestHelpers.openCreateSpaceForm(driver, port);
        PO_SpacesTestHelpers.disableHtmlValidation(driver);

        PO_SpacesView.submitCreateSpaceForm(
                driver,
                "Sala Norte",
                "SALA",
                "Edificio A - Planta 1",
                "8",
                "Intento duplicado.",
                "ACTIVE"
        );

        PO_SpacesView.checkElementBy(driver, "text", "Ya existe un espacio activo con ese nombre.");
    }

    @Test
    @Order(15)
    void prueba15_editarEspacioExistenteDatosValidos_admin() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openEditForSpaceName(driver, "Cowork 05");

        String updatedName = "Cowork 05 Editado";
        PO_SpacesView.submitEditSpaceForm(
                driver,
                updatedName,
                "COWORK",
                "Edificio C - Zona cowork",
                "2",
                "Descripcion actualizada.",
                "ACTIVE"
        );

        PO_SpacesView.checkElementBy(driver, "text", updatedName);
    }

    @Test
    @Order(16)
    void prueba16_editarEspacioExistenteDatosInvalidos_capacidadMenorUno_admin() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openEditForSpaceName(driver, "Sala Norte");
        PO_SpacesTestHelpers.disableHtmlValidation(driver);

        PO_SpacesView.submitEditSpaceForm(
                driver,
                "Sala Norte",
                "SALA",
                "Edificio A - Planta 1",
                "0",
                "Intento con capacidad invalida.",
                "ACTIVE"
        );

        PO_SpacesView.checkElementBy(driver, "text", "Numero de plazas incorrecto.");
    }

    @Test
    @Order(17)
    void prueba17_desactivarEspacio_y_verificar_no_reservable() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");

        PO_SpacesView.openEditForSpaceName(driver, "Sala Norte");
        PO_SpacesView.submitEditSpaceForm(
                driver,
                "Sala Norte",
                "SALA",
                "Edificio A - Planta 1",
                "8",
                "Sala amplia con pantalla y pizarra para reuniones.",
                "CANCELLED"
        );

        PO_SpacesTestHelpers.loginAsStandardUserFresh(driver, port);
        driver.navigate().to("http://localhost:" + port + "/espacios/disponibles");
        PO_SpacesView.checkElementBy(driver, "id", "spaces-table");
        Assertions.assertFalse(textContains(driver.getPageSource(), "Sala Norte"));
    }

    @Test
    @Order(18)
    void prueba18_activarEspacio_y_verificar_reservable() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");

        PO_SpacesView.openEditForSpaceName(driver, "Sala Norte");
        PO_SpacesView.submitEditSpaceForm(
                driver,
                "Sala Norte",
                "SALA",
                "Edificio A - Planta 1",
                "8",
                "Sala amplia con pantalla y pizarra para reuniones.",
                "ACTIVE"
        );

        PO_SpacesTestHelpers.loginAsStandardUserFresh(driver, port);
        driver.navigate().to("http://localhost:" + port + "/espacios/disponibles");
        PO_SpacesView.checkElementBy(driver, "id", "spaces-table");
        Assertions.assertTrue(textContains(driver.getPageSource(), "Sala Norte"));
    }

    @Test
    @Order(19)
    void prueba19_crearBloqueoMantenimientoValido() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openDetailForSpaceName(driver, "Sala Norte");

        LocalDate baseDate = LocalDate.now().plusDays(1);
        LocalDateTime start = baseDate.plusDays(2).atTime(10, 0);
        LocalDateTime end = baseDate.plusDays(2).atTime(11, 0);
        String reason = "Bloqueo QA 19";

        PO_SpacesView.openMaintenanceDialog(driver);
        PO_SpacesView.submitMaintenanceBlockForm(
                driver,
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                reason
        );

        Assertions.assertTrue(textContains(driver.getPageSource(), reason));
    }

    @Test
    @Order(20)
    void prueba20_crearBloqueoSolapadoConOtroBloqueo() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openDetailForSpaceName(driver, "Sala Norte");

        LocalDate baseDate = LocalDate.now().plusDays(1);
        LocalDateTime start = baseDate.plusDays(4).atTime(10, 0);
        LocalDateTime end = baseDate.plusDays(4).atTime(12, 0);

        PO_SpacesView.openMaintenanceDialog(driver);
        PO_SpacesView.submitMaintenanceBlockForm(
                driver,
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                "Bloqueo QA 20 base"
        );

        LocalDateTime overlapStart = baseDate.plusDays(4).atTime(11, 0);
        LocalDateTime overlapEnd = baseDate.plusDays(4).atTime(13, 0);

        PO_SpacesView.openMaintenanceDialog(driver);
        PO_SpacesView.submitMaintenanceBlockForm(
                driver,
                overlapStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                overlapEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                "Bloqueo QA 20 solapado"
        );

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> {
                    String source = lowerText(webDriver.getPageSource());
                    return source.contains("se solapa con otro bloqueo activo")
                            || source.contains("solapa")
                            || source.contains("colisiones");
                });
        String pageSource = lowerText(driver.getPageSource());
        boolean hasOverlapError = pageSource.contains("se solapa con otro bloqueo activo")
                || pageSource.contains("solapa");
        boolean hasCollisions = pageSource.contains("colisiones");
        Assertions.assertTrue(
                hasOverlapError || hasCollisions,
                "Debe mostrarse el error de solape con otro bloqueo o el listado de colisiones."
        );
    }

    @Test
    @Order(21)
    void prueba21_crearBloqueoSolapadoConReservaActiva() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        LocalDate reservationDate = LocalDate.now().plusDays(8);
        LocalDateTime reservationStart = reservationDate.atTime(9, 0);
        LocalDateTime reservationEnd = reservationDate.plusDays(1).atTime(18, 0);
        reservationService.createIfMissing(
                "Sala Norte",
                "10000001S",
                reservationStart,
                reservationEnd,
                ReservationStatus.ACTIVE,
                "Reserva QA 21 NUEVA"
        );

        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openDetailForSpaceName(driver, "Sala Norte");
        PO_SpacesView.checkAvailability(driver, reservationDate.toString(), reservationDate.toString());
        String startLabel = reservationStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String endLabel = reservationEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        boolean hasReason = PO_SpacesView.occupiedSlotsContainText(driver, "Reserva QA 21 NUEVA");
        boolean hasGenericLabel = PO_SpacesView.occupiedSlotsContainText(driver, "Reserva activa");
        boolean hasTimeRange = PO_SpacesView.occupiedSlotsContainText(driver, startLabel)
                && PO_SpacesView.occupiedSlotsContainText(driver, endLabel);
        Assertions.assertTrue(
                hasReason || hasGenericLabel || hasTimeRange,
                "Debe existir una reserva activa en el horario antes de crear el bloqueo."
        );

        LocalDateTime overlapStart = reservationDate.atTime(10, 0);
        LocalDateTime overlapEnd = reservationDate.plusDays(1).atTime(12, 0);

        PO_SpacesView.openMaintenanceDialog(driver);
        PO_SpacesView.submitMaintenanceBlockForm(
                driver,
                overlapStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                overlapEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                "Bloqueo QA 21"
        );

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> {
                    String source = lowerText(webDriver.getPageSource());
                    return source.contains("solapa")
                            || source.contains("colisiones")
                            || source.contains("reserva activa")
                            || source.contains("block.overlap.reservation");
                });

        String pageSource = lowerText(driver.getPageSource());
        boolean hasOverlapError = pageSource.contains("solapa") || pageSource.contains("block.overlap.reservation");
        boolean hasCollisionInfo = pageSource.contains("colisiones");
        boolean hasReservationCollision = pageSource.contains("reserva activa") || pageSource.contains("reserva");
        Assertions.assertTrue(textContains(driver.getPageSource(), "Bloquear para mantenimiento"));
        Assertions.assertTrue(
                hasOverlapError || (hasCollisionInfo && hasReservationCollision),
                "Debe mostrarse un error o listado de colisiones con una reserva activa."
        );
    }

    @Test
    @Order(22)
    void prueba22_cancelarBloqueoMantenimiento_y_verificar_que_no_impide_reservas() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openDetailForSpaceName(driver, "Sala Norte");

        LocalDate baseDate = LocalDate.now().plusDays(1);
        LocalDateTime start = baseDate.plusDays(5).atTime(15, 0);
        LocalDateTime end = baseDate.plusDays(5).atTime(16, 0);
        String reason = "Bloqueo QA 22";

        PO_SpacesView.openMaintenanceDialog(driver);
        PO_SpacesView.submitMaintenanceBlockForm(
                driver,
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                reason
        );

        String date = baseDate.plusDays(5).toString();
        PO_SpacesView.checkAvailability(driver, date, date);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> PO_SpacesView.occupiedSlotsContainText(webDriver, reason));
        Assertions.assertTrue(PO_SpacesView.occupiedSlotsContainText(driver, reason));

        PO_SpacesView.cancelMaintenanceBlockByReason(driver, reason);

        PO_SpacesView.checkAvailability(driver, date, date);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> !PO_SpacesView.occupiedSlotsContainText(webDriver, reason));
        Assertions.assertFalse(PO_SpacesView.occupiedSlotsContainText(driver, reason));
    }

    @Test
    @Order(23)
    void prueba23_listadoGlobalConPaginacion() {
        ensurePaginatedReservationsForSalaNorte();
        loginAsAdminToGlobalReservations();

        List<WebElement> rows = PO_GlobalReservationsView.getRows(driver);
        Assertions.assertFalse(rows.isEmpty(), "Debe haber reservas en el listado global");
        Assertions.assertEquals(5, rows.size(), "La primera pagina debe mostrar exactamente 5 reservas");
        Assertions.assertFalse(
                driver.findElements(By.id("next-page")).isEmpty(),
                "El listado global debe indicar que hay al menos dos paginas"
        );
        List<String> firstPageRows = PO_GlobalReservationsView.getRowTexts(driver);
        Assertions.assertTrue(
                firstPageRows.stream().anyMatch(row -> row.contains("Sala Norte")),
                "La primera pagina debe contener reservas reales del sistema"
        );
        Assertions.assertFalse(driver.findElements(By.id("next-page")).isEmpty(), "Debe existir enlace a la pagina siguiente");

        PO_GlobalReservationsView.goToNextPage(driver);
        Assertions.assertTrue(
                matchesPaginationPage(PO_GlobalReservationsView.getPaginationText(driver), 2),
                "La segunda pagina debe ser accesible"
        );
        List<WebElement> secondPageRows = PO_GlobalReservationsView.getRows(driver);
        Assertions.assertFalse(secondPageRows.isEmpty(), "La segunda pagina debe mostrar reservas");
        Assertions.assertTrue(secondPageRows.size() <= 5, "La segunda pagina no debe superar el tamano maximo");
        Assertions.assertNotEquals(
                firstPageRows,
                PO_GlobalReservationsView.getRowTexts(driver),
                "El contenido de la segunda pagina debe ser distinto al de la primera"
        );
    }

    @Test
    @Order(24)
    void prueba24_filtrarListadoGlobalPorEspacio() {
        ensurePaginatedReservationsForSalaNorte();
        loginAsAdminToGlobalReservations();

        PO_GlobalReservationsView.filterBySpaceVisibleText(driver, "Sala Norte");
        List<WebElement> rows = PO_GlobalReservationsView.getRows(driver);
        Assertions.assertFalse(rows.isEmpty(), "El filtro por espacio debe devolver resultados");
        Assertions.assertEquals(5, rows.size(), "La primera pagina filtrada debe mostrar 5 reservas");
        rows.forEach(row -> Assertions.assertTrue(row.getText().contains("Sala Norte")));
        Assertions.assertFalse(
                driver.findElements(By.id("next-page")).isEmpty(),
                "El resultado filtrado debe permitir ir a la pagina siguiente"
        );

        PO_GlobalReservationsView.goToNextPage(driver);
        List<WebElement> secondPageRows = PO_GlobalReservationsView.getRows(driver);
        Assertions.assertFalse(secondPageRows.isEmpty(), "La segunda pagina filtrada debe contener reservas");
        Assertions.assertTrue(secondPageRows.size() <= 5, "La segunda pagina filtrada no debe superar 5 reservas");
        secondPageRows.forEach(row -> Assertions.assertTrue(row.getText().contains("Sala Norte")));
        Assertions.assertTrue(
                matchesPaginationPage(PO_GlobalReservationsView.getPaginationText(driver), 2),
                "La segunda pagina del filtro por espacio debe ser accesible"
        );
    }

    @Test
    @Order(25)
    void prueba25_filtrarListadoGlobalPorRangoFechas() {
        LocalDate rangeStart = ensurePaginatedReservationsForDateRange();
        loginAsAdminToGlobalReservations();

        PO_GlobalReservationsView.filterByDateRange(
                driver,
                rangeStart.toString(),
                rangeStart.plusDays(1).toString()
        );
        List<WebElement> rows = PO_GlobalReservationsView.getRows(driver);
        Assertions.assertEquals(5, rows.size(), "La primera pagina filtrada por fechas debe mostrar 5 reservas");
        List<String> firstPageRows = PO_GlobalReservationsView.getRowTexts(driver);
        Assertions.assertTrue(
                firstPageRows.stream().anyMatch(row -> row.contains("Reserva filtro fecha 1")),
                "La primera pagina debe contener reservas del rango filtrado"
        );
        Assertions.assertTrue(
                matchesPaginationPage(PO_GlobalReservationsView.getPaginationText(driver), 1),
                "El filtro por fechas debe mantener la paginacion"
        );

        PO_GlobalReservationsView.goToNextPage(driver);
        List<WebElement> secondPageRows = PO_GlobalReservationsView.getRows(driver);
        Assertions.assertFalse(secondPageRows.isEmpty(), "La segunda pagina del filtro por fechas debe contener resultados");
        Assertions.assertTrue(secondPageRows.size() <= 5, "La segunda pagina filtrada por fechas no debe superar 5 reservas");
        Assertions.assertTrue(
                matchesPaginationPage(PO_GlobalReservationsView.getPaginationText(driver), 2),
                "Debe poder navegarse por la segunda pagina del filtro por fechas"
        );
        List<String> allFilteredRows = PO_GlobalReservationsView.getRowTexts(driver);
        Assertions.assertTrue(
                allFilteredRows.stream().anyMatch(row -> row.contains("Reserva filtro fecha 6")),
                "La segunda pagina debe contener reservas adicionales del rango filtrado"
        );
        Assertions.assertFalse(
                allFilteredRows.stream().anyMatch(row -> row.contains("Reunion de planificacion")),
                "No deben aparecer reservas fuera del rango solicitado"
        );
    }

    @Test
    @Order(26)
    void prueba26_consultarListadoEspaciosDisponibles() {
        PO_SpacesTestHelpers.loginAsStandardUser(driver, port);
        PO_SpacesView.checkElementBy(driver, "id", "spaces-table");
        List<WebElement> rows = PO_SpacesView.getRows(driver);
        Assertions.assertFalse(rows.isEmpty(), "Debe mostrarse al menos un espacio activo");
    }

    @Test
    @Order(27)
    void prueba27_aplicarFiltroEnListadoEspacios() {
        PO_SpacesTestHelpers.loginAsStandardUser(driver, port);

        PO_SpacesView.applyFilter(driver, "AULA", "20");
        List<WebElement> rows = PO_SpacesView.getRows(driver);

        Assertions.assertFalse(rows.isEmpty(), "Con filtro AULA y capacidad >=20 debe salir al menos un resultado");
        String pageText = safeText(driver.getPageSource());
        Assertions.assertTrue(pageText.contains("Aula 2.1"));
        Assertions.assertFalse(pageText.contains("Sala Norte"));
    }

    @Test
    @Order(28)
    void prueba28_accederDetalleDeEspacioDesdeListado() {
        PO_SpacesTestHelpers.loginAsStandardUser(driver, port);

        PO_SpacesView.openFirstSpaceDetail(driver);
        String pageText = safeText(driver.getPageSource());
        Assertions.assertTrue(pageText.contains("Franjas ocupadas"));
        Assertions.assertTrue(pageText.contains("Consultar disponibilidad"));
    }

    @Test
    @Order(29)
    void prueba29_consultarDisponibilidadConFranjasOcupadas() {
        PO_SpacesTestHelpers.loginAsStandardUser(driver, port);

        driver.navigate().to("http://localhost:" + port + "/spaces");
        PO_SpacesView.openSpaceDetailByName(driver, "Sala Norte");
        LocalDate baseDate = LocalDate.now().plusDays(1);
        PO_SpacesView.checkAvailability(
                driver,
                baseDate.toString(),
                baseDate.plusDays(1).toString()
        );

        List<WebElement> occupiedRows = PO_SpacesView.getOccupiedRows(driver);
        Assertions.assertFalse(occupiedRows.isEmpty(), "Deben aparecer franjas ocupadas por reserva o bloqueo");
        String pageText = lowerText(driver.getPageSource());
        Assertions.assertTrue(pageText.contains("reserva") || pageText.contains("bloqueo"));
     }

    @Test
    @Order(39)
    void prueba39_cambiarIdiomaEnInterfaz() {
        PO_SpacesTestHelpers.loginAsStandardUser(driver, port);

        driver.navigate().to("http://localhost:" + port + "/spaces?lang=en");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.titleContains("Available spaces list"));
        String pageEn = driver.getPageSource();
        Assertions.assertNotNull(pageEn);
        Assertions.assertTrue(textContains(pageEn, "Available spaces list"));

        driver.navigate().to("http://localhost:" + port + "/spaces?lang=es");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.titleContains("Listado de espacios disponibles"));
        String pageEs = driver.getPageSource();
        Assertions.assertNotNull(pageEs);
        Assertions.assertTrue(textContains(pageEs, "Listado de espacios disponibles"));
    }

    @Test
    @Order(46)
    void prueba46_exportarReservasCsvFiltradas() throws Exception {
        ensurePaginatedReservationsForSalaNorte();
        loginAsAdminToGlobalReservations();

        PO_GlobalReservationsView.filterBySpaceVisibleText(driver, "Sala Norte");
        PO_GlobalReservationsView.exportCsv(driver);

        Path csvPath = waitForCsvDownload();
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        Assertions.assertFalse(lines.isEmpty(), "El CSV debe contener cabecera y datos");
        Assertions.assertEquals("Espacio;Usuario;Inicio fecha;Inicio hora;Fin fecha;Fin hora;Estado", lines.getFirst());
        Assertions.assertTrue(lines.size() >= 2, "El CSV debe contener al menos una reserva");
        Assertions.assertTrue(lines.stream().skip(1).allMatch(line -> line.contains("Sala Norte")));
    }

    private void loginAsAdminToGlobalReservations() {
        PO_SpacesTestHelpers.loginAsAdmin(driver, port);
        driver.navigate().to("http://localhost:" + port + "/reservas/listado-global");
    }

    private void ensurePaginatedReservationsForSalaNorte() {
        LocalDate baseDate = LocalDate.now().plusDays(7);
        reservationService.createIfMissing(
                "Sala Norte",
                "10000003V",
                baseDate.atTime(9, 0),
                baseDate.atTime(10, 0),
                ReservationStatus.ACTIVE,
                "Reserva extra 1 Sala Norte"
        );
        reservationService.createIfMissing(
                "Sala Norte",
                "10000004L",
                baseDate.plusDays(1).atTime(9, 0),
                baseDate.plusDays(1).atTime(10, 0),
                ReservationStatus.ACTIVE,
                "Reserva extra 2 Sala Norte"
        );
        reservationService.createIfMissing(
                "Sala Norte",
                "10000005M",
                baseDate.plusDays(2).atTime(9, 0),
                baseDate.plusDays(2).atTime(10, 0),
                ReservationStatus.ACTIVE,
                "Reserva extra 3 Sala Norte"
        );
    }

    private LocalDate ensurePaginatedReservationsForDateRange() {
        LocalDate rangeStart = LocalDate.now().plusDays(30);
        PO_SpacesTestHelpers.ensureStandardUser(userService);
        ensureSpaceExists("Sala Norte", "SALA", "Edificio A - Planta 1", 8);
        ensureSpaceExists("Aula 2.1", "AULA", "Edificio B - Planta 2", 25);
        ensureSpaceExists("Aula 3.2", "AULA", "Edificio B - Planta 3", 30);
        ensureSpaceExists("Cowork 05", "COWORK", "Edificio C - Zona cowork", 1);
        ensureUserExists("10000002Q", "Luis", "Quintana", "Us3r@2-PASSW");
        ensureUserExists("10000003V", "Marta", "Vega", "Us3r@3-PASSW");
        ensureUserExists("10000004L", "Pablo", "Lopez", "Us3r@4-PASSW");
        ensureUserExists("10000005M", "Elena", "Moreno", "Us3r@5-PASSW");
        reservationService.createIfMissing(
                "Sala Norte",
                "10000001S",
                rangeStart.atTime(9, 0),
                rangeStart.atTime(10, 0),
                ReservationStatus.ACTIVE,
                "Reserva filtro fecha 1"
        );
        reservationService.createIfMissing(
                "Aula 2.1",
                "10000002Q",
                rangeStart.atTime(10, 0),
                rangeStart.atTime(11, 0),
                ReservationStatus.ACTIVE,
                "Reserva filtro fecha 2"
        );
        reservationService.createIfMissing(
                "Aula 3.2",
                "10000003V",
                rangeStart.atTime(11, 0),
                rangeStart.atTime(12, 0),
                ReservationStatus.ACTIVE,
                "Reserva filtro fecha 3"
        );
        reservationService.createIfMissing(
                "Cowork 05",
                "10000004L",
                rangeStart.plusDays(1).atTime(9, 0),
                rangeStart.plusDays(1).atTime(10, 0),
                ReservationStatus.ACTIVE,
                "Reserva filtro fecha 4"
        );
        reservationService.createIfMissing(
                "Aula 2.1",
                "10000005M",
                rangeStart.plusDays(1).atTime(10, 0),
                rangeStart.plusDays(1).atTime(11, 0),
                ReservationStatus.ACTIVE,
                "Reserva filtro fecha 5"
        );
        reservationService.createIfMissing(
                "Sala Norte",
                "10000003V",
                rangeStart.plusDays(1).atTime(11, 0),
                rangeStart.plusDays(1).atTime(12, 0),
                ReservationStatus.ACTIVE,
                "Reserva filtro fecha 6"
        );
        return rangeStart;
    }

    private void ensureUserExists(String dni, String name, String lastName, String password) {
        if (!userService.existsByDni(dni)) {
            userService.registerStandardUser(new com.uniovi.sdi.reservationmanagement.entities.User(
                    dni, name, lastName, password
            ));
        }
    }

    private void ensureSpaceExists(String name, String type, String location, int capacity) {
        spaceService.createIfMissing(name, type, location, capacity, SpaceStatus.ACTIVE, null);
    }

    private Path waitForCsvDownload() throws Exception {
        long timeoutMs = 10000;
        long pollMs = 250;
        long waited = 0;
        while (waited < timeoutMs) {
            try (var stream = Files.list(downloadDir)) {
                var files = stream.filter(path -> path.toString().endsWith(".csv")).toList();
                if (!files.isEmpty()) {
                    Path file = files.getFirst();
                    if (Files.size(file) > 0) {
                        return file;
                    }
                }
            }
            Thread.sleep(pollMs);
            waited += pollMs;
        }
        throw new IllegalStateException("No se descargo el CSV en el tiempo esperado");
    }

    private Path createDownloadDir() {
        try {
            return Files.createTempDirectory("csv-downloads-");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo crear el directorio de descargas.", ex);
        }
    }

    private void cleanupDownloadDir() {
        if (downloadDir == null) {
            return;
        }
        try (var paths = Files.walk(downloadDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }
    }

    private String safeText(String value) {
        return String.valueOf(value);
    }

    private boolean textContains(String value, String token) {
        return safeText(value).contains(token);
    }

    private boolean matchesPaginationPage(String paginationText, int page) {
        String normalized = Normalizer.normalize(safeText(paginationText), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.contains("pagina " + page + " de")
                || normalized.contains("page " + page + " of");
    }

    private String lowerText(String value) {
        return String.valueOf(value).toLowerCase(Locale.ROOT);
    }

}
