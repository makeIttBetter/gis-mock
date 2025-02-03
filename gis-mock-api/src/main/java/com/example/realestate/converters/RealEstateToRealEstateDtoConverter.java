package com.example.realestate.converters;

import com.example.realestate.dto.RealEstateDto;
import com.example.realestate.model.RealEstate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RealEstateToRealEstateDtoConverter implements Converter<RealEstate, RealEstateDto> {

    @Override
    public RealEstateDto convert(RealEstate source) {
        log.debug("Converting RealEstate to RealEstateDto: {}", source);
        RealEstateDto dto = new RealEstateDto();
        dto.setId(source.getId());
        dto.setMlsNumber(source.getMlsNumber());
        dto.setAddress(source.getAddress());
        dto.setCity(source.getCity());
        dto.setState(source.getState());
        dto.setZip(source.getZip());
        dto.setStatus(source.getStatus());
        dto.setListPrice(source.getListPrice());
        dto.setLatitude(source.getLatitude().toString());
        dto.setLongitude(source.getLongitude().toString());
        log.debug("Conversion complete");
        return dto;
    }
}
