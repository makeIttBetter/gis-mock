package com.example.realestate.converters;

import com.example.realestate.model.RealEstate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A specialized converter/utility to update an existing RealEstate
 * with fields from a "new" RealEstate object, without overwriting lat/lng
 * if the incoming row does not have them.
 * This is NOT a typical "Converter<S, D>", but a helper class to merge data.
 */
@Slf4j
@Component
public class RealEstateUpdateConverter {

    /**
     * Updates the fields on 'existing' with those from 'incoming',
     * except lat/lng if not provided by the new record.
     */
    public void updateFields(RealEstate existing, RealEstate incoming) {
        log.debug("Updating existing RealEstate (MLS#: {}) with incoming data (MLS#: {})",
                existing.getMlsNumber(), incoming.getMlsNumber());

        // Skip lat/lng updates if the new row had them missing
        if (incoming.getLatitude() != null) {
            existing.setLatitude(incoming.getLatitude());
        }
        if (incoming.getLongitude() != null) {
            existing.setLongitude(incoming.getLongitude());
        }

        // Then copy the other relevant fields:
        existing.setTaxId(incoming.getTaxId());
        existing.setAddress(incoming.getAddress());
        existing.setCity(incoming.getCity());
        existing.setState(incoming.getState());
        existing.setZip(incoming.getZip());
        existing.setStatus(incoming.getStatus());
        existing.setShortSale(incoming.getShortSale());
        existing.setTimeUC(incoming.getTimeUC());
        existing.setDom(incoming.getDom());
        existing.setSoldDate(incoming.getSoldDate());
        existing.setSoldTerms(incoming.getSoldTerms());
        existing.setSoldPrice(incoming.getSoldPrice());
        existing.setSoldConcessions(incoming.getSoldConcessions());
        existing.setListPrice(incoming.getListPrice());
        existing.setOriginalListPrice(incoming.getOriginalListPrice());
        existing.setUnderContractDate(incoming.getUnderContractDate());
        existing.setEntryDate(incoming.getEntryDate());
        existing.setEffectiveDateOfListingAgreement(incoming.getEffectiveDateOfListingAgreement());
        existing.setStatusChangeDate(incoming.getStatusChangeDate());
        existing.setOffMarketDate(incoming.getOffMarketDate());
        existing.setReinstatedDate(incoming.getReinstatedDate());
        existing.setCancelDate(incoming.getCancelDate());
        existing.setOfferUnder3rdPartyReview(incoming.getOfferUnder3rdPartyReview());
        existing.setPriceIncreaseDate(incoming.getPriceIncreaseDate());
        existing.setPriceIncreaseDaysBack(incoming.getPriceIncreaseDaysBack());
        existing.setPriceReductionDate(incoming.getPriceReductionDate());
        existing.setPriceReductionDaysBack(incoming.getPriceReductionDaysBack());
        existing.setBackupStatusDate(incoming.getBackupStatusDate());
        existing.setWithdrawalDate(incoming.getWithdrawalDate());
        existing.setExpireDate(incoming.getExpireDate());
        existing.setAcres(incoming.getAcres());
        existing.setPropertyType(incoming.getPropertyType());
        existing.setStyle(incoming.getStyle());
        existing.setYearBuilt(incoming.getYearBuilt());
        existing.setGrossLivingAreaGla(incoming.getGrossLivingAreaGla());
        existing.setTotalSquareFeet(incoming.getTotalSquareFeet());
        existing.setTotalBedrooms(incoming.getTotalBedrooms());
        existing.setTotalBathrooms(incoming.getTotalBathrooms());
        existing.setTotalFullBathrooms(incoming.getTotalFullBathrooms());
        existing.setTotalThreeQuarterBathrooms(incoming.getTotalThreeQuarterBathrooms());
        existing.setTotalHalfBathrooms(incoming.getTotalHalfBathrooms());
        existing.setTotalKitchens(incoming.getTotalKitchens());
        existing.setBasementSquareFeet(incoming.getBasementSquareFeet());
        existing.setBasementFinished(incoming.getBasementFinished());
        existing.setBasementBedrooms(incoming.getBasementBedrooms());
        existing.setBasementFullBathrooms(incoming.getBasementFullBathrooms());
        existing.setBasementThreeQuarterBathrooms(incoming.getBasementThreeQuarterBathrooms());
        existing.setBasementHalfBathrooms(incoming.getBasementHalfBathrooms());
        existing.setBasement(incoming.getBasement());
        existing.setHeating(incoming.getHeating());
        existing.setAirConditioning(incoming.getAirConditioning());
        existing.setGarageCapacity(incoming.getGarageCapacity());
        existing.setCarportCapacity(incoming.getCarportCapacity());
        existing.setGarageParking(incoming.getGarageParking());
        existing.setDecks(incoming.getDecks());
        existing.setSolar(incoming.getSolar());
        existing.setSolarOwnership(incoming.getSolarOwnership());
        existing.setTotalFireplaces(incoming.getTotalFireplaces());
        existing.setLandscape(incoming.getLandscape());
        existing.setPoolAvailable(incoming.getPoolAvailable());
        existing.setPoolDetails(incoming.getPoolDetails());
        existing.setHoaFee(incoming.getHoaFee());
        existing.setHoaAmenities(incoming.getHoaAmenities());
        existing.setHoaRemarks(incoming.getHoaRemarks());
        existing.setMainFloorSquareFeet(incoming.getMainFloorSquareFeet());
        existing.setSecondFloorSquareFeet(incoming.getSecondFloorSquareFeet());
        existing.setThirdFloorSquareFeet(incoming.getThirdFloorSquareFeet());
        existing.setFourthFloorSquareFeet(incoming.getFourthFloorSquareFeet());
        existing.setExteriorFeatures(incoming.getExteriorFeatures());
        existing.setFloor(incoming.getFloor());
        existing.setCounty(incoming.getCounty());
        existing.setInteriorFeatures(incoming.getInteriorFeatures());
        existing.setPublicRemarks(incoming.getPublicRemarks());
        existing.setBacCompensation(incoming.getBacCompensation());
        existing.setMlsLink(incoming.getMlsLink());
        existing.setQuality(incoming.getQuality());
        existing.setCondition(incoming.getCondition());
        existing.setLocationPositive(incoming.getLocationPositive());
        existing.setLocationNegative(incoming.getLocationNegative());
        existing.setViewPositive(incoming.getViewPositive());
        existing.setViewNegative(incoming.getViewNegative());
        existing.setAdu(incoming.getAdu());
        existing.setRemodUpdate(incoming.getRemodUpdate());
        existing.setOutbuilding(incoming.getOutbuilding());
        existing.setFullAddress(incoming.getFullAddress());
        existing.setLegal(incoming.getLegal());
        existing.setVerified(incoming.getVerified());
        existing.setLegalMatchToTax(incoming.getLegalMatchToTax());
        existing.setCountyRecordLink(incoming.getCountyRecordLink());
        existing.setHtmlBodyTd(incoming.getHtmlBodyTd());

        log.debug("Finished updating existing RealEstate (MLS#: {})", existing.getMlsNumber());
    }
}
