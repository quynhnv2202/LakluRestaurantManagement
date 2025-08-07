package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.NewSalaryRate;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.enums.SalaryType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SalaryRateMapper {

    @Mapping(target = "type", source = "type", qualifiedByName = "stringToSalaryType")
    SalaryRate toEntity(NewSalaryRate newSalaryRate);

    @Mapping(target = "type", source = "type", qualifiedByName = "stringToSalaryType")
    void updateEntityFromDto(NewSalaryRate newSalaryRate, @MappingTarget SalaryRate salaryRate);

    @Named("stringToSalaryType")
    static SalaryType stringToSalaryType(String type) {
        if (type == null) {
            return null;
        }
        return SalaryType.valueOf(type.toUpperCase());
    }
}