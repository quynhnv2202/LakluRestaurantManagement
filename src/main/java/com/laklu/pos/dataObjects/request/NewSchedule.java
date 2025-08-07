package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.ShiftType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
@Getter
@Setter
public class NewSchedule {
    @Getter
    @Setter
    @Builder
    public static class UserDTO {
        private Integer staffId;
        private Boolean isManager;
    }

    private List<UserDTO> user;

    @NotNull(message = "Thời gian bắt đầu ca không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime shiftStart; // Giờ bắt đầu ca

    @NotNull(message = "Thời gian kết thúc ca không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime shiftEnd; // Giờ kết thúc ca

    @NotNull(message = "Loại ca không được để trống")
    ShiftType shiftType; // Loại ca

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    String note; // Ghi chú
}