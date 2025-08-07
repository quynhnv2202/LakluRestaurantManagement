package com.laklu.pos.mapper;

import com.laklu.pos.dataObjects.response.OrderItemResponse;
import com.laklu.pos.entities.OrderItem;
import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {
    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "menuItemId", source = "menuItem.id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "statusLabel", source = "status", qualifiedByName = "mapOrderItemStatusToLabel")
    OrderItemResponse toResponse(OrderItem orderItem);

    List<OrderItemResponse> toResponseList(List<OrderItem> orderItems);

    @Named("mapOrderItemStatusToLabel")
    static String mapOrderItemStatusToLabel(OrderItemStatus status) {
        return status != null ? status.getLabel() : null;
    }
}
