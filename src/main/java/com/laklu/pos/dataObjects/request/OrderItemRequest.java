package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemRequest {
    @NotNull(message = "Món ăn không được để trống")
    private Long menuItemId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

}
