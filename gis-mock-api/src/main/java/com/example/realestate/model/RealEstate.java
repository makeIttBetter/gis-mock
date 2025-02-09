package com.example.realestate.model;

import com.example.realestate.annotations.ExportField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "real_estate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealEstate extends Model {

    @ExportField(fieldName = "mls_number", displayName = "MLS#")
    @Column(name = "mls_number")
    private String mlsNumber;

    @ExportField(fieldName = "tax_id", displayName = "Tax ID")
    @Column(name = "tax_id")
    private String taxId;

    @ExportField(fieldName = "address", displayName = "Address")
    @Column(name = "address")
    private String address;

    @ExportField(fieldName = "city", displayName = "City")
    @Column(name = "city")
    private String city;

    @ExportField(fieldName = "state", displayName = "State")
    @Column(name = "state")
    private String state;

    @ExportField(fieldName = "zip", displayName = "Zip")
    @Column(name = "zip")
    private String zip;

    @ExportField(fieldName = "status", displayName = "Status")
    @Column(name = "status")
    private String status;

    @ExportField(fieldName = "short_sale", displayName = "Short Sale")
    @Column(name = "short_sale")
    private String shortSale;

    @ExportField(fieldName = "time_uc", displayName = "Time UC")
    @Column(name = "time_uc")
    private String timeUC;

    @ExportField(fieldName = "dom", displayName = "DOM")
    @Column(name = "dom")
    private Integer dom;

    @ExportField(fieldName = "sold_date", displayName = "Sold Date")
    @Column(name = "sold_date")
    private LocalDate soldDate;

    @ExportField(fieldName = "sold_terms", displayName = "Sold Terms")
    @Column(name = "sold_terms")
    private String soldTerms;

    @ExportField(fieldName = "sold_price", displayName = "Sold Price")
    @Column(name = "sold_price")
    private String soldPrice;

    @ExportField(fieldName = "sold_concessions", displayName = "Sold Concessions")
    @Column(name = "sold_concessions")
    private String soldConcessions;

    @ExportField(fieldName = "list_price", displayName = "List Price")
    @Column(name = "list_price")
    private String listPrice;

    @ExportField(fieldName = "original_list_price", displayName = "Original List Price")
    @Column(name = "original_list_price")
    private String originalListPrice;

    @ExportField(fieldName = "under_contract_date", displayName = "Under Contract Date")
    @Column(name = "under_contract_date")
    private LocalDate underContractDate;

    @ExportField(fieldName = "entry_date", displayName = "Entry Date")
    @Column(name = "entry_date")
    private LocalDate entryDate;

    @ExportField(fieldName = "effective_date_of_listing_agreement", displayName = "Effective Date Of The Listing Agreement")
    @Column(name = "effective_date_of_listing_agreement")
    private LocalDate effectiveDateOfListingAgreement;

    @ExportField(fieldName = "status_change_date", displayName = "Status Change Date")
    @Column(name = "status_change_date")
    private LocalDate statusChangeDate;

    @ExportField(fieldName = "off_market_date", displayName = "Off Market Date")
    @Column(name = "off_market_date")
    private LocalDate offMarketDate;

    @ExportField(fieldName = "reinstated_date", displayName = "Reinstated Date")
    @Column(name = "reinstated_date")
    private LocalDate reinstatedDate;

    @ExportField(fieldName = "cancel_date", displayName = "Cancel Date")
    @Column(name = "cancel_date")
    private LocalDate cancelDate;

    @ExportField(fieldName = "offer_under_3rd_party_review", displayName = "Offer Under 3rd Party Review")
    @Column(name = "offer_under_3rd_party_review")
    private String offerUnder3rdPartyReview;

    @ExportField(fieldName = "price_increase_date", displayName = "Price Increase Date")
    @Column(name = "price_increase_date")
    private LocalDate priceIncreaseDate;

    @ExportField(fieldName = "price_increase_days_back", displayName = "Price Increase Days Back")
    @Column(name = "price_increase_days_back")
    private Integer priceIncreaseDaysBack;

    @ExportField(fieldName = "price_reduction_date", displayName = "Price Reduction Date")
    @Column(name = "price_reduction_date")
    private LocalDate priceReductionDate;

    @ExportField(fieldName = "price_reduction_days_back", displayName = "Price Reduction Days Back")
    @Column(name = "price_reduction_days_back")
    private Integer priceReductionDaysBack;

    @ExportField(fieldName = "backup_status_date", displayName = "Backup Status Date")
    @Column(name = "backup_status_date")
    private LocalDate backupStatusDate;

    @ExportField(fieldName = "withdrawal_date", displayName = "Withdrawal Date")
    @Column(name = "withdrawal_date")
    private LocalDate withdrawalDate;

    @ExportField(fieldName = "expire_date", displayName = "Expire Date")
    @Column(name = "expire_date")
    private LocalDate expireDate;

    @ExportField(fieldName = "acres", displayName = "Acres")
    @Column(name = "acres", precision = 5, scale = 2)
    private BigDecimal acres;

    @ExportField(fieldName = "property_type", displayName = "Property Type")
    @Column(name = "property_type")
    private String propertyType;

    @ExportField(fieldName = "style", displayName = "Style")
    @Column(name = "style")
    private String style;

    @ExportField(fieldName = "year_built", displayName = "Year Built")
    @Column(name = "year_built")
    private Integer yearBuilt;

    @ExportField(fieldName = "gross_living_area_gla", displayName = "Gross Living Area (GLA)")
    @Column(name = "gross_living_area_gla")
    private Integer grossLivingAreaGla;

    @ExportField(fieldName = "total_square_feet", displayName = "Total Square Feet")
    @Column(name = "total_square_feet")
    private Integer totalSquareFeet;

    @ExportField(fieldName = "total_bedrooms", displayName = "Total Bedrooms")
    @Column(name = "total_bedrooms")
    private Integer totalBedrooms;

    @ExportField(fieldName = "total_bathrooms", displayName = "Total Bathrooms")
    @Column(name = "total_bathrooms")
    private Integer totalBathrooms;

    @ExportField(fieldName = "total_full_bathrooms", displayName = "Total Full Bathrooms")
    @Column(name = "total_full_bathrooms")
    private Integer totalFullBathrooms;

    @ExportField(fieldName = "total_three_quarter_bathrooms", displayName = "Total Three-quarter Bathrooms")
    @Column(name = "total_three_quarter_bathrooms")
    private Integer totalThreeQuarterBathrooms;

    @ExportField(fieldName = "total_half_bathrooms", displayName = "Total Half Bathrooms")
    @Column(name = "total_half_bathrooms")
    private Integer totalHalfBathrooms;

    @ExportField(fieldName = "total_kitchens", displayName = "Total Kitchens")
    @Column(name = "total_kitchens")
    private Integer totalKitchens;

    @ExportField(fieldName = "basement_square_feet", displayName = "Basement Square Feet")
    @Column(name = "basement_square_feet")
    private Integer basementSquareFeet;

    @ExportField(fieldName = "basement_finished", displayName = "Basement Finished")
    @Column(name = "basement_finished")
    private Boolean basementFinished;

    @ExportField(fieldName = "basement_bedrooms", displayName = "Basement Bedrooms")
    @Column(name = "basement_bedrooms")
    private Integer basementBedrooms;

    @ExportField(fieldName = "basement_full_bathrooms", displayName = "Basement Full Bathrooms")
    @Column(name = "basement_full_bathrooms")
    private Integer basementFullBathrooms;

    @ExportField(fieldName = "basement_three_quarter_bathrooms", displayName = "Basement Three-quarter Bathrooms")
    @Column(name = "basement_three_quarter_bathrooms")
    private Integer basementThreeQuarterBathrooms;

    @ExportField(fieldName = "basement_half_bathrooms", displayName = "Basement Half Bathrooms")
    @Column(name = "basement_half_bathrooms")
    private Integer basementHalfBathrooms;

    @ExportField(fieldName = "basement", displayName = "Basement")
    @Column(name = "basement")
    private String basement;

    @ExportField(fieldName = "heating", displayName = "Heating")
    @Column(name = "heating")
    private String heating;

    @ExportField(fieldName = "air_conditioning", displayName = "Air Conditioning")
    @Column(name = "air_conditioning")
    private String airConditioning;

    @ExportField(fieldName = "garage_capacity", displayName = "Garage Capacity")
    @Column(name = "garage_capacity")
    private Integer garageCapacity;

    @ExportField(fieldName = "carport_capacity", displayName = "Carport Capacity")
    @Column(name = "carport_capacity")
    private Integer carportCapacity;

    @ExportField(fieldName = "garage_parking", displayName = "Garage/Parking")
    @Column(name = "garage_parking")
    private String garageParking;

    @ExportField(fieldName = "decks", displayName = "Decks")
    @Column(name = "decks")
    private Integer decks;

    @ExportField(fieldName = "solar", displayName = "Solar?")
    @Column(name = "solar")
    private Boolean solar;

    @ExportField(fieldName = "solar_ownership", displayName = "Solar Ownership")
    @Column(name = "solar_ownership")
    private String solarOwnership;

    @ExportField(fieldName = "total_fireplaces", displayName = "Total Fireplaces")
    @Column(name = "total_fireplaces")
    private Integer totalFireplaces;

    @ExportField(fieldName = "landscape", displayName = "Landscape")
    @Column(name = "landscape")
    private String landscape;

    @ExportField(fieldName = "pool_available", displayName = "Pool?")
    @Column(name = "pool_available")
    private Boolean poolAvailable;

    @ExportField(fieldName = "pool_details", displayName = "Pool")
    @Column(name = "pool_details")
    private String poolDetails;

    @ExportField(fieldName = "hoa_fee", displayName = "HOA Fee")
    @Column(name = "hoa_fee")
    private String hoaFee;

    @ExportField(fieldName = "hoa_amenities", displayName = "HOA Amenities")
    @Column(name = "hoa_amenities")
    private String hoaAmenities;

    @ExportField(fieldName = "hoa_remarks", displayName = "HOA Remarks")
    @Column(name = "hoa_remarks")
    private String hoaRemarks;

    @ExportField(fieldName = "main_floor_square_feet", displayName = "Main Floor Square Feet")
    @Column(name = "main_floor_square_feet")
    private Integer mainFloorSquareFeet;

    @ExportField(fieldName = "second_floor_square_feet", displayName = "Second Floor Square Feet")
    @Column(name = "second_floor_square_feet")
    private Integer secondFloorSquareFeet;

    @ExportField(fieldName = "third_floor_square_feet", displayName = "Third Floor Square Feet")
    @Column(name = "third_floor_square_feet")
    private Integer thirdFloorSquareFeet;

    @ExportField(fieldName = "fourth_floor_square_feet", displayName = "Fourth Floor Square Feet")
    @Column(name = "fourth_floor_square_feet")
    private Integer fourthFloorSquareFeet;

    @ExportField(fieldName = "exterior_features", displayName = "Exterior Features")
    @Column(name = "exterior_features")
    private String exteriorFeatures;

    @ExportField(fieldName = "floor", displayName = "Floor")
    @Column(name = "floor")
    private String floor;

    @ExportField(fieldName = "county", displayName = "County")
    @Column(name = "county")
    private String county;

    @ExportField(fieldName = "interior_features", displayName = "Interior Features")
    @Column(name = "interior_features")
    private String interiorFeatures;

    @ExportField(fieldName = "public_remarks", displayName = "Public Remarks")
    @Column(name = "public_remarks", columnDefinition = "TEXT")
    private String publicRemarks;

    @ExportField(fieldName = "bac_compensation", displayName = "BAC Compensation")
    @Column(name = "bac_compensation")
    private String bacCompensation;

    @ExportField(fieldName = "mls_link", displayName = "MLS Link")
    @Column(name = "mls_link")
    private String mlsLink;

    @ExportField(fieldName = "quality", displayName = "Quality")
    @Column(name = "quality")
    private String quality;

    @ExportField(fieldName = "condition", displayName = "Condition")
    @Column(name = "condition")
    private String condition;

    @ExportField(fieldName = "location_positive", displayName = "Location +")
    @Column(name = "location_positive")
    private String locationPositive;

    @ExportField(fieldName = "location_negative", displayName = "Location -")
    @Column(name = "location_negative")
    private String locationNegative;

    @ExportField(fieldName = "view_positive", displayName = "View +")
    @Column(name = "view_positive")
    private String viewPositive;

    @ExportField(fieldName = "view_negative", displayName = "View -")
    @Column(name = "view_negative")
    private String viewNegative;

    @ExportField(fieldName = "adu", displayName = "ADU")
    @Column(name = "adu")
    private Boolean adu;

    @ExportField(fieldName = "remod_update", displayName = "Remod / Update")
    @Column(name = "remod_update")
    private Boolean remodUpdate;

    @ExportField(fieldName = "outbuilding", displayName = "Outbuilding")
    @Column(name = "outbuilding")
    private Boolean outbuilding;

    @ExportField(fieldName = "full_address", displayName = "Full Address")
    @Column(name = "full_address")
    private String fullAddress;

    @ExportField(fieldName = "latitude", displayName = "Latitude")
    @Column(name = "latitude", precision = 38, scale = 20)
    private BigDecimal latitude;

    @ExportField(fieldName = "longitude", displayName = "Longitude")
    @Column(name = "longitude", precision = 38, scale = 20)
    private BigDecimal longitude;

    @ExportField(fieldName = "legal", displayName = "Legal")
    @Column(name = "legal")
    private String legal;

    @ExportField(fieldName = "verified", displayName = "Verified")
    @Column(name = "verified")
    private Boolean verified;

    @ExportField(fieldName = "legal_match_to_tax", displayName = "LegalMatchToTax")
    @Column(name = "legal_match_to_tax")
    private Boolean legalMatchToTax;

    @ExportField(fieldName = "county_record_link", displayName = "CountyRecordLink")
    @Column(name = "county_record_link")
    private String countyRecordLink;

    @ExportField(fieldName = "html_body_td", displayName = "/html/body/table/tbody/tr/td/table/tbody/tr[4]/td[2]")
    @Column(name = "html_body_td")
    private String htmlBodyTd;

    // Export getDaysBack via a getter method below.
    @ExportField(fieldName = "days_back", displayName = "Days Back")
    @Transient
    public Integer getDaysBack() {
        if (this.soldDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        return (int) ChronoUnit.DAYS.between(this.soldDate, today);
    }
}
