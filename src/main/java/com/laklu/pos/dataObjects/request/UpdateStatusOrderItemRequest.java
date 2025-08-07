package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.validator.ValidEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateStatusOrderItemRequest {

    @ValidEnum(enumClass = OrderItemStatus.class, message = "Trạng thái món ăn đang đặt không hợp lệ!")
    String status;
}
