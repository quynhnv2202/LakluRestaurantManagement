package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.OrderPolicy;
import com.laklu.pos.auth.policies.ReservationPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewOrderRequest;
import com.laklu.pos.dataObjects.request.ReservationRequest;
import com.laklu.pos.dataObjects.request.TableOrderRequest;
import com.laklu.pos.dataObjects.response.*;
import com.laklu.pos.entities.*;
import com.laklu.pos.exceptions.httpExceptions.BadRequestException;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.OrderMapper;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.services.OrderService;
import com.laklu.pos.services.ReservationService;
import com.laklu.pos.services.ScheduleService;
import com.laklu.pos.services.TableService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.UserMustHaveScheduleAndAttendance;
import com.laklu.pos.valueObjects.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/table-order")
@Tag(name = "Table Order Controller", description = "Quản lý đặt bàn và order cùng lúc")
@AllArgsConstructor
public class TableOrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderPolicy orderPolicy;
    private final ReservationService reservationService;
    private final ReservationPolicy reservationPolicy;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final TableService tableService;

    @Operation(summary = "Tạo đặt bàn và order cùng lúc", description = "API này dùng để tạo đặt bàn và order cùng lúc")
    @PostMapping("/")
    @Transactional
    public ApiResponseEntity createTableOrder(
            @Valid @RequestBody TableOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws Exception {
        
        // Kiểm tra quyền
        Ultis.throwUnless(orderPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        
        // Lấy thông tin staff từ người dùng đăng nhập
        User staff = userPrincipal.getPersitentUser();

        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        
        // Lấy danh sách bàn
        List<Table> tables = tableService.getAllTables().stream()
                .filter(table -> request.getTableIds().contains(table.getId()))
                .toList();
        
        
        // Tính tổng dung lượng bàn
        int totalCapacity = tables.stream().mapToInt(Table::getCapacity).sum();
        
        // Kiểm tra số lượng người có phù hợp với tổng dung lượng bàn
        if (request.getNumberOfPeople() > totalCapacity) {
            // Thay vì ném exception, đặt số người bằng với tổng dung lượng bàn
            request.setNumberOfPeople(totalCapacity);
        }
        
        // Tạo request cho Reservation
        LocalDateTime now = LocalDateTime.now();
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .reservationTime(now)
                .checkIn(now)
                .tableIds(request.getTableIds())
                .numberOfPeople(request.getNumberOfPeople())
                .build();
        
        // Tạo reservation
        Reservation reservation = reservationService.createReservation(reservationRequest);
        reservationService.updateReservationStatus(reservation, Reservation.Status.CONFIRMED);
        
        // Tạo request cho Order
        NewOrderRequest newOrderRequest = new NewOrderRequest();
        newOrderRequest.setReservationId(reservation.getId());
        newOrderRequest.setOrderItems(request.getOrderItems());
        
        // Tạo order
        Order order = orderService.createOrder(newOrderRequest, staff);
        // Chuyển đổi sang OrderResponseDTO
        List<TableInfo> tableInfos = tables.stream()
                .map(table -> TableInfo.builder()
                        .id(table.getId())
                        .tableNumber(table.getTableNumber())
                        .build())
                .collect(Collectors.toList());


        OrderResponseDTO responseDTO = new OrderResponseDTO(
                order.getId(),
                order.getReservation().getId(),
                order.getStaff().getId(),
                order.getStatus().getLabel(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                tableInfos,
                orderMapper.toOrderResponse(order).getOrderItems()
        );
        
        return ApiResponseEntity.success(responseDTO);
    }
} 