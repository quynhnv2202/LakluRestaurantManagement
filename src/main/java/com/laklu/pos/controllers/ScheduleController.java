package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.SchedulePolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewSchedule;
import com.laklu.pos.dataObjects.request.ScheduleCheckInRequest;
import com.laklu.pos.dataObjects.request.ScheduleCheckOutRequest;
import com.laklu.pos.dataObjects.response.ScheduleDetailDTO;
import com.laklu.pos.dataObjects.response.ScheduleResponse;
import com.laklu.pos.entities.CalendarResponseDTO;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.ScheduleUser;
import com.laklu.pos.entities.User;
import com.laklu.pos.entities.Attendance;
import com.laklu.pos.exceptions.RuleNotValidException;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.AttendanceRepository;
import com.laklu.pos.repositories.CashRegisterRepository;
import com.laklu.pos.repositories.ProfileRepository;
import com.laklu.pos.services.ScheduleService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.ScheduleCanBeDeleted;
import com.laklu.pos.validator.ScheduleMinimumFourStaff;
import com.laklu.pos.validator.ScheduleMustHaveOneManager;
import com.laklu.pos.validator.ScheduleMustMatchToday;
import com.laklu.pos.validator.ScheduleTimeMustBePresentOrFuture;
import com.laklu.pos.validator.ShiftEndAfterShiftStart;
import com.laklu.pos.validator.ValidShiftTypeForTime;
import com.laklu.pos.validator.AutoAssignShiftType;
import com.laklu.pos.valueObjects.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/schedule")
@Tag(name = "Schedule Controller", description = "Quản lý lịch làm việc")
public class ScheduleController {

    private final SchedulePolicy schedulePolicy;
    private final ScheduleService scheduleService;
    private final ProfileRepository profileRepository;
    private final AttendanceRepository attendanceRepository;
    private final CashRegisterRepository cashRegisterRepository;

