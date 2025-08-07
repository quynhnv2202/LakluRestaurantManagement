package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReservationDTO {
    @Size(min = 3, max = 100, message = "Tên phải nằm trong khoảng từ 3 đến 100 kí tự")
    private String customerName;

    @Size(min = 10, max = 15, message = "Số điện thoại gồm 10 chữ số")
    private String customerPhone;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @FutureOrPresent(message = "Thời gian check-in phải là hiện tại hoặc tương lai")
    private LocalDateTime checkIn;

    @Min(value = 1, message = "Số lượng người phải lớn hơn 0")
    private Integer numberOfPeople;
}
