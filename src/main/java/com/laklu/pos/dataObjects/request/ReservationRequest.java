package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationRequest {
    @NotNull(message = "Không được để trống")
    @Size(min = 3, max = 100, message = "Tên phải nằm trong khoảng từ 3 đến 100 kí tự")
    String customerName;

    @NotNull(message = "Không được bỏ trống")
    @Size(min = 10, max = 15, message = "Số điện thoại gồm 10 chữ số")
    String customerPhone;

    LocalDateTime reservationTime;

    @NotNull(message = "Không được bỏ trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @FutureOrPresent(message = "Thời gian check-in phải là hiện tại hoặc tương lai")
    LocalDateTime checkIn;

    @NotEmpty(message = "Hãy chọn bàn")
    List<Integer> tableIds;
    private Integer numberOfPeople;
}