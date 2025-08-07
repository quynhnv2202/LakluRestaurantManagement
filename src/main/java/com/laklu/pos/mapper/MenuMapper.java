package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.NewMenu;
import com.laklu.pos.entities.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MenuMapper {
    void updateMenuFromDto(NewMenu dto, @MappingTarget Menu entity);
}
