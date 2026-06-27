package com.uniovi.sdi.reservationmanagement.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private CsvUtils() {
    }

    public static String toCsv(List<String[]> rows) {
        return rows.stream()
                .map(CsvUtils::toCsvLine)
                .collect(Collectors.joining("\n"));
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_FORMAT.format(dateTime);
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : TIME_FORMAT.format(dateTime);
    }

    private static String toCsvLine(String[] values) {
        return java.util.Arrays.stream(values)
                .map(CsvUtils::escape)
                .collect(Collectors.joining(";"));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
