package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.NewTable;
import com.laklu.pos.dataObjects.request.TableUpdateRequest;
import com.laklu.pos.entities.Table;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TableMapper {

    @Mapping(target = "status", constant = "AVAILABLE") // Mặc định trạng thái là AVAILABLE
    Table toEntity(NewTable request);

    // Chỉ cập nhật các trường không null từ request vào entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTable(TableUpdateRequest request, @MappingTarget Table table);
}
