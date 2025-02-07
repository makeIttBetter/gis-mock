package com.example.realestate.dto;

import lombok.Data;
import java.util.List;

@Data
public class RealEstateCsvUploadResult {
    private int totalRows;
    private int processedRows;
    // The number of rows that were updated in the database from the CSV file
    private int updatedCount;
    // The number of rows that were created in the database from the CSV file
    private int createdCount;

    // List of errors that occurred during processing
    private List<RealEstateCsvErrorDto> errors;

    // The header row of the CSV file
    private String[] headerRow;
    // The rows that were not saved to the database
    private List<String[]> unsavedRows;

}
