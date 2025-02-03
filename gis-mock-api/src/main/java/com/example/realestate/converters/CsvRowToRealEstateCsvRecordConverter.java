package com.example.realestate.converters;

import com.example.realestate.dto.RealEstateCsvRecord;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
public class CsvRowToRealEstateCsvRecordConverter implements Converter<String[], RealEstateCsvRecord> {

    private final Map<String, Integer> headerMap;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;

    public CsvRowToRealEstateCsvRecordConverter(Map<String, Integer> headerMap) {
        this.headerMap = headerMap;
    }

    @Override
    public RealEstateCsvRecord convert(String[] row) {
        RealEstateCsvRecord record = new RealEstateCsvRecord();
        // Do not set the ID since it will be generated automatically.
        // record.setId(getValue(row, "id")); // REMOVED

        record.setMlsNumber(getValue(row, "mls#") != null ? getValue(row, "mls#") : getValue(row, "mlsnumber"));
        record.setTaxId(getValue(row, "tax id"));
        record.setAddress(getValue(row, "address"));
        record.setCity(getValue(row, "city"));
        record.setState(getValue(row, "state"));
        record.setZip(getValue(row, "zip"));
        record.setStatus(getValue(row, "status"));
        record.setShortSale(getValue(row, "short sale"));
        record.setTimeUC(getValue(row, "time uc"));
        record.setDom(parseInteger(getValue(row, "dom")));
        record.setSoldDate(parseDate(getValue(row, "sold date")));
        record.setSoldTerms(getValue(row, "sold terms"));
        record.setSoldPrice(getValue(row, "sold price"));
        record.setSoldConcessions(getValue(row, "sold concessions"));
        record.setListPrice(getValue(row, "list price"));
        record.setOriginalListPrice(getValue(row, "original list price"));
        record.setUnderContractDate(parseDate(getValue(row, "under contract date")));
        record.setEntryDate(parseDate(getValue(row, "entry date")));
        record.setEffectiveDateOfListingAgreement(parseDate(getValue(row, "effective date of the listing agreement")));
        record.setStatusChangeDate(parseDate(getValue(row, "status change date")));
        record.setOffMarketDate(parseDate(getValue(row, "off market date")));
        record.setReinstatedDate(parseDate(getValue(row, "reinstated date")));
        record.setCancelDate(parseDate(getValue(row, "cancel date")));
        record.setOfferUnder3rdPartyReview(getValue(row, "offer under 3rd party review"));
        record.setPriceIncreaseDate(parseDate(getValue(row, "price increase date")));
        record.setPriceIncreaseDaysBack(parseInteger(getValue(row, "price increase days back")));
        record.setPriceReductionDate(parseDate(getValue(row, "price reduction date")));
        record.setPriceReductionDaysBack(parseInteger(getValue(row, "price reduction days back")));
        record.setBackupStatusDate(parseDate(getValue(row, "backup status date")));
        record.setWithdrawalDate(parseDate(getValue(row, "withdrawal date")));
        record.setExpireDate(parseDate(getValue(row, "expire date")));
        record.setAcres(parseBigDecimal(getValue(row, "acres")));
        record.setPropertyType(getValue(row, "property type"));
        record.setStyle(getValue(row, "style"));
        record.setYearBuilt(parseInteger(getValue(row, "year built")));
        record.setGrossLivingAreaGla(parseInteger(getValue(row, "gross living area (gla)")));
        record.setTotalSquareFeet(parseInteger(getValue(row, "total square feet")));
        record.setTotalBedrooms(parseInteger(getValue(row, "total bedrooms")));
        record.setTotalBathrooms(parseInteger(getValue(row, "total bathrooms")));
        record.setTotalFullBathrooms(parseInteger(getValue(row, "total full bathrooms")));
        record.setTotalThreeQuarterBathrooms(parseInteger(getValue(row, "total three-quarter bathrooms")));
        record.setTotalHalfBathrooms(parseInteger(getValue(row, "total half bathrooms")));
        record.setTotalKitchens(parseInteger(getValue(row, "total kitchens")));
        record.setBasementSquareFeet(parseInteger(getValue(row, "basement square feet")));
        record.setBasementFinished(parseBoolean(getValue(row, "basement finished")));
        record.setBasementBedrooms(parseInteger(getValue(row, "basement bedrooms")));
        record.setBasementFullBathrooms(parseInteger(getValue(row, "basement full bathrooms")));
        record.setBasementThreeQuarterBathrooms(parseInteger(getValue(row, "basement three-quarter bathrooms")));
        record.setBasementHalfBathrooms(parseInteger(getValue(row, "basement half bathrooms")));
        record.setBasement(getValue(row, "basement"));
        record.setHeating(getValue(row, "heating"));
        record.setAirConditioning(getValue(row, "air conditioning"));
        record.setGarageCapacity(parseInteger(getValue(row, "garage capacity")));
        record.setCarportCapacity(parseInteger(getValue(row, "carport capacity")));
        record.setGarageParking(getValue(row, "garage/parking"));
        record.setDecks(parseInteger(getValue(row, "decks")));
        record.setSolar(parseBoolean(getValue(row, "solar?")));
        record.setSolarOwnership(getValue(row, "solar ownership"));
        record.setTotalFireplaces(parseInteger(getValue(row, "total fireplaces")));
        record.setLandscape(getValue(row, "landscape"));
        record.setPoolAvailable(parseBoolean(getValue(row, "pool?")));
        record.setPoolDetails(getValue(row, "pool"));
        record.setHoaFee(getValue(row, "hoa fee"));
        record.setHoaAmenities(getValue(row, "hoa amenities"));
        record.setHoaRemarks(getValue(row, "hoa remarks"));
        record.setMainFloorSquareFeet(parseInteger(getValue(row, "main floor square feet")));
        record.setSecondFloorSquareFeet(parseInteger(getValue(row, "second floor square feet")));
        record.setThirdFloorSquareFeet(parseInteger(getValue(row, "third floor square feet")));
        record.setFourthFloorSquareFeet(parseInteger(getValue(row, "fourth floor square feet")));
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
        record.setAdu(parseBoolean(getValue(row, "adu")));
        record.setRemodUpdate(parseBoolean(getValue(row, "remod / update")));
        record.setOutbuilding(parseBoolean(getValue(row, "outbuilding")));
        record.setFullAddress(getValue(row, "full address"));
        record.setLatitude(parseBigDecimal(getValue(row, "latitude")));
        record.setLongitude(parseBigDecimal(getValue(row, "longitude")));
        record.setLegal(getValue(row, "legal"));
        record.setVerified(parseBoolean(getValue(row, "verified")));
        record.setDaysBack(parseInteger(getValue(row, "days back")));
        record.setLegalMatchToTax(parseBoolean(getValue(row, "legalmatchtotax")));
        record.setCountyRecordLink(getValue(row, "countyrecordlink"));
//        record.setHtmlBodyTd(getValue(row, "html_body_td"));
        return record;
    }

    private String getValue(String[] row, String columnName) {
        Integer index = headerMap.get(columnName.toLowerCase());
        if (index == null || index >= row.length) return null;
        return row[index].trim();
    }

    private Integer parseInteger(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Integer.parseInt(value.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : new BigDecimal(value.replaceAll("[$,]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : LocalDate.parse(value, dateFormatter);
        } catch (Exception ex) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) return null;
        value = value.toLowerCase();
        if (value.equals("yes") || value.equals("true") || value.equals("1")) return true;
        if (value.equals("no") || value.equals("false") || value.equals("0")) return false;
        return null;
    }
}
