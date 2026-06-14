package com.palak.expensemanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/import")
public class ImportController {

    @PostMapping
    public ResponseEntity<String> uploadCsv(@RequestParam("csvFile") MultipartFile csvFile) {
        if (csvFile == null || csvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("CSV file is required.");
        }
        return ResponseEntity.ok("CSV uploaded successfully.");
    }
}
