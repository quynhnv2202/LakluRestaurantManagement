package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.OrderStatus;
import com.laklu.pos.validator.ValidEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStatusOrderRequest {

    @ValidEnum(enumClass = OrderStatus.class, message = "Trạng thái đơn hàng không hợp lệ!")
    String status;

}
