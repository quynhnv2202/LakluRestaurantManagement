package com.laklu.pos.dataObjects.response;

import com.laklu.pos.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponseDTO {
    Integer id;
    Integer reservationId;
    int staffId;
    String statusLabel;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<TableInfo> tables;
    List<OrderItemResponse> orderItems;
}
