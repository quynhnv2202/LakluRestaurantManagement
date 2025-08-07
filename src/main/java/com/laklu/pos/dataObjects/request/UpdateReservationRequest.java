package com.laklu.pos.dataObjects.request;

import lombok.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReservationRequest {


    @Size(min = 3, max = 100, message = "Tên khách hàng phải từ 3 đến 100 ký tự")
    private String customerName;


    @Size(min = 10, max = 15, message = "Số điện thoại phải từ 10 đến 15 chữ số")
    private String customerPhone;


    private LocalDateTime reservationTime;


    private List<Integer> tableIds;
}
