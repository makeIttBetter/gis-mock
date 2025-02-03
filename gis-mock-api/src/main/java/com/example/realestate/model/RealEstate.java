package com.example.realestate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "real_estate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealEstate extends Model {

    @Column(name = "mls_number")
    private String mlsNumber;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "status")
    private String status;

    @Column(name = "short_sale")
    private String shortSale;

    @Column(name = "time_uc")
    private String timeUC;

    @Column(name = "dom")
    private Integer dom;

    @Column(name = "sold_date")
    private LocalDate soldDate;

    @Column(name = "sold_terms")
    private String soldTerms;

    @Column(name = "sold_price")
    private String soldPrice;

    @Column(name = "sold_concessions")
    private String soldConcessions;

    @Column(name = "list_price")
    private String listPrice;

    @Column(name = "original_list_price")
    private String originalListPrice;

    @Column(name = "under_contract_date")
    private LocalDate underContractDate;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "effective_date_of_listing_agreement")
    private LocalDate effectiveDateOfListingAgreement;

    @Column(name = "status_change_date")
    private LocalDate statusChangeDate;

    @Column(name = "off_market_date")
    private LocalDate offMarketDate;

    @Column(name = "reinstated_date")
    private LocalDate reinstatedDate;

    @Column(name = "cancel_date")
    private LocalDate cancelDate;

    @Column(name = "offer_under_3rd_party_review")
    private String offerUnder3rdPartyReview;

    @Column(name = "price_increase_date")
    private LocalDate priceIncreaseDate;

    @Column(name = "price_increase_days_back")
    private Integer priceIncreaseDaysBack;

    @Column(name = "price_reduction_date")
    private LocalDate priceReductionDate;

    @Column(name = "price_reduction_days_back")
    private Integer priceReductionDaysBack;

    @Column(name = "backup_status_date")
    private LocalDate backupStatusDate;

    @Column(name = "withdrawal_date")
    private LocalDate withdrawalDate;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "acres", precision = 5, scale = 2)
    private BigDecimal acres;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "style")
    private String style;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "gross_living_area_gla")
    private Integer grossLivingAreaGla;

    @Column(name = "total_square_feet")
    private Integer totalSquareFeet;

    @Column(name = "total_bedrooms")
    private Integer totalBedrooms;

    @Column(name = "total_bathrooms")
    private Integer totalBathrooms;

    @Column(name = "total_full_bathrooms")
    private Integer totalFullBathrooms;

    @Column(name = "total_three_quarter_bathrooms")
    private Integer totalThreeQuarterBathrooms;

    @Column(name = "total_half_bathrooms")
    private Integer totalHalfBathrooms;

    @Column(name = "total_kitchens")
    private Integer totalKitchens;

    @Column(name = "basement_square_feet")
    private Integer basementSquareFeet;

    @Column(name = "basement_finished")
    private Boolean basementFinished;

    @Column(name = "basement_bedrooms")
    private Integer basementBedrooms;

    @Column(name = "basement_full_bathrooms")
    private Integer basementFullBathrooms;

    @Column(name = "basement_three_quarter_bathrooms")
    private Integer basementThreeQuarterBathrooms;

    @Column(name = "basement_half_bathrooms")
    private Integer basementHalfBathrooms;

    @Column(name = "basement")
    private String basement;

    @Column(name = "heating")
    private String heating;

    @Column(name = "air_conditioning")
    private String airConditioning;

    @Column(name = "garage_capacity")
    private Integer garageCapacity;

    @Column(name = "carport_capacity")
    private Integer carportCapacity;

    @Column(name = "garage_parking")
    private String garageParking;

    @Column(name = "decks")
    private Integer decks;

    @Column(name = "solar")
    private Boolean solar;

    @Column(name = "solar_ownership")
    private String solarOwnership;

    @Column(name = "total_fireplaces")
    private Integer totalFireplaces;

    @Column(name = "landscape")
    private String landscape;

    @Column(name = "pool_available")
    private Boolean poolAvailable;

    @Column(name = "pool_details")
    private String poolDetails;

    @Column(name = "hoa_fee")
    private String hoaFee;

    @Column(name = "hoa_amenities")
    private String hoaAmenities;

    @Column(name = "hoa_remarks")
    private String hoaRemarks;

    @Column(name = "main_floor_square_feet")
    private Integer mainFloorSquareFeet;

    @Column(name = "second_floor_square_feet")
    private Integer secondFloorSquareFeet;

    @Column(name = "third_floor_square_feet")
    private Integer thirdFloorSquareFeet;

    @Column(name = "fourth_floor_square_feet")
    private Integer fourthFloorSquareFeet;

    @Column(name = "exterior_features")
    private String exteriorFeatures;

    @Column(name = "floor")
    private String floor;

    @Column(name = "county")
    private String county;

    @Column(name = "interior_features")
    private String interiorFeatures;

    @Column(name = "public_remarks", columnDefinition = "TEXT")
    private String publicRemarks;

    @Column(name = "bac_compensation")
    private String bacCompensation;

    @Column(name = "mls_link")
    private String mlsLink;

    @Column(name = "quality")
    private String quality;

    @Column(name = "condition")
    private String condition;

    @Column(name = "location_positive")
    private String locationPositive;

    @Column(name = "location_negative")
    private String locationNegative;

    @Column(name = "view_positive")
    private String viewPositive;

    @Column(name = "view_negative")
    private String viewNegative;

    @Column(name = "adu")
    private Boolean adu;

    @Column(name = "remod_update")
    private Boolean remodUpdate;

    @Column(name = "outbuilding")
    private Boolean outbuilding;

    @Column(name = "full_address")
    private String fullAddress;

    @Column(name = "latitude", precision = 38, scale = 20)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 38, scale = 20)
    private BigDecimal longitude;

    @Column(name = "legal")
    private String legal;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "days_back")
    private Integer daysBack;

    @Column(name = "legal_match_to_tax")
    private Boolean legalMatchToTax;

    @Column(name = "county_record_link")
    private String countyRecordLink;

    @Column(name = "html_body_td")
    private String htmlBodyTd;
}
