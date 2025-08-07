package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.request.NewSchedule;
import com.laklu.pos.entities.Schedule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    Schedule toSchedule(NewSchedule newSchedule);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateScheduleFromDto(NewSchedule newSchedule, @MappingTarget Schedule schedule);
}
