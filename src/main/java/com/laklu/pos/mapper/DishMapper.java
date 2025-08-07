package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.NewDish;
import com.laklu.pos.dataObjects.response.DishResponse;
import com.laklu.pos.entities.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DishMapper {
    void updateDishFromDto(NewDish dto, @MappingTarget Dish entity);

    DishResponse toDishResponse(Dish dish);
}