    // Lấy danh sách lịch làm việc
    @Operation(summary = "Lấy danh sách tất cả lịch làm việc", description = "API này dùng để lấy toàn bộ các bạn của quán")
    @GetMapping("/")
    public ApiResponseEntity index() throws Exception {
        Ultis.throwUnless(schedulePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        List<Schedule> schedules = scheduleService.getAllSchedules();
        List<CalendarResponseDTO<Long, ScheduleDetailDTO>> calendarResponses = schedules.stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(calendarResponses);
    }

    // Tạo mới lịch làm việc
    @Operation(summary = "Tạo lịch làm việc", description = "API này dùng để tạo lịch làm việc mới")
    @PostMapping("/")
    public ApiResponseEntity store(@RequestBody @Validated NewSchedule newSchedule) throws Exception {
        Ultis.throwUnless(schedulePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());

        // Kiểm tra thời gian phải là hiện tại hoặc tương lai
        RuleValidator.validate(new ScheduleTimeMustBePresentOrFuture(newSchedule));
        
        // Tự động gán ShiftType nếu chưa được chọn
        RuleValidator.validate(new AutoAssignShiftType(newSchedule));
        
        // Kiểm tra số lượng nhân viên tối thiểu là 4
        RuleValidator.validate(new ScheduleMinimumFourStaff(newSchedule));
        
        RuleValidator.validate(new ScheduleMustHaveOneManager(newSchedule));
        RuleValidator.validate(new ShiftEndAfterShiftStart(newSchedule));
        // Kiểm tra xem ShiftType có phù hợp với thời gian bắt đầu ca không
        RuleValidator.validate(new ValidShiftTypeForTime(newSchedule));

        Schedule createdSchedule = scheduleService.storeSchedule(newSchedule);

        return ApiResponseEntity.success(new ScheduleResponse(createdSchedule));
    }

    // Cập nhật lịch làm việc theo ID
    @Operation(summary = "Cập nhật lịch làm việc", description = "API này dùng để cập nhật thông tin lịch làm việc")
    @PutMapping("/{id}")
    public ApiResponseEntity update(@PathVariable Long id, @RequestBody @Validated NewSchedule newSchedule) throws Exception {
        var schedule = scheduleService.findOrFail(id);

        Ultis.throwUnless(schedulePolicy.canEdit(JwtGuard.userPrincipal(), schedule), new ForbiddenException());

        // Tự động gán ShiftType nếu chưa được chọn
        RuleValidator.validate(new AutoAssignShiftType(newSchedule));
        
        // Kiểm tra số lượng nhân viên tối thiểu là 4
        RuleValidator.validate(new ScheduleMinimumFourStaff(newSchedule));
        
        RuleValidator.validate(new ScheduleMustHaveOneManager(newSchedule));
        RuleValidator.validate(new ShiftEndAfterShiftStart(newSchedule));
        // Kiểm tra xem ShiftType có phù hợp với thời gian bắt đầu ca không
        RuleValidator.validate(new ValidShiftTypeForTime(newSchedule));

        Schedule updatedSchedule = scheduleService.editSchedule(schedule, newSchedule);

        return ApiResponseEntity.success(new ScheduleResponse(updatedSchedule));
    }

    // Lấy chi tiết lịch làm việc theo ID
    @Operation(summary = "Hiện thị lịch làm việc theo ID", description = "API này dùng để lấy thông tin lịch làm việc theo ID")
    @GetMapping("/{id}")
    public ApiResponseEntity show(@PathVariable Long id) throws Exception {
        var schedule = scheduleService.findOrFail(id);

        Ultis.throwUnless(schedulePolicy.canView(JwtGuard.userPrincipal(), schedule), new ForbiddenException());

        List<NewSchedule.UserDTO> users = schedule.getScheduleUsers().stream()
                .map(scheduleUsers -> NewSchedule.UserDTO.builder()
                        .staffId(scheduleUsers.getUser().getId())
                        .isManager(scheduleUsers.getIsManager())
                        .build())
                .collect(Collectors.toList());

        NewSchedule scheduleDTO = NewSchedule.builder()
                .user(users)
                .shiftStart(schedule.getShiftStart())
                .shiftEnd(schedule.getShiftEnd())
                .shiftType(schedule.getShiftType())
                .note(schedule.getNote())
                .build();

        return ApiResponseEntity.success(scheduleDTO);
    }
    // Xóa lịch làm việc theo ID
    @Operation(summary = "Xóa lịch làm việc theo ID", description = "API này dùng để xóa thông tin lịch làm việc theo ID")
    @DeleteMapping("/{id}")
    public ApiResponseEntity delete(@PathVariable Long id) throws Exception {
        try {
            var schedule = scheduleService.findOrFail(id);

            // Kiểm tra quyền xóa
            Ultis.throwUnless(schedulePolicy.canDelete(JwtGuard.userPrincipal(), schedule), new ForbiddenException());
            
            // Kiểm tra điều kiện xóa
            ScheduleCanBeDeleted scheduleCanBeDeleted = new ScheduleCanBeDeleted(schedule, attendanceRepository, cashRegisterRepository);
            RuleValidator.validate(scheduleCanBeDeleted);

            scheduleService.deleteSchedule(id);

            return ApiResponseEntity.success("Xóa lịch làm việc thành công");
        } catch (NotFoundException e) {
            return ApiResponseEntity.exception(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Không tìm thấy lịch làm việc với ID: " + id
            );
        } catch (ForbiddenException e) {
            return ApiResponseEntity.exception(
                org.springframework.http.HttpStatus.FORBIDDEN,
                "Bạn không có quyền xóa lịch làm việc này"
            );
        } catch (RuleNotValidException e) {
            return ApiResponseEntity.exception(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                ((java.util.HashMap<String, String>) e.getErrors()).values().iterator().next()
            );
        } catch (Exception e) {
            return ApiResponseEntity.exception(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể xóa lịch làm việc: " + e.getMessage()
            );
        }
    }

    // Tạo mã QR điểm danh
    @Operation(summary = "Tạo mã qr check in code", description = "API này dùng để tạo mã QR điểm danh")
    @PostMapping(produces = MediaType.IMAGE_PNG_VALUE, value = "/check-in-qr-code/{id}")
    public ResponseEntity<byte[]> generateQRCheckInCode(@PathVariable("id") long id) throws Exception {
        var schedule = scheduleService.findOrFail(id);
        ScheduleMustMatchToday scheduleMustMatchToday = new ScheduleMustMatchToday(schedule, scheduleService);
        RuleValidator.validate(scheduleMustMatchToday);
        Ultis.throwUnless(schedulePolicy.canCreateCheckInQrCode(JwtGuard.userPrincipal()), new ForbiddenException());
        return ResponseEntity.ok(scheduleService.generateCheckInCode(schedule));
    }

    @Operation(summary = "Tạo mã qr check out code", description = "API này dùng để tạo mã QR điểm danh check out")
    @PostMapping(produces = MediaType.IMAGE_PNG_VALUE, value = "/check-out-qr-code/{id}")
    public ResponseEntity<byte[]>  generateQRCheckOutCode(@PathVariable("id") long id) throws Exception {
        var schedule = scheduleService.findOrFail(id);
        ScheduleMustMatchToday scheduleMustMatchToday = new ScheduleMustMatchToday(schedule, scheduleService);
        RuleValidator.validate(scheduleMustMatchToday);
        Ultis.throwUnless(schedulePolicy.canCreateCheckOutQrCode(JwtGuard.userPrincipal()), new ForbiddenException());
        return ResponseEntity.ok(scheduleService.generateCheckOutCode(schedule));
    }

    @Operation(summary = "Check in vào ca làm việc", description = "API này dùng để check in vào ca làm việc")
    @PostMapping("/schedule-check-in")
    public ApiResponseEntity scheduleCheckIn(@RequestBody ScheduleCheckInRequest request) throws Exception {
        Schedule schedule = scheduleService.findOrFail(request.getScheduleId());
        UserPrincipal userPrincipal = scheduleService.getScheduleUser(request.getUsername(), request.getPassword());

        // Kiểm tra quyền check in
        Ultis.throwUnless(schedulePolicy.canCheckIn(userPrincipal, schedule), new ForbiddenException());

        boolean alreadyCheckedIn = scheduleService.hasCheckedIn(schedule, userPrincipal.getPersitentUser());
        if (alreadyCheckedIn) {
            // If already checked in, throw an exception
            return ApiResponseEntity.success("Bạn đã điểm danh rồi.");
        }

        scheduleService.validateCheckInCode(String.valueOf(request.getScheduleId()), request.getExpiry(), request.getSignature());

        // Kiểm tra thời gian hiện tại có sớm hơn thời gian bắt đầu ca không
        LocalTime currentTime = Ultis.getCurrentTime();
        LocalTime scheduleStartTime = schedule.getShiftStart().toLocalTime();
        
        scheduleService.createCheckInAttendance(schedule, userPrincipal.getPersitentUser());
        
        if (currentTime.isBefore(scheduleStartTime)) {
            // Nếu check in sớm, thông báo cho người dùng
            return ApiResponseEntity.success("Check in thành công. Bạn đã check in sớm, thời gian check in được ghi nhận là " + scheduleStartTime);
        } else {
            return ApiResponseEntity.success("Check in thành công");
        }
    }

    @Operation(summary = "Check out khỏi ca làm việc", description = "API này dùng để check out khỏi ca làm việc")
    @PostMapping("/schedule-check-out")
    public ApiResponseEntity scheduleCheckOut(@RequestBody ScheduleCheckOutRequest request) throws Exception {
        Schedule schedule = scheduleService.findOrFail(request.getScheduleId());
        UserPrincipal userPrincipal = scheduleService.getScheduleUser(request.getUsername(), request.getPassword());
        // Kiểm tra quyền check in
        Ultis.throwUnless(schedulePolicy.canCheckOut(userPrincipal, schedule), new ForbiddenException());
        scheduleService.validateCheckOutCode(String.valueOf(request.getScheduleId()), request.getExpiry(), request.getSignature());
        scheduleService.checkOutAttendance(schedule, userPrincipal.getPersitentUser());

        return ApiResponseEntity.success("Check out thành công");
    }

    // Lấy lịch làm việc theo ID của nhân viên
    @Operation(summary = "Lấy lịch làm việc theo ID nhân viên", description = "API này dùng để lấy danh sách lịch làm việc của một nhân viên cụ thể")
    @GetMapping("/staff/{staffId}")
    public ApiResponseEntity getSchedulesByStaffId(@PathVariable Integer staffId) throws Exception {
        Ultis.throwUnless(schedulePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        List<Schedule> schedules = scheduleService.getSchedulesByStaffId(staffId);
        List<CalendarResponseDTO<Long, ScheduleDetailDTO>> calendarResponses = schedules.stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(calendarResponses);
    }

    @Operation(summary = "Lấy lịch làm việc theo khoảng thời gian", 
              description = "API này dùng để lấy danh sách lịch làm việc từ ngày bắt đầu đến ngày kết thúc")
    @GetMapping("/by-date-range")
    public ApiResponseEntity getSchedulesByDateRange(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) throws Exception {
        
        Ultis.throwUnless(schedulePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        List<Schedule> schedules = scheduleService.getSchedulesByDateRange(startDate, endDate);
        List<CalendarResponseDTO<Long, ScheduleDetailDTO>> calendarResponses = schedules.stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(calendarResponses);
    }

    @Operation(summary = "Lấy lịch làm việc theo ID user và khoảng thời gian",
            description = "API này dùng để lấy danh sách lịch làm việc của một nhân viên trong khoảng thời gian cụ thể")
    @GetMapping("/staff/{staffId}/date-range")
    public ApiResponseEntity getSchedulesByStaffIdAndDateRange(
            @PathVariable Integer staffId,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("endDate") String endDateStr) throws Exception {

        Ultis.throwUnless(schedulePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        List<Schedule> schedules = scheduleService.getSchedulesByUserIdAndDateRange(staffId, startDate, endDate);

        List<CalendarResponseDTO<Long, ScheduleDetailDTO>> calendarResponses = schedules.stream()
                .map(this::convertToCalendarResponse)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(calendarResponses);
    }

    @Operation(summary = "Clone lịch làm việc", 
            description = "API này dùng để tạo bản sao của một lịch làm việc đã tồn tại với ngày làm việc mới")
    @PostMapping("/{id}/clone")
    public ApiResponseEntity cloneSchedule(
            @PathVariable Long id, 
            @RequestParam("newDate") @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate newDate,
            @RequestParam(value = "updateShiftType", defaultValue = "true") boolean updateShiftType) throws Exception {
        
        // Lấy lịch làm việc cần clone
        Schedule existingSchedule = scheduleService.findOrFail(id);
        
        // Kiểm tra quyền tạo lịch làm việc
        Ultis.throwUnless(schedulePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        
        // Clone lịch làm việc với ngày mới
        Schedule clonedSchedule = scheduleService.cloneSchedule(existingSchedule, newDate, updateShiftType);
        
        return ApiResponseEntity.success(new ScheduleResponse(clonedSchedule), "Clone lịch làm việc thành công");
    }

    @Operation(summary = "Clone lịch làm việc của tuần trước",
            description = "API này dùng để tạo bản sao của tất cả lịch làm việc trong tuần trước sang tuần hiện tại")
    @PostMapping("/clone-previous-week")
    public ApiResponseEntity clonePreviousWeekSchedules(
            @RequestParam(value = "currentWeekStart", required = false) 
                @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate currentWeekStart,
            @RequestParam(value = "updateShiftType", defaultValue = "true") boolean updateShiftType) throws Exception {
        
        // Kiểm tra quyền tạo lịch làm việc
        Ultis.throwUnless(schedulePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        
        // Nếu không cung cấp ngày bắt đầu của tuần hiện tại, lấy ngày thứ 2 của tuần hiện tại
        if (currentWeekStart == null) {
            LocalDate today = LocalDate.now();
            int daysToSubtract = today.getDayOfWeek().getValue() - 1; // Thứ 2 có giá trị 1
            currentWeekStart = today.minusDays(daysToSubtract);
        }
        
        // Clone lịch làm việc từ tuần trước
        List<Schedule> clonedSchedules = scheduleService.clonePreviousWeekSchedules(currentWeekStart, updateShiftType);
        
        // Chuyển đổi danh sách lịch làm việc sang response
        List<ScheduleResponse> responses = clonedSchedules.stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
        
        return ApiResponseEntity.success(responses, "Đã clone " + clonedSchedules.size() + " lịch làm việc từ tuần trước");
    }

    @Operation(summary = "Clone lịch làm việc sang tuần sau",
            description = "API này dùng để tạo bản sao của tất cả lịch làm việc từ tuần đã chọn sang tuần tiếp theo")
    @PostMapping("/clone-to-next-week")
    public ApiResponseEntity cloneToNextWeek(
            @RequestParam("selectedWeek") @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate selectedWeekStart,
            @RequestParam(value = "updateShiftType", defaultValue = "true") boolean updateShiftType,
            @RequestParam(value = "overwriteExisting", defaultValue = "false") boolean overwriteExisting) throws Exception {
        
        // Kiểm tra quyền tạo lịch làm việc
        Ultis.throwUnless(schedulePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        
        // Chuẩn hóa lại ngày bắt đầu của tuần để đảm bảo là ngày thứ 2
        int dayOfWeek = selectedWeekStart.getDayOfWeek().getValue(); // Thứ 2 có giá trị 1
        if (dayOfWeek != 1) {
            // Nếu không phải thứ 2, điều chỉnh về ngày thứ 2 của tuần đó
            selectedWeekStart = selectedWeekStart.minusDays(dayOfWeek - 1);
        }
        
        // Tính ngày bắt đầu tuần tiếp theo
        LocalDate nextWeekStart = selectedWeekStart.plusDays(7);
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
        
        // Kiểm tra xem đã có lịch làm việc trong tuần tiếp theo chưa
        boolean hasExistingSchedules = scheduleService.hasSchedulesInDateRange(nextWeekStart, nextWeekEnd);
        
        // Nếu đã có lịch làm việc và không được ghi đè, trả về thông báo lỗi
        if (hasExistingSchedules && !overwriteExisting) {
            // Định dạng ngày để hiển thị thông báo
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String nextWeekStartStr = nextWeekStart.format(formatter);
            String nextWeekEndStr = nextWeekEnd.format(formatter);
            
            return ApiResponseEntity.exception(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Đã có lịch làm việc trong tuần " + nextWeekStartStr + " - " + nextWeekEndStr + ". Vui lòng chọn tùy chọn ghi đè nếu muốn thay thế."
            );
        }
        
        // Clone lịch làm việc từ tuần đã chọn sang tuần tiếp theo
        List<Schedule> clonedSchedules = scheduleService.cloneSelectedWeekToNextWeek(selectedWeekStart, updateShiftType, overwriteExisting);
        
        // Định dạng ngày để hiển thị thông báo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String nextWeekStartStr = nextWeekStart.format(formatter);
        String nextWeekEndStr = nextWeekEnd.format(formatter);
        
        // Chuyển đổi danh sách lịch làm việc sang response
        List<ScheduleResponse> responses = clonedSchedules.stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
        
        String message = "Đã clone " + clonedSchedules.size() + " lịch làm việc sang tuần sau (" + nextWeekStartStr + " - " + nextWeekEndStr + ")";
        if (hasExistingSchedules && overwriteExisting) {
            message += " và ghi đè lịch làm việc cũ";
        }
        
        return ApiResponseEntity.success(responses, message);
    }

    @Operation(summary = "Clone lịch làm việc giữa hai tuần cụ thể",
            description = "API này dùng để tạo bản sao của tất cả lịch làm việc từ một tuần nguồn sang một tuần đích cụ thể")
    @PostMapping("/clone-between-weeks")
    public ApiResponseEntity cloneBetweenWeeks(
            @RequestParam("sourceWeek") @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate sourceWeekStart,
            @RequestParam("targetWeek") @DateTimeFormat(pattern="dd/MM/yyyy") LocalDate targetWeekStart,
            @RequestParam(value = "updateShiftType", defaultValue = "true") boolean updateShiftType,
            @RequestParam(value = "overwriteExisting", defaultValue = "false") boolean overwriteExisting) throws Exception {
        
        // Kiểm tra quyền tạo lịch làm việc
        Ultis.throwUnless(schedulePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        
        // Chuẩn hóa ngày bắt đầu của tuần nguồn để đảm bảo là ngày thứ 2
        int sourceDayOfWeek = sourceWeekStart.getDayOfWeek().getValue(); // Thứ 2 có giá trị 1
        if (sourceDayOfWeek != 1) {
            sourceWeekStart = sourceWeekStart.minusDays(sourceDayOfWeek - 1);
        }
        
        // Chuẩn hóa ngày bắt đầu của tuần đích để đảm bảo là ngày thứ 2
        int targetDayOfWeek = targetWeekStart.getDayOfWeek().getValue();
        if (targetDayOfWeek != 1) {
            targetWeekStart = targetWeekStart.minusDays(targetDayOfWeek - 1);
        }
        
        // Tính ngày kết thúc của tuần đích
        LocalDate targetWeekEnd = targetWeekStart.plusDays(6);
        
        // Kiểm tra xem đã có lịch làm việc trong tuần đích chưa
        boolean hasExistingSchedules = scheduleService.hasSchedulesInDateRange(targetWeekStart, targetWeekEnd);
        
        // Nếu đã có lịch làm việc và không được ghi đè, trả về thông báo lỗi
        if (hasExistingSchedules && !overwriteExisting) {
            // Định dạng ngày để hiển thị thông báo
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String targetWeekStartStr = targetWeekStart.format(formatter);
            String targetWeekEndStr = targetWeekEnd.format(formatter);
            
            return ApiResponseEntity.exception(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Đã có lịch làm việc trong tuần " + targetWeekStartStr + " - " + targetWeekEndStr + ". Vui lòng chọn tùy chọn ghi đè nếu muốn thay thế."
            );
        }
        
        // Clone lịch làm việc giữa hai tuần cụ thể
        List<Schedule> clonedSchedules = scheduleService.cloneBetweenSpecificWeeks(
            sourceWeekStart, targetWeekStart, updateShiftType, overwriteExisting);
        
        // Định dạng ngày để hiển thị thông báo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String sourceWeekStartStr = sourceWeekStart.format(formatter);
        String sourceWeekEndStr = sourceWeekStart.plusDays(6).format(formatter);
        String targetWeekStartStr = targetWeekStart.format(formatter);
        String targetWeekEndStr = targetWeekEnd.format(formatter);
        
        // Chuyển đổi danh sách lịch làm việc sang response
        List<ScheduleResponse> responses = clonedSchedules.stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toList());
        
        String message = "Đã clone " + clonedSchedules.size() + " lịch làm việc từ tuần " + 
                         sourceWeekStartStr + " - " + sourceWeekEndStr + " sang tuần " + 
                         targetWeekStartStr + " - " + targetWeekEndStr;
                         
        if (hasExistingSchedules && overwriteExisting) {
            message += " và ghi đè lịch làm việc cũ";
        }
        
        return ApiResponseEntity.success(responses, message);
    }

    // Add this method to ScheduleController
    private CalendarResponseDTO<Long, ScheduleDetailDTO> convertToCalendarResponse(Schedule schedule) {
        List<ScheduleUser> scheduleUsers = schedule.getScheduleUsers();
        
        // Lấy manager profile và fullName
        String managerFullName = scheduleUsers.stream()
                .filter(ScheduleUser::getIsManager)
                .map(scheduleUser -> {
                    Optional<Profile> profile = profileRepository.findByUserId(scheduleUser.getUser().getId());
                    return profile.map(Profile::getFullName).orElse(scheduleUser.getUser().getUsername());
                })
                .findFirst()
                .orElse(null);

        int numberOfStaff = scheduleUsers.size();
        
        // Lấy danh sách fullName của tất cả nhân viên
        List<String> userFullNames = scheduleUsers.stream()
                .map(scheduleUser -> {
                    Optional<Profile> profile = profileRepository.findByUserId(scheduleUser.getUser().getId());
                    return profile.map(Profile::getFullName).orElse(scheduleUser.getUser().getUsername());
                })
                .collect(Collectors.toList());

        String note = schedule.getNote();

        User currentUser = JwtGuard.userPrincipal().getPersitentUser();
        boolean hasAttended = scheduleService.hasCheckedIn(schedule, currentUser);

        // Tạo map lưu trạng thái điểm danh của từng nhân viên với fullName
        Map<String, Boolean> userAttendancesByFullName = scheduleUsers.stream()
                .collect(Collectors.toMap(
                    scheduleUser -> {
                        Optional<Profile> profile = profileRepository.findByUserId(scheduleUser.getUser().getId());
                        return profile.map(Profile::getFullName).orElse(scheduleUser.getUser().getUsername());
                    },
                    scheduleUser -> scheduleService.hasCheckedIn(schedule, scheduleUser.getUser())
                ));

        // Tạo map lưu thông tin clock in/out của từng nhân viên
        Map<String, ScheduleDetailDTO.ClockInOutInfo> userClockInClockOut = scheduleUsers.stream()
                .collect(Collectors.toMap(
                    scheduleUser -> {
                        Optional<Profile> profile = profileRepository.findByUserId(scheduleUser.getUser().getId());
                        return profile.map(Profile::getFullName).orElse(scheduleUser.getUser().getUsername());
                    },
                    scheduleUser -> {
                        Optional<Attendance> attendance = schedule.getAttendances().stream()
                                .filter(a -> a.getStaff().getId().equals(scheduleUser.getUser().getId()))
                                .findFirst();
                        
                        return ScheduleDetailDTO.ClockInOutInfo.builder()
                                .clockIn(attendance.map(Attendance::getClockIn).orElse(null))
                                .clockOut(attendance.map(Attendance::getClockOut).orElse(null))
                                .build();
                    }
                ));

        ScheduleDetailDTO detail = ScheduleDetailDTO.builder()
                .id(schedule.getId())
                .managerFullName(managerFullName)
                .numberOfStaff(numberOfStaff)
                .userFullNames(userFullNames)
                .timeIn(schedule.getShiftStart())
                .timeOut(schedule.getShiftEnd())
                .note(note)
                .attended(String.valueOf(hasAttended))
                .userAttendancesByFullName(userAttendancesByFullName)
                .userClockInClockOut(userClockInClockOut)
                .build();

        return new CalendarResponseDTO<>(detail);
    }
}

