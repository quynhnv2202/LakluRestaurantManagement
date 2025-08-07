package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateOrderItemQuantity {

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
}
