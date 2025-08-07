package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.ReservationPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.ReservationTableUpdateDTO;
import com.laklu.pos.dataObjects.request.TransferTableRequest;
import com.laklu.pos.dataObjects.request.UpdateReservationDTO;
import com.laklu.pos.dataObjects.response.PageResponse;
import com.laklu.pos.dataObjects.response.ReservationResponse;
import com.laklu.pos.dataObjects.response.ScheduleDetailDTO;
import com.laklu.pos.dataObjects.response.TableInfo;
import com.laklu.pos.entities.*;
import com.laklu.pos.dataObjects.request.ReservationRequest;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.repositories.TableRepository;
import com.laklu.pos.services.ReservationService;
import com.laklu.pos.services.ScheduleService;
import com.laklu.pos.services.UserService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.UserMustHaveScheduleAndAttendance;
import com.laklu.pos.validator.ValidationRule;
import com.laklu.pos.validator.ReservationMustHaveAtLeastOneTable;
import com.laklu.pos.validator.ReservationCancellationRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation Controller", description = "Quản lý thông tin đặt bàn")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReservationController {

    ReservationService reservationService;
    ReservationPolicy reservationPolicy;
    TableRepository tableRepository;
    ScheduleRepository scheduleRepository;
    ScheduleService scheduleService;
    private final UserService userService;

    @Operation(summary = "Tạo đặt bàn", description = "API này dùng để thu thập thông tin khách hàng")
    @PostMapping("/")
    public ApiResponseEntity store(@Valid @RequestBody ReservationRequest request) throws Exception {
        Ultis.throwUnless(reservationPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        request.setReservationTime(LocalDateTime.now());

        List<Table> tables = tableRepository.findAllById(request.getTableIds());
        // get table seat capacity
        Integer totalSeat = tables.stream().map(Table::getCapacity).reduce(0, Integer::sum);
        var peopleMustBeSuitable = new ValidationRule(
                (v) -> request.getNumberOfPeople() <= totalSeat,
                "",
                "Số người không được vượt quá số chỗ của bàn"
        );

        RuleValidator.validate(peopleMustBeSuitable);

        Reservation reservation = reservationService.createReservation(request);

        return ApiResponseEntity.success(reservation);
    }

    @Operation(summary = "Cập nhật thông tin đặt bàn", description = "API này dùng để cập nhật thông tin khách hàng, số điện thoại, giờ check-in và số người")
    @PutMapping("/{id}")
    public ApiResponseEntity update(@PathVariable Integer id, @Valid @RequestBody UpdateReservationDTO request) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);

        Ultis.throwUnless(reservationPolicy.canEdit(JwtGuard.userPrincipal(), reservation), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        // Kiểm tra xem số người có phù hợp với số chỗ của các bàn hiện tại không
        List<Table> tables = reservation.getReservationTables().stream()
                .map(ReservationTable::getTable)
                .collect(Collectors.toList());
        
        Integer totalSeat = tables.stream().map(Table::getCapacity).reduce(0, Integer::sum);
        var peopleMustBeSuitable = new ValidationRule(
                (v) -> request.getNumberOfPeople() <= totalSeat,
                "",
                "Số người không được vượt quá số chỗ của bàn"
        );

        RuleValidator.validate(peopleMustBeSuitable);

        Reservation updatedReservation = reservationService.updateReservationInfo(id, request);

        return ApiResponseEntity.success(convertToCalendarResponse(updatedReservation));
    }

    @Operation(summary = "Thêm bàn vào đặt bàn", description = "API này dùng để thêm bàn vào đặt bàn")
    @PostMapping("/{reservation_id}/tables")
    public ApiResponseEntity update(@PathVariable("reservation_id") Integer id, @Valid @RequestBody ReservationTableUpdateDTO request) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);

        Ultis.throwUnless(reservationPolicy.canEdit(JwtGuard.userPrincipal(), reservation), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        Reservation updatedReservation = reservationService.addTablesToReservation(reservation, request.getTableIds());

        return ApiResponseEntity.success("Thêm bàn đặt thành công");
    }

    @Operation(summary = "Xóa bàn khỏi đặt bàn", description = "API này dùng để xóa bàn khỏi đặt bàn")
    @DeleteMapping("/{reservation_id}/tables")
    public ApiResponseEntity deleteTables(@PathVariable("reservation_id") Integer id, @Valid @RequestBody ReservationTableUpdateDTO request) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);

        Ultis.throwUnless(reservationPolicy.canEdit(JwtGuard.userPrincipal(), reservation), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        // Kiểm tra xem có còn ít nhất 1 bàn sau khi xóa không
        RuleValidator.validate(new ReservationMustHaveAtLeastOneTable(reservation, request.getTableIds()));

        // Lấy tất cả các bàn hiện tại của đặt bàn
        List<Table> currentTables = reservation.getReservationTables().stream()
                .map(ReservationTable::getTable)
                .collect(Collectors.toList());
        
        // Lấy các bàn sẽ bị xóa
        List<Table> tablesToRemove = tableRepository.findAllById(request.getTableIds());
        
        // Tính toán các bàn còn lại sau khi xóa
        List<Table> remainingTables = new ArrayList<>(currentTables);
        remainingTables.removeAll(tablesToRemove);
        
        // Tính tổng sức chứa của các bàn còn lại
        Integer remainingSeatCapacity = remainingTables.stream()
                .map(Table::getCapacity)
                .reduce(0, Integer::sum);
        
        // Kiểm tra xem sức chứa còn lại có đủ cho số người đã đặt không
        var capacityMustBeSufficient = new ValidationRule(
                (v) -> remainingSeatCapacity >= reservation.getNumberOfPeople(),
                "",
                "Không thể xóa bàn vì sức chứa còn lại không đủ cho số người đã đặt"
        );
        
        RuleValidator.validate(capacityMustBeSufficient);

        reservationService.deleteTablesReservation(reservation, request.getTableIds());

        return ApiResponseEntity.success("Xóa bàn đặt thành công");
    }

    @Operation(summary = "Lấy danh sách đặt bàn", description = "API này dùng để lấy danh sách đặt bàn với phân trang")
    @GetMapping("/")
    public ApiResponseEntity index(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {
        Ultis.throwUnless(reservationPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        Page<Reservation> reservationPage = reservationService.getAllReservation(page, size);

        List<CalendarResponseDTO<Integer, ReservationResponse>> calendarResponses = reservationPage.getContent().stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());
        
        PageResponse<CalendarResponseDTO<Integer, ReservationResponse>> pageResponse = 
                PageResponse.fromPage(reservationPage, calendarResponses);

        return ApiResponseEntity.success(pageResponse);
    }

    @Operation(summary = "Lấy thông tin đặt bàn theo ID", description = "API này dùng để lấy thông tin đặt bàn theo ID")
    @GetMapping("/{id}")
    public ApiResponseEntity getReservationById(@PathVariable Integer id) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);
        Ultis.throwUnless(reservationPolicy.canView(JwtGuard.userPrincipal(),reservation), new ForbiddenException());
        return ApiResponseEntity.success(convertToCalendarResponse(reservation));
    }

    @Operation(summary = "Hủy đặt bàn", description = "API này dùng để hủy đặt bàn, chuyển trạng thái sang CANCELLED")
    @PostMapping("/{id}/cancel")
    public ApiResponseEntity cancelReservation(@PathVariable Integer id) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);
        
        Ultis.throwUnless(reservationPolicy.canEdit(JwtGuard.userPrincipal(), reservation), new ForbiddenException());

        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        
        // Áp dụng rule mới kiểm tra khả năng hủy đặt bàn
        ReservationCancellationRule cancellationRule = new ReservationCancellationRule(reservation, reservationService);
        RuleValidator.validate(cancellationRule);
     
        // Cập nhật trạng thái đặt bàn sang CANCELLED
        reservationService.updateReservationStatus(reservation, Reservation.Status.CANCELLED);
        
        return ApiResponseEntity.success("Hủy đặt bàn thành công");
    }

    @Operation(summary = "Lọc đặt bàn theo ngày và trạng thái", description = "API này dùng để lọc đặt bàn theo ngày và trạng thái với phân trang")
    @GetMapping("/filter")
    public ApiResponseEntity filterReservations(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Reservation.Status status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {
        
        Ultis.throwUnless(reservationPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        Page<Reservation> reservationPage;
        
        if (date != null && status != null) {
            // Lọc theo cả ngày và trạng thái
            LocalDateTime startOfDay = LocalDate.parse(date).atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
            
            reservationPage = reservationService.findByDateAndStatus(startOfDay, endOfDay, status, page, size);
        } else if (date != null) {
            // Chỉ lọc theo ngày
            LocalDateTime startOfDay = LocalDate.parse(date).atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
            
            reservationPage = reservationService.findByDate(startOfDay, endOfDay, page, size);
        } else if (status != null) {
            // Chỉ lọc theo trạng thái
            reservationPage = reservationService.findByStatus(status, page, size);
        } else {
            // Không có điều kiện lọc
            reservationPage = reservationService.getAllReservation(page, size);
        }
        
        List<CalendarResponseDTO<Integer, ReservationResponse>> calendarResponses = reservationPage.getContent().stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());
        
        PageResponse<CalendarResponseDTO<Integer, ReservationResponse>> pageResponse = 
                PageResponse.fromPage(reservationPage, calendarResponses);
        
        return ApiResponseEntity.success(pageResponse);
    }

    @Operation(summary = "Tìm kiếm đặt bàn", description = "API này dùng để tìm kiếm đặt bàn theo tên khách hàng hoặc số điện thoại với phân trang")
    @GetMapping("/search")
    public ApiResponseEntity searchReservations(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {
        Ultis.throwUnless(reservationPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        Page<Reservation> reservationPage = reservationService.searchByNameOrPhone(keyword, page, size);
        
        List<CalendarResponseDTO<Integer, ReservationResponse>> calendarResponses = reservationPage.getContent().stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());
        
        PageResponse<CalendarResponseDTO<Integer, ReservationResponse>> pageResponse = 
                PageResponse.fromPage(reservationPage, calendarResponses);
        
        return ApiResponseEntity.success(pageResponse);
    }

    @Operation(summary = "Lọc đặt bàn theo khoảng thời gian", description = "API này dùng để lọc đặt bàn theo các khoảng thời gian: hôm nay, hôm qua, tuần này, tháng này, ca tối (4h chiều đến 3h sáng)")
    @GetMapping("/time-range")
    public ApiResponseEntity getReservationsByTimeRange(
            @RequestParam(required = true) String timeRange,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {
        
        Ultis.throwUnless(reservationPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        Page<Reservation> reservationPage;
        
        switch (timeRange.toLowerCase()) {
            case "today":
                reservationPage = reservationService.findToday(page, size);
                break;
            case "yesterday":
                reservationPage = reservationService.findYesterday(page, size);
                break;
            case "week":
                reservationPage = reservationService.findThisWeek(page, size);
                break;
            case "month":
                reservationPage = reservationService.findThisMonth(page, size);
                break;
            case "all":
                reservationPage = reservationService.getAllReservation(page, size);
                break;
            default:
                // Mặc định là hôm nay
                reservationPage = reservationService.findToday(page, size);
                break;
        }
        
        List<CalendarResponseDTO<Integer, ReservationResponse>> calendarResponses = reservationPage.getContent().stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());
        
        PageResponse<CalendarResponseDTO<Integer, ReservationResponse>> pageResponse = 
                PageResponse.fromPage(reservationPage, calendarResponses);
        
        return ApiResponseEntity.success(pageResponse);
    }

    @Operation(summary = "Lọc đặt bàn hoạt động theo khoảng thời gian", description = "API này dùng để lọc đặt bàn hoạt động theo các khoảng thời gian: hôm nay, hôm qua, tuần này, tháng này, ca tối (4h chiều đến 3h sáng)")
    @GetMapping("/time-range/active")
    public ApiResponseEntity getActiveReservationsByTimeRange(
            @RequestParam(required = true) String timeRange,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {

        Ultis.throwUnless(reservationPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        Page<Reservation> reservationPage;

        switch (timeRange.toLowerCase()) {
            case "today":
                reservationPage = reservationService.findEveningToEarlyMorningActiveReservations(page, size);
                break;
            case "yesterday":
                reservationPage = reservationService.findYesterday(page, size);
                break;
            case "week":
                reservationPage = reservationService.findThisWeek(page, size);
                break;
            case "month":
                reservationPage = reservationService.findThisMonth(page, size);
                break;
            case "all":
                reservationPage = reservationService.getAllReservation(page, size);
                break;
            default:
                // Mặc định là hôm nay
                reservationPage = reservationService.findEveningToEarlyMorningActiveReservations(page, size);
                break;
        }

        List<CalendarResponseDTO<Integer, ReservationResponse>> calendarResponses = reservationPage.getContent().stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());

        PageResponse<CalendarResponseDTO<Integer, ReservationResponse>> pageResponse =
                PageResponse.fromPage(reservationPage, calendarResponses);

        return ApiResponseEntity.success(pageResponse);
    }

    @Operation(summary = "Chuyển bàn", description = "API này dùng để chuyển bàn từ đặt bàn này sang đặt bàn khác")
    @PostMapping("/{reservation_id}/transfer-tables")
    public ApiResponseEntity transferTables(
            @PathVariable("reservation_id") Integer id,
            @Valid @RequestBody TransferTableRequest request) throws Exception {
        Reservation reservation = reservationService.findOrFail(id);

        Ultis.throwUnless(reservationPolicy.canEdit(JwtGuard.userPrincipal(), reservation), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        // Kiểm tra xem các bàn cần chuyển có thuộc đặt bàn này không
        boolean allTablesBelongToReservation = request.getFromTableIds().stream()
                .allMatch(tableId -> reservation.getReservationTables().stream()
                        .anyMatch(rt -> rt.getTable().getId().equals(tableId)));
        
        if (!allTablesBelongToReservation) {
            return ApiResponseEntity.success("Một hoặc nhiều bàn không thuộc đặt bàn này");
        }
        
        // Lấy tất cả các bàn hiện tại của đặt bàn
        List<Table> currentTables = reservation.getReservationTables().stream()
                .map(ReservationTable::getTable)
                .collect(Collectors.toList());
        
        // Lấy các bàn sẽ bị xóa và các bàn sẽ được thêm vào
        List<Table> tablesToRemove = tableRepository.findAllById(request.getFromTableIds());
        List<Table> tablesToAdd = tableRepository.findAllById(request.getToTableIds());
        
        // Tính toán các bàn sau khi chuyển
        List<Table> resultingTables = new ArrayList<>(currentTables);
        resultingTables.removeAll(tablesToRemove);
        resultingTables.addAll(tablesToAdd);
        
        // Tính tổng sức chứa sau khi chuyển bàn
        Integer resultingSeatCapacity = resultingTables.stream()
                .map(Table::getCapacity)
                .reduce(0, Integer::sum);
        
        // Kiểm tra xem sức chứa sau khi chuyển có đủ cho số người đã đặt không
        var capacityMustBeSufficient = new ValidationRule(
                (v) -> resultingSeatCapacity >= reservation.getNumberOfPeople(),
                "",
                "Không thể chuyển bàn vì sức chứa sau khi chuyển không đủ cho số người đã đặt"
        );
        
        RuleValidator.validate(capacityMustBeSufficient);

        reservationService.transferTables(reservation, request.getFromTableIds(), request.getToTableIds());

        return ApiResponseEntity.success("Chuyển bàn thành công");
    }

    private CalendarResponseDTO<Integer, ReservationResponse> convertToCalendarResponse(Reservation reservation) {
        try {
            String createBy = userService.findUserById(reservation.getUserId())
                    .map(User::getUsername)
                    .orElse("Unknown");

            List<TableInfo> tables = reservation.getReservationTables().stream()
                    .map(reservationTable -> TableInfo.builder()
                            .id(reservationTable.getTable().getId())
                            .tableNumber(reservationTable.getTable().getTableNumber())
                            .build())
                    .collect(Collectors.toList());

            ReservationResponse detail = ReservationResponse.builder()
                    .id(reservation.getId())
                    .createBy(createBy)
                    .customerName(reservation.getCustomerName())
                    .customerPhone(reservation.getCustomerPhone())
                    .status(reservation.getStatus())
                    .tables(tables)
                    .numberOfPeople(reservation.getNumberOfPeople())
                    .checkIn(reservation.getCheckIn())
                    .checkOut(reservation.getCheckOut())
                    .build();

            return new CalendarResponseDTO<>(detail);
        } catch (Exception e) {
            throw e; // Re-throw or return a custom error response
        }
    }

}