package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.UpdateAttendanceDTO;
import com.laklu.pos.entities.Attendance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAttendanceFromDto(UpdateAttendanceDTO updateAttendanceDTO, @MappingTarget Attendance attendance);
} 