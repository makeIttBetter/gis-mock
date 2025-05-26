package com.example.realestate.dto.upload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A small helper class to keep data for each row:
 * - The converted DTO (RealEstateCsvRecord)
 * - The original row array
 * - The row number
 */
@Getter
@RequiredArgsConstructor
public class RowData {
    private final RealEstateCsvRecord record;
    private final String[] rowArray;
    private final int rowNumber;
}