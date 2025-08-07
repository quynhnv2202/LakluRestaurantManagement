package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderRequest {
    @NotNull(message = "Yêu cầu đặt bàn")
    private Long reservationId;

    @NotNull(message = "Yêu cầu nhân viên")
    private Long staffId;

    @NotNull(message = "Danh sách món không được để trống")
    private List<OrderItemRequest> orderItems;
}
