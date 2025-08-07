package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MergeOrderRequest {
    @NotNull(message = "Danh sách Order ID khong duoc de trong")
    private List<Integer> orderIds;
    @NotNull(message = "Reservation ID khong duoc de trong")
    private Integer reservationId;
}
