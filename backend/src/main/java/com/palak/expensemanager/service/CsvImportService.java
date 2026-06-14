package com.palak.expensemanager.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.palak.expensemanager.dto.CsvRecord;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvImportService {

    private static final Logger LOGGER = Logger.getLogger(CsvImportService.class.getName());

    public List<CsvRecord> parseCsv(MultipartFile file) {
        List<CsvRecord> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                return records;
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int rowNumber = i + 1;

                try {
                    CsvRecord record = parseRow(row, rowNumber);
                    records.add(record);
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, "Skipping invalid CSV row {0}: {1}", new Object[]{rowNumber, ex.getMessage()});
                }
            }
        } catch (IOException | CsvException e) {
            LOGGER.log(Level.SEVERE, "Failed to parse CSV file", e);
        }

        return records;
    }

    private CsvRecord parseRow(String[] row, int rowNumber) {
        if (row.length < 7) {
            throw new IllegalArgumentException("Row has insufficient columns");
        }

        String description = row[0].trim();
        BigDecimal amount = parseAmount(row[1].trim());
        String currency = row[2].trim();
        String paidBy = row[3].trim();
        String group = row[4].trim();
        String participants = row[5].trim();
        LocalDate expenseDate = parseDate(row[6].trim());

        return new CsvRecord(rowNumber, description, amount, currency, paidBy, group, participants, expenseDate);
    }

    private BigDecimal parseAmount(String value) {
        if (value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value.replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }

    private LocalDate parseDate(String value) {
        if (value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
                DateTimeFormatter.ofPattern("MM-dd-yyyy"),
                DateTimeFormatter.ofPattern("MMM-dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        if (value.matches("[A-Za-z]{3}-\\d{1,2}")) {
            String currentYearValue = value + "-" + LocalDate.now().getYear();
            try {
                return LocalDate.parse(currentYearValue, DateTimeFormatter.ofPattern("MMM-d-yyyy"));
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Ambiguous date format");
    }
}
