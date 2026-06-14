package com.palak.expensemanager.dto;

import com.palak.expensemanager.entity.ImportAnomaly;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportReport {

    private int totalRows;
    private int anomaliesFound;
    private List<ImportAnomaly> anomalies;
}
