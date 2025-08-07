package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewOrderRequest {
    @NotNull(message = "Yêu cầu thông tin đặt bàn")
    Integer reservationId;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<NewOrderItemRequest> orderItems;

}
