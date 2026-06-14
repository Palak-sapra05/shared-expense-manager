package com.palak.expensemanager.service;

import com.palak.expensemanager.dto.CsvRecord;
import com.palak.expensemanager.dto.ImportReport;
import com.palak.expensemanager.entity.ImportAnomaly;
import com.palak.expensemanager.repository.ImportAnomalyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportService {

    private final CsvImportService csvImportService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final ImportAnomalyRepository importAnomalyRepository;

    public ImportService(
            CsvImportService csvImportService,
            AnomalyDetectionService anomalyDetectionService,
            ImportAnomalyRepository importAnomalyRepository) {
        this.csvImportService = csvImportService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.importAnomalyRepository = importAnomalyRepository;
    }

    public ImportReport importCsv(MultipartFile csvFile) {
        List<CsvRecord> records = csvImportService.parseCsv(csvFile);
        List<ImportAnomaly> anomalies = anomalyDetectionService.detectAnomalies(records);
        anomalies.forEach(importAnomalyRepository::save);
        return new ImportReport(records.size(), anomalies.size(), anomalies);
    }
}
