package com.laklu.pos.validator;

import com.laklu.pos.entities.Reservation;
import com.laklu.pos.services.ReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Rule kiểm tra khả năng hủy đặt bàn dựa trên trạng thái của đặt bàn và các điều kiện khác
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationCancellationRule implements Rule {

    final Reservation reservation;
    final ReservationService reservationService;
    
    private String errorMessage = "";

    @Override
    public String field() {
        return "reservation.status";
    }

    @Override
    public boolean isValid() {
        // Kiểm tra trạng thái đặt bàn đã hoàn thành
        if (reservation.getStatus() == Reservation.Status.COMPLETED) {
            errorMessage = "Không thể hủy đặt bàn đã hoàn thành";
            return false;
        }
        
        // Kiểm tra trạng thái đặt bàn đã bị hủy trước đó
        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            errorMessage = "Đặt bàn đã được hủy trước đó";
            return false;
        }
        
        // Nếu đặt bàn đã được xác nhận, kiểm tra xem có đơn hàng nào không
        if (reservation.getStatus() == Reservation.Status.CONFIRMED) {
            // Kiểm tra xem có đơn hàng active nào không
            if (reservationService.hasActiveOrders(reservation)) {
                errorMessage = "Không thể hủy đặt bàn đã xác nhận và có đơn hàng";
                return false;
            }
        }
        
        return true;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
} 