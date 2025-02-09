package com.example.realestate.export.cnst;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration specifying the exact order of CSV columns for RealEstate exports.
 * The keys MUST match the @ExportField.fieldName in the RealEstate entity
 * (e.g. "mls_number", "tax_id", "short_sale", etc.).
 * The values are the CSV column headers.
 * The LinkedHashMap preserves insertion order.
 */
@Getter
@Component
public class RealEstateCsvConfig {

    private final Map<String, String> exportColumnsOrdered = new LinkedHashMap<>();

    public RealEstateCsvConfig() {

        // From Model.java:
        exportColumnsOrdered.put("id", "ID");

        // RealEstate.java (snake_case match with @ExportField annotations):
        exportColumnsOrdered.put("mls_number", "MLS#");
        exportColumnsOrdered.put("tax_id", "Tax ID");
        exportColumnsOrdered.put("address", "Address");
        exportColumnsOrdered.put("city", "City");
        exportColumnsOrdered.put("state", "State");
        exportColumnsOrdered.put("zip", "Zip");
        exportColumnsOrdered.put("status", "Status");
        exportColumnsOrdered.put("short_sale", "Short Sale");
        exportColumnsOrdered.put("time_uc", "Time UC");
        exportColumnsOrdered.put("dom", "DOM");
        exportColumnsOrdered.put("sold_date", "Sold Date");
        exportColumnsOrdered.put("sold_terms", "Sold Terms");
        exportColumnsOrdered.put("sold_price", "Sold Price");
        exportColumnsOrdered.put("sold_concessions", "Sold Concessions");
        exportColumnsOrdered.put("list_price", "List Price");
        exportColumnsOrdered.put("original_list_price", "Original List Price");
        exportColumnsOrdered.put("under_contract_date", "Under Contract Date");
        exportColumnsOrdered.put("entry_date", "Entry Date");
        exportColumnsOrdered.put("effective_date_of_listing_agreement", "Effective Date Of The Listing Agreement");
        exportColumnsOrdered.put("status_change_date", "Status Change Date");
        exportColumnsOrdered.put("off_market_date", "Off Market Date");
        exportColumnsOrdered.put("reinstated_date", "Reinstated Date");
        exportColumnsOrdered.put("cancel_date", "Cancel Date");
        exportColumnsOrdered.put("offer_under_3rd_party_review", "Offer Under 3rd Party Review");
        exportColumnsOrdered.put("price_increase_date", "Price Increase Date");
        exportColumnsOrdered.put("price_increase_days_back", "Price Increase Days Back");
        exportColumnsOrdered.put("price_reduction_date", "Price Reduction Date");
        exportColumnsOrdered.put("price_reduction_days_back", "Price Reduction Days Back");
        exportColumnsOrdered.put("backup_status_date", "Backup Status Date");
        exportColumnsOrdered.put("withdrawal_date", "Withdrawal Date");
        exportColumnsOrdered.put("expire_date", "Expire Date");
        exportColumnsOrdered.put("acres", "Acres");
        exportColumnsOrdered.put("property_type", "Property Type");
        exportColumnsOrdered.put("style", "Style");
        exportColumnsOrdered.put("year_built", "Year Built");
        exportColumnsOrdered.put("gross_living_area_gla", "Gross Living Area (GLA)");
        exportColumnsOrdered.put("total_square_feet", "Total Square Feet");
        exportColumnsOrdered.put("total_bedrooms", "Total Bedrooms");
        exportColumnsOrdered.put("total_bathrooms", "Total Bathrooms");
        exportColumnsOrdered.put("total_full_bathrooms", "Total Full Bathrooms");
        exportColumnsOrdered.put("total_three_quarter_bathrooms", "Total Three-quarter Bathrooms");
        exportColumnsOrdered.put("total_half_bathrooms", "Total Half Bathrooms");
        exportColumnsOrdered.put("total_kitchens", "Total Kitchens");
        exportColumnsOrdered.put("basement_square_feet", "Basement Square Feet");
        exportColumnsOrdered.put("basement_finished", "Basement Finished");
        exportColumnsOrdered.put("basement_bedrooms", "Basement Bedrooms");
        exportColumnsOrdered.put("basement_full_bathrooms", "Basement Full Bathrooms");
        exportColumnsOrdered.put("basement_three_quarter_bathrooms", "Basement Three-quarter Bathrooms");
        exportColumnsOrdered.put("basement_half_bathrooms", "Basement Half Bathrooms");
        exportColumnsOrdered.put("basement", "Basement");
        exportColumnsOrdered.put("heating", "Heating");
        exportColumnsOrdered.put("air_conditioning", "Air Conditioning");
        exportColumnsOrdered.put("garage_capacity", "Garage Capacity");
        exportColumnsOrdered.put("carport_capacity", "Carport Capacity");
        exportColumnsOrdered.put("garage_parking", "Garage/Parking");
        exportColumnsOrdered.put("decks", "Decks");
        exportColumnsOrdered.put("solar", "Solar?");
        exportColumnsOrdered.put("solar_ownership", "Solar Ownership");
        exportColumnsOrdered.put("total_fireplaces", "Total Fireplaces");
        exportColumnsOrdered.put("landscape", "Landscape");
        exportColumnsOrdered.put("pool_available", "Pool?");
        exportColumnsOrdered.put("pool_details", "Pool");
        exportColumnsOrdered.put("hoa_fee", "HOA Fee");
        exportColumnsOrdered.put("hoa_amenities", "HOA Amenities");
        exportColumnsOrdered.put("hoa_remarks", "HOA Remarks");
        exportColumnsOrdered.put("main_floor_square_feet", "Main Floor Square Feet");
        exportColumnsOrdered.put("second_floor_square_feet", "Second Floor Square Feet");
        exportColumnsOrdered.put("third_floor_square_feet", "Third Floor Square Feet");
        exportColumnsOrdered.put("fourth_floor_square_feet", "Fourth Floor Square Feet");
        exportColumnsOrdered.put("exterior_features", "Exterior Features");
        exportColumnsOrdered.put("floor", "Floor");
        exportColumnsOrdered.put("county", "County");
        exportColumnsOrdered.put("interior_features", "Interior Features");
        exportColumnsOrdered.put("public_remarks", "Public Remarks");
        exportColumnsOrdered.put("bac_compensation", "BAC Compensation");
        exportColumnsOrdered.put("mls_link", "MLS Link");
        exportColumnsOrdered.put("quality", "Quality");
        exportColumnsOrdered.put("condition", "Condition");
        exportColumnsOrdered.put("location_positive", "Location +");
        exportColumnsOrdered.put("location_negative", "Location -");
        exportColumnsOrdered.put("view_positive", "View +");
        exportColumnsOrdered.put("view_negative", "View -");
        exportColumnsOrdered.put("adu", "ADU");
        exportColumnsOrdered.put("remod_update", "Remod / Update");
        exportColumnsOrdered.put("outbuilding", "Outbuilding");
        exportColumnsOrdered.put("full_address", "Full Address");
        exportColumnsOrdered.put("latitude", "Latitude");
        exportColumnsOrdered.put("longitude", "Longitude");
        exportColumnsOrdered.put("legal", "Legal");
        exportColumnsOrdered.put("verified", "Verified");
        exportColumnsOrdered.put("days_back", "Days Back");
        exportColumnsOrdered.put("legal_match_to_tax", "LegalMatchToTax");
        exportColumnsOrdered.put("county_record_link", "CountyRecordLink");
        exportColumnsOrdered.put("html_body_td", "/html/body/table/tbody/tr/td/table/tbody/tr[4]/td[2]");

        // Fields from Model.java
        exportColumnsOrdered.put("created_at", "Created at");
        exportColumnsOrdered.put("updated_at", "Updated at");
    }
}
