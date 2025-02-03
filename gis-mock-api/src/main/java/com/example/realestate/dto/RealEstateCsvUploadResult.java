package com.example.realestate.dto;

import lombok.Data;
import java.util.List;

@Data
public class RealEstateCsvUploadResult {
    private int totalRows;
    private int processedRows;
    private List<RealEstateCsvErrorDto> errors;
}
