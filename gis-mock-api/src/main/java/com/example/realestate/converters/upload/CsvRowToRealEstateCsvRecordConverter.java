package com.example.realestate.converters.upload;

import com.example.realestate.converters.Converter;
import com.example.realestate.dto.upload.RealEstateCsvRecord;
import com.example.realestate.util.CsvParsingUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Converts a raw CSV row (String[]) to a RealEstateCsvRecord,
 * using the headerMap to locate each column by name.
 */
@Slf4j
public class CsvRowToRealEstateCsvRecordConverter implements Converter<String[], RealEstateCsvRecord> {

    private final Map<String, Integer> headerMap;

    public CsvRowToRealEstateCsvRecordConverter(Map<String, Integer> headerMap) {
        this.headerMap = headerMap;
    }

    @Override
    public RealEstateCsvRecord convert(String[] row) {
        // Grab each cell by header name, then parse as needed
        RealEstateCsvRecord record = new RealEstateCsvRecord();

        record.setMlsNumber(getValue(row, "mls#"));
        if (record.getMlsNumber() == null) {
            // also handle column named "mlsnumber" if that’s how it appears
            record.setMlsNumber(getValue(row, "mlsnumber"));
        }

        record.setTaxId(getValue(row, "tax id"));
        record.setAddress(getValue(row, "address"));
        record.setCity(getValue(row, "city"));
        record.setState(getValue(row, "state"));
        record.setZip(getValue(row, "zip"));
        record.setStatus(getValue(row, "status"));
        record.setShortSale(getValue(row, "short sale"));
        record.setTimeUC(getValue(row, "time uc"));
        record.setDom(CsvParsingUtils.parseInteger(getValue(row, "dom")));
        record.setSoldDate(CsvParsingUtils.parseDate(getValue(row, "sold date")));
        record.setSoldTerms(getValue(row, "sold terms"));
        record.setSoldPrice(getValue(row, "sold price"));
        record.setSoldConcessions(getValue(row, "sold concessions"));
        record.setListPrice(getValue(row, "list price"));
        record.setOriginalListPrice(getValue(row, "original list price"));
        record.setUnderContractDate(CsvParsingUtils.parseDate(getValue(row, "under contract date")));
        record.setEntryDate(CsvParsingUtils.parseDate(getValue(row, "entry date")));
        record.setEffectiveDateOfListingAgreement(CsvParsingUtils.parseDate(
                getValue(row, "effective date of the listing agreement")));
        record.setStatusChangeDate(CsvParsingUtils.parseDate(getValue(row, "status change date")));
        record.setOffMarketDate(CsvParsingUtils.parseDate(getValue(row, "off market date")));
        record.setReinstatedDate(CsvParsingUtils.parseDate(getValue(row, "reinstated date")));
        record.setCancelDate(CsvParsingUtils.parseDate(getValue(row, "cancel date")));
        record.setOfferUnder3rdPartyReview(getValue(row, "offer under 3rd party review"));
        record.setPriceIncreaseDate(CsvParsingUtils.parseDate(getValue(row, "price increase date")));
        record.setPriceIncreaseDaysBack(CsvParsingUtils.parseInteger(getValue(row, "price increase days back")));
        record.setPriceReductionDate(CsvParsingUtils.parseDate(getValue(row, "price reduction date")));
        record.setPriceReductionDaysBack(CsvParsingUtils.parseInteger(getValue(row, "price reduction days back")));
        record.setBackupStatusDate(CsvParsingUtils.parseDate(getValue(row, "backup status date")));
        record.setWithdrawalDate(CsvParsingUtils.parseDate(getValue(row, "withdrawal date")));
        record.setExpireDate(CsvParsingUtils.parseDate(getValue(row, "expire date")));
        record.setAcres(CsvParsingUtils.parseBigDecimal(getValue(row, "acres")));
        record.setPropertyType(getValue(row, "property type"));
        record.setStyle(getValue(row, "style"));
        record.setYearBuilt(CsvParsingUtils.parseInteger(getValue(row, "year built")));
        record.setGrossLivingAreaGla(CsvParsingUtils.parseInteger(getValue(row, "gross living area (gla)")));
        record.setTotalSquareFeet(CsvParsingUtils.parseInteger(getValue(row, "total square feet")));
        record.setTotalBedrooms(CsvParsingUtils.parseInteger(getValue(row, "total bedrooms")));
        record.setTotalBathrooms(CsvParsingUtils.parseInteger(getValue(row, "total bathrooms")));
        record.setTotalFullBathrooms(CsvParsingUtils.parseInteger(getValue(row, "total full bathrooms")));
        record.setTotalThreeQuarterBathrooms(CsvParsingUtils.parseInteger(getValue(row, "total three-quarter bathrooms")));
        record.setTotalHalfBathrooms(CsvParsingUtils.parseInteger(getValue(row, "total half bathrooms")));
        record.setTotalKitchens(CsvParsingUtils.parseInteger(getValue(row, "total kitchens")));
        record.setBasementSquareFeet(CsvParsingUtils.parseInteger(getValue(row, "basement square feet")));
        record.setBasementFinished(CsvParsingUtils.parseInteger(getValue(row, "basement finished")));
        record.setBasementBedrooms(CsvParsingUtils.parseInteger(getValue(row, "basement bedrooms")));
        record.setBasementFullBathrooms(CsvParsingUtils.parseInteger(getValue(row, "basement full bathrooms")));
        record.setBasementThreeQuarterBathrooms(
                CsvParsingUtils.parseInteger(getValue(row, "basement three-quarter bathrooms")));
        record.setBasementHalfBathrooms(CsvParsingUtils.parseInteger(getValue(row, "basement half bathrooms")));
        record.setBasement(getValue(row, "basement"));
        record.setHeating(getValue(row, "heating"));
        record.setAirConditioning(getValue(row, "air conditioning"));
        record.setGarageCapacity(CsvParsingUtils.parseInteger(getValue(row, "garage capacity")));
        record.setCarportCapacity(CsvParsingUtils.parseInteger(getValue(row, "carport capacity")));
        record.setGarageParking(getValue(row, "garage/parking"));
        record.setDecks(CsvParsingUtils.parseInteger(getValue(row, "decks")));
        record.setSolar(CsvParsingUtils.parseBoolean(getValue(row, "solar?")));
        record.setSolarOwnership(getValue(row, "solar ownership"));
        record.setTotalFireplaces(CsvParsingUtils.parseInteger(getValue(row, "total fireplaces")));
        record.setLandscape(getValue(row, "landscape"));
        record.setPoolAvailable(CsvParsingUtils.parseBoolean(getValue(row, "pool?")));
        record.setPoolDetails(getValue(row, "pool"));
        record.setHoaFee(getValue(row, "hoa fee"));
        record.setHoaAmenities(getValue(row, "hoa amenities"));
        record.setHoaRemarks(getValue(row, "hoa remarks"));
        record.setMainFloorSquareFeet(CsvParsingUtils.parseInteger(getValue(row, "main floor square feet")));
        record.setSecondFloorSquareFeet(CsvParsingUtils.parseInteger(getValue(row, "second floor square feet")));
        record.setThirdFloorSquareFeet(CsvParsingUtils.parseInteger(getValue(row, "third floor square feet")));
        record.setFourthFloorSquareFeet(CsvParsingUtils.parseInteger(getValue(row, "fourth floor square feet")));
        record.setExteriorFeatures(getValue(row, "exterior features"));
        record.setFloor(getValue(row, "floor"));
        record.setCounty(getValue(row, "county"));
        record.setInteriorFeatures(getValue(row, "interior features"));
        record.setPublicRemarks(getValue(row, "public remarks"));
        record.setBacCompensation(getValue(row, "bac compensation"));
        record.setMlsLink(getValue(row, "mls link"));
        record.setQuality(getValue(row, "quality"));
        record.setCondition(getValue(row, "condition"));
        record.setLocationPositive(getValue(row, "location +"));
        record.setLocationNegative(getValue(row, "location -"));
        record.setViewPositive(getValue(row, "view +"));
        record.setViewNegative(getValue(row, "view -"));
        record.setAdu(CsvParsingUtils.parseBoolean(getValue(row, "adu")));
        record.setRemodUpdate(CsvParsingUtils.parseBoolean(getValue(row, "remod / update")));
        record.setOutbuilding(CsvParsingUtils.parseBoolean(getValue(row, "outbuilding")));
        record.setFullAddress(getValue(row, "full address"));
        record.setLatitude(CsvParsingUtils.parseBigDecimal(getValue(row, "latitude")));
        record.setLongitude(CsvParsingUtils.parseBigDecimal(getValue(row, "longitude")));
        record.setLegal(getValue(row, "legal"));
        record.setVerified(CsvParsingUtils.parseBoolean(getValue(row, "verified")));
        record.setDaysBack(CsvParsingUtils.parseInteger(getValue(row, "days back")));
        record.setLegalMatchToTax(CsvParsingUtils.parseBoolean(getValue(row, "legalmatchtotax")));
        record.setCountyRecordLink(getValue(row, "countyrecordlink"));
        record.setHtmlBodyTd(getValue(row, "/html/body/table/tbody/tr/td/table/tbody/tr[4]/td[2]"));

        return record;
    }

    /**
     * Retrieve a column's value (by case‐insensitive columnName) from the row.
     * Return null if not found or out of range.
     */
    private String getValue(String[] row, String columnName) {
        if (columnName == null) return null;
        Integer idx = headerMap.get(columnName.toLowerCase());
        if (idx == null || idx < 0 || idx >= row.length) {
            return null;
        }
        // Optionally strip quotes:
        return CsvParsingUtils.stripSurroundingQuotes(row[idx]);
    }
}
