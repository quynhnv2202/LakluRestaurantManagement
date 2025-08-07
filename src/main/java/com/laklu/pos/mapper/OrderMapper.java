package com.laklu.pos.mapper;



import com.laklu.pos.dataObjects.response.OrderResponse;
import com.laklu.pos.entities.Order;
import com.laklu.pos.enums.OrderStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "statusLabel", source = "status", qualifiedByName = "mapOrderStatusToLabel")
    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponses(List<Order> orders);

    @Named("mapOrderStatusToLabel")
    static String mapOrderStatusToLabel(OrderStatus status) {
        return status != null ? status.getLabel() : null;
    }
}
