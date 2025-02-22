package com.example.realestate.converters.model;

import com.example.realestate.dto.model.RealEstateUpdateDto;
import com.example.realestate.model.RealEstate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Performs a partial update on an existing RealEstate
 * by reading fields from RealEstateUpdateDto, skipping null fields.
 */
@Slf4j
@Component
public class RealEstatePartialUpdateConverter {

    /**
     * Updates the given RealEstate (in place) with non-null fields from the updateDto.
     */
    public void updateEntity(RealEstate existing, RealEstateUpdateDto updateDto) {
        log.debug("Updating RealEstate ID={} with updateDto: {}", existing.getId(), updateDto);

        if (updateDto.getSoldTerms() != null) {
            existing.setSoldTerms(updateDto.getSoldTerms());
        }
        if (updateDto.getSoldPrice() != null) {
            existing.setSoldPrice(updateDto.getSoldPrice());
        }
        if (updateDto.getMlsNumber() != null) {
            existing.setMlsNumber(updateDto.getMlsNumber());
        }
        if (updateDto.getTaxId() != null) {
            existing.setTaxId(updateDto.getTaxId());
        }
        if (updateDto.getAddress() != null) {
            existing.setAddress(updateDto.getAddress());
        }
        if (updateDto.getCity() != null) {
            existing.setCity(updateDto.getCity());
        }
        if (updateDto.getState() != null) {
            existing.setState(updateDto.getState());
        }
        if (updateDto.getZip() != null) {
            existing.setZip(updateDto.getZip());
        }
        if (updateDto.getStatus() != null) {
            existing.setStatus(updateDto.getStatus());
        }

        // The "fullAddress" is handled in the service layer after this converter
        // so that we can rebuild it if any address parts changed.
    }
}
