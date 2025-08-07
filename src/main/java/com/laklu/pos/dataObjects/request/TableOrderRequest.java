package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableOrderRequest {
    @Size(min = 3, max = 100, message = "Tên phải nằm trong khoảng từ 3 đến 100 kí tự")
    String customerName = "khách_hàng";

    String customerPhone = "0000000000";

    @NotEmpty(message = "Hãy chọn ít nhất một bàn")
    List<Integer> tableIds;

    Integer numberOfPeople = 1;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<NewOrderItemRequest> orderItems;
} 