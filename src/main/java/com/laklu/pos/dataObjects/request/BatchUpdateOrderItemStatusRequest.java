package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.OrderItemStatus;
import com.laklu.pos.validator.ValidEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateOrderItemStatusRequest {
    @ValidEnum(enumClass = OrderItemStatus.class, message = "Trạng thái món ăn đang đặt không hợp lệ!")
    private String status;
    
    @NotEmpty(message = "Danh sách ID không được để trống")
    private List<Integer> orderItemIds;
} 