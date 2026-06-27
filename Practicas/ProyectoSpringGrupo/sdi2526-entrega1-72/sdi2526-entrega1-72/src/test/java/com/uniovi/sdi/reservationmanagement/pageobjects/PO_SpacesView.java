package com.uniovi.sdi.reservationmanagement.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class PO_SpacesView extends PO_View {

    public static List<WebElement> getRows(WebDriver driver) {
        return driver.findElements(By.cssSelector("#spaces-table tbody tr.space-row"));
    }

    public static boolean isSpaceNamePresentInPagedList(WebDriver driver, String spaceName) {
        if (spaceName == null) {
            return false;
        }
        while (true) {
            String pageSource = driver.getPageSource();
            if (pageSource != null && pageSource.contains(spaceName)) {
                return true;
            }
            List<WebElement> nextButtons = driver.findElements(By.id("next-page"));
            if (nextButtons.isEmpty()) {
                return false;
            }
            nextButtons.getFirst().click();
        }
    }



    public static void openFirstSpaceDetail(WebDriver driver) {
        driver.findElement(By.cssSelector("a.space-detail-link")).click();
    }

    public static void openDetailForSpaceName(WebDriver driver, String spaceName) {
        openSpaceActionByNameOrThrow(
                driver,
                spaceName,
                By.cssSelector("a.space-detail-link"),
                "No se encontro el espacio con nombre: " + spaceName
        );
    }

    public static void openEditForSpaceName(WebDriver driver, String spaceName) {
        openSpaceActionByNameOrThrow(
                driver,
                spaceName,
                By.cssSelector("a.text-link[href*='/espacios/'][href$='/editar']"),
                "No se encontro el espacio con nombre: " + spaceName
        );
    }


    public static void openSpaceDetailByName(WebDriver driver, String name) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre del espacio no puede ser nulo.");
        }
        openSpaceActionByNameOrThrow(
                driver,
                name,
                By.cssSelector("a.space-detail-link"),
                "No se encontro el espacio: " + name
        );
    }

    public static List<WebElement> getOccupiedRows(WebDriver driver) {
        return driver.findElements(By.cssSelector("#occupied-slots-table tbody tr.occupied-slot-row"));
    }

    public static void applyFilter(WebDriver driver, String type, String minCapacity) {
        WebElement typeInput = driver.findElement(By.id("type"));
        Select typeSelect = new Select(typeInput);
        typeSelect.selectByValue(type);

        WebElement capacityInput = driver.findElement(By.id("minCapacity"));
        capacityInput.click();
        capacityInput.clear();
        capacityInput.sendKeys(minCapacity);

        WebElement submit = driver.findElement(By.id("btn-filter-spaces"));
        try {
            submit.click();
        } catch (ElementNotInteractableException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        }
    }

    public static void checkAvailability(WebDriver driver, String dateFrom, String dateTo) {
        WebElement from = driver.findElement(By.id("dateFrom"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                from
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", from, dateFrom);

        WebElement to = driver.findElement(By.id("dateTo"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", to, dateTo);

        WebElement submit = driver.findElement(By.id("btn-check-availability"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                submit
        );
        try {
            submit.click();
        } catch (ElementNotInteractableException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        }
    }

    public static boolean occupiedSlotsContainText(WebDriver driver, String text) {
        List<WebElement> tables = driver.findElements(By.id("occupied-slots-table"));
        if (tables.isEmpty()) {
            return false;
        }
        return tables.getFirst().getText().contains(text);
    }

    public static void submitCreateSpaceForm(
            WebDriver driver,
            String name,
            String type,
            String location,
            String capacity,
            String description,
            String status
    ) {
        fillSpaceForm(driver, name, type, location, capacity, description, status);
        submitSpaceForm(driver);
    }

    public static void submitEditSpaceForm(
            WebDriver driver,
            String name,
            String type,
            String location,
            String capacity,
            String description,
            String status
    ) {
        fillSpaceForm(driver, name, type, location, capacity, description, status);
        submitSpaceForm(driver);
    }

    private static void fillSpaceForm(
            WebDriver driver,
            String name,
            String type,
            String location,
            String capacity,
            String description,
            String status
    ) {
        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.click();
        nameInput.clear();
        nameInput.sendKeys(name);

        Select typeSelect;
        List<WebElement> typeInputs = driver.findElements(By.id("spaceType"));
        if (!typeInputs.isEmpty()) {
            typeSelect = new Select(typeInputs.getFirst());
        } else {
            typeSelect = new Select(driver.findElement(By.id("type")));
        }
        typeSelect.selectByValue(type);

        WebElement locationInput = driver.findElement(By.id("location"));
        locationInput.click();
        locationInput.clear();
        locationInput.sendKeys(location);

        WebElement capacityInput = driver.findElement(By.id("capacity"));
        capacityInput.click();
        capacityInput.clear();
        capacityInput.sendKeys(capacity);

        WebElement descriptionInput = driver.findElement(By.id("description"));
        descriptionInput.click();
        descriptionInput.clear();
        if (description != null) {
            descriptionInput.sendKeys(description);
        }

        Select statusSelect = new Select(driver.findElement(By.id("status")));
        statusSelect.selectByValue(status);
    }

    private static void submitSpaceForm(WebDriver driver) {
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    public static void openMaintenanceDialog(WebDriver driver) {
        WebElement button = driver.findElement(By.id("open-maintenance-dialog"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                button
        );
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(button));
        try {
            button.click();
        } catch (ElementNotInteractableException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }
    }

    public static void submitMaintenanceBlockForm(
            WebDriver driver,
            String startDateTime,
            String endDateTime,
            String reason
    ) {
        WebElement startInput = driver.findElement(By.id("startDateTime"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                startInput
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", startInput, startDateTime);

        WebElement endInput = driver.findElement(By.id("endDateTime"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", endInput, endDateTime);

        WebElement reasonInput = driver.findElement(By.id("reason"));
        reasonInput.clear();
        reasonInput.sendKeys(reason);

        WebElement submit = driver.findElement(By.cssSelector("dialog form button[type='submit']"));
        try {
            submit.click();
        } catch (ElementNotInteractableException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        }
    }

    public static void cancelMaintenanceBlockByReason(WebDriver driver, String reason) {
        List<WebElement> forms = driver.findElements(
                By.cssSelector("form[action*='/bloqueos/'][action$='/cancelar']")
        );
        for (WebElement form : forms) {
            WebElement row = form.findElement(By.xpath("./ancestor::tr"));
            if (row.getText().contains(reason)) {
                WebElement submit = form.findElement(By.cssSelector("button[type='submit']"));
                try {
                    submit.click();
                } catch (ElementNotInteractableException ex) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
                }
                return;
            }
        }
        throw new IllegalStateException("No se encontro el bloqueo con descripcion: " + reason);
    }

    private static void openSpaceActionByNameOrThrow(
            WebDriver driver,
            String spaceName,
            By actionLocator,
            String errorMessage
    ) {
        while (true) {
            List<WebElement> rows = getRows(driver);
            for (WebElement row : rows) {
                String rowText = row.getText();
                if (rowText.contains(spaceName)) {
                    WebElement action = row.findElement(actionLocator);
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                            action
                    );
                    try {
                        action.click();
                    } catch (ElementNotInteractableException ex) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", action);
                    }
                    return;
                }
            }
            List<WebElement> nextButtons = driver.findElements(By.id("next-page"));
            if (nextButtons.isEmpty()) {
                throw new IllegalStateException(errorMessage);
            }
            WebElement next = nextButtons.getFirst();
            try {
                next.click();
            } catch (ElementNotInteractableException ex) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", next);
            }
        }
    }
}
