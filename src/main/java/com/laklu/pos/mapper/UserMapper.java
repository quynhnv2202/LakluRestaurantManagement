package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.NewMenu;
import com.laklu.pos.dataObjects.request.UpdateUser;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    void updateUserFromDto(UpdateUser dto, @MappingTarget User entity);
}
