package com.laklu.pos.services;

import com.google.zxing.WriterException;
import com.laklu.pos.dataObjects.ScheduleCheckInCode;
import com.laklu.pos.dataObjects.ScheduleCheckOutCode;
import com.laklu.pos.dataObjects.request.NewSchedule;
import com.laklu.pos.entities.*;
import com.laklu.pos.enums.ShiftType;
import com.laklu.pos.exceptions.httpExceptions.BadRequestException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.exceptions.httpExceptions.UnauthorizedException;
import com.laklu.pos.mapper.ScheduleMapper;
import com.laklu.pos.repositories.AttendanceRepository;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.repositories.ScheduleUserRepository;
import com.laklu.pos.repositories.UserRepository;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.valueObjects.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleUserRepository scheduleUserRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ScheduleMapper scheduleMapper;
    private final QRCodeGenerator qrCodeGenerator;
    private final SignedUrlGenerator signedUrlGenerator;
    private final AuthenticationProvider daoAuthenticationProvider;
    private final AttendanceRepository attendanceRepository;

    @Value("${app.base.attendance-checkin}")
    private String checkInEndpoint;

    @Value("${app.base.attendance-checkout}")
    private String checkOutEndpoint;

    @Value("${app.base.attendance-expire-time}")
    private Long checkCodeExpiry;

    public ScheduleService(ScheduleRepository scheduleRepository, 
                           ScheduleUserRepository scheduleUserRepository,
                           UserRepository userRepository, 
                           UserService userService, 
                           ScheduleMapper scheduleMapper, 
                           QRCodeGenerator qrCodeGenerator, 
                           SignedUrlGenerator signedUrlGenerator, 
                           AuthenticationProvider daoAuthenticationProvider, 
                           AttendanceRepository attendanceRepository) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleUserRepository = scheduleUserRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.scheduleMapper = scheduleMapper;
        this.qrCodeGenerator = qrCodeGenerator;
        this.signedUrlGenerator = signedUrlGenerator;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.attendanceRepository = attendanceRepository;
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> findScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    public Schedule findOrFail(Long id) {
        return this.findScheduleById(id).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Schedule storeSchedule(NewSchedule newSchedule) {
        Schedule schedule = scheduleMapper.toSchedule(newSchedule);
        
        // Lưu schedule trước để đảm bảo có ID
        schedule = scheduleRepository.save(schedule);
        
        final Schedule finalSchedule = schedule;
        List<ScheduleUser> scheduleUsers = newSchedule.getUser().stream()
                .map(userDTO -> {
                    User user = userRepository.findById(userDTO.getStaffId())
                            .orElseThrow(NotFoundException::new);
                    return new ScheduleUser(null, finalSchedule, user, userDTO.getIsManager(), null);
                })
                .collect(Collectors.toList());
        
        // Lưu danh sách ScheduleUser
        scheduleUserRepository.saveAll(scheduleUsers);
        
        // Cập nhật lại danh sách trong đối tượng Schedule
        schedule.setScheduleUsers(scheduleUsers);
        
        return schedule;
    }

    @Transactional
    public Schedule editSchedule(Schedule schedule, NewSchedule newSchedule) {
        scheduleMapper.updateScheduleFromDto(newSchedule, schedule);
        
        // Lưu schedule trước để đảm bảo có ID
        schedule = scheduleRepository.save(schedule);
        
        // Lấy ID của schedule để sử dụng khi xóa và thêm mới ScheduleUser
        final Long scheduleId = schedule.getId();
        
        // Xóa tất cả các ScheduleUser hiện tại
        scheduleUserRepository.deleteAllByScheduleId(scheduleId);
        
        // Tạo danh sách ScheduleUser mới
        final Schedule finalSchedule = schedule;
        List<ScheduleUser> newScheduleUsers = newSchedule.getUser().stream()
                .map(userDTO -> {
                    User user = userRepository.findById(userDTO.getStaffId())
                            .orElseThrow(NotFoundException::new);
                    return new ScheduleUser(null, finalSchedule, user, userDTO.getIsManager(), null);
                })
                .collect(Collectors.toList());
        
        // Lưu danh sách ScheduleUser mới
        scheduleUserRepository.saveAll(newScheduleUsers);
        
        // Cập nhật lại danh sách trong đối tượng Schedule
        schedule.setScheduleUsers(newScheduleUsers);
        
        return schedule;
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = this.findOrFail(id);
        
        // Xóa tất cả ScheduleUser liên quan
        scheduleUserRepository.deleteAllByScheduleId(id);
        
        // Xóa Schedule
        scheduleRepository.delete(schedule);
    }

    public byte[] generateCheckInCode(Schedule schedule) throws IOException, WriterException {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("scheduleId", schedule.getId().toString());
        String link = this.signedUrlGenerator.generateSignedUrl(checkInEndpoint, payload, checkCodeExpiry);
        ScheduleCheckInCode scheduleCheckInCode = new ScheduleCheckInCode(link);
        return this.qrCodeGenerator.getQRCode(scheduleCheckInCode, 200, 200);
    }

    public void validateCheckInCode(String scheduleId, long expiry, String signature) throws Exception {
        HashMap<String, String> signedUrlData = new HashMap<>();
        signedUrlData.put("scheduleId", scheduleId);
        Ultis.throwUnless(this.signedUrlGenerator.isGeneratedSignedUrl(signedUrlData, expiry, signature), new BadRequestException());
    }

    public byte[] generateCheckOutCode(Schedule schedule) throws IOException, WriterException {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("scheduleId", schedule.getId().toString());
        payload.put("checkOut", "true");
        String link = this.signedUrlGenerator.generateSignedUrl(checkOutEndpoint, payload, checkCodeExpiry);
        ScheduleCheckOutCode scheduleCheckInCode = new ScheduleCheckOutCode(link);
        return this.qrCodeGenerator.getQRCode(scheduleCheckInCode, 200, 200);
    }

    public void validateCheckOutCode(String scheduleId, long expiry, String signature) throws Exception {
        HashMap<String, String> signedUrlData = new HashMap<>();
        signedUrlData.put("scheduleId", scheduleId);
        signedUrlData.put("checkOut", "true");
        Ultis.throwUnless(this.signedUrlGenerator.isGeneratedSignedUrl(signedUrlData, expiry, signature), new BadRequestException());
    }

    public UserPrincipal getScheduleUser(String username, String password) throws Exception {
       var result = this.daoAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
       Ultis.throwUnless(result != null, new UnauthorizedException());

       return (UserPrincipal) result.getPrincipal();
    }

    public Attendance createCheckInAttendance(Schedule schedule, User user) {
        Attendance attendance = new Attendance();
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setSchedule(schedule);
        attendance.setStaff(user);
        attendance.setSchedule(schedule);
        
        // Lấy thời gian hiện tại
        LocalTime currentTime = Ultis.getCurrentTime();
        
        // Lấy thời gian bắt đầu ca làm việc
        LocalTime scheduleStartTime = schedule.getShiftStart().toLocalTime();
        
        // Nếu thời gian hiện tại sớm hơn thời gian bắt đầu ca làm việc
        // thì lưu thời gian check in là thời gian bắt đầu ca làm việc
        if (currentTime.isBefore(scheduleStartTime)) {
            attendance.setClockIn(scheduleStartTime);
        } else {
            // Ngược lại, lưu thời gian hiện tại
            attendance.setClockIn(currentTime);
        }
        
        attendance.setStatus(Attendance.Status.PRESENT);

        this.attendanceRepository.save(attendance);
        return attendance;
    }

    public Attendance checkOutAttendance(Schedule schedule, User user) {
        Attendance attendance = this.attendanceRepository.findByScheduleAndStaff(schedule, user).orElseThrow(NotFoundException::new);
        attendance.setClockOut(Ultis.getCurrentTime());
        attendance.setStatus(Attendance.Status.PRESENT);
        this.attendanceRepository.save(attendance);
        return attendance;
    }

    public List<Schedule> getSchedulesByStaffId(Integer staffId) {
        User staff = userService.findOrFail(staffId);
        return scheduleRepository.findAllByScheduleUsers_User(staff);
    }

    public List<Schedule> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();  // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return scheduleRepository.findByShiftStartBetween(startDateTime, endDateTime);
    }

    public boolean hasCheckedIn(Schedule schedule, User user) {
        return attendanceRepository.findByScheduleAndStaff(schedule, user).isPresent();
    }

    public List<Schedule> getSchedulesByUserIdAndDateRange(Integer userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59

        return scheduleRepository.findByUserIdAndDateRange(userId, startDateTime, endDateTime);
    }

    /**
     * Kiểm tra xem đã có lịch làm việc nào trong khoảng thời gian chỉ định chưa
     * 
     * @param startDate Ngày bắt đầu của khoảng thời gian
     * @param endDate Ngày kết thúc của khoảng thời gian
     * @return true nếu đã có lịch làm việc, false nếu chưa có
     */
    public boolean hasSchedulesInDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Schedule> existingSchedules = scheduleRepository.findByShiftStartBetween(startDateTime, endDateTime);
        return !existingSchedules.isEmpty();
    }
    
    /**
     * Clone lịch làm việc của tuần đã chọn sang tuần tiếp theo, 
     * với kiểm tra trùng lặp và tùy chọn ghi đè
     * 
     * @param selectedWeekStartDate Ngày đầu tiên của tuần đã chọn
     * @param updateShiftType Có tự động cập nhật loại ca làm việc theo thời gian mới hay không
     * @param overwriteExisting Có ghi đè lịch làm việc đã tồn tại hay không
     * @return Danh sách các lịch làm việc đã được clone
     */
    @Transactional
    public List<Schedule> cloneSelectedWeekToNextWeek(LocalDate selectedWeekStartDate, boolean updateShiftType, boolean overwriteExisting) {
        // Tính ngày kết thúc của tuần đã chọn (thứ 2 -> chủ nhật: 7 ngày)
        LocalDate selectedWeekEndDate = selectedWeekStartDate.plusDays(6);
        
        // Tính ngày bắt đầu và kết thúc của tuần tiếp theo
        LocalDate nextWeekStartDate = selectedWeekStartDate.plusDays(7);
        LocalDate nextWeekEndDate = nextWeekStartDate.plusDays(6);
        
        // Kiểm tra xem đã có lịch làm việc trong tuần tiếp theo chưa
        if (!overwriteExisting && hasSchedulesInDateRange(nextWeekStartDate, nextWeekEndDate)) {
            // Nếu đã có lịch làm việc và không được ghi đè, trả về danh sách rỗng
            return List.of();
        }
        
        // Nếu yêu cầu ghi đè, xóa tất cả lịch làm việc trong tuần tiếp theo
        if (overwriteExisting && hasSchedulesInDateRange(nextWeekStartDate, nextWeekEndDate)) {
            deleteSchedulesInDateRange(nextWeekStartDate, nextWeekEndDate);
        }
        
        // Clone lịch làm việc từ tuần đã chọn sang tuần tiếp theo (cộng thêm 7 ngày)
        return cloneSchedulesFromPeriod(selectedWeekStartDate, selectedWeekEndDate, 7, updateShiftType);
    }
    
    /**
     * Xóa tất cả lịch làm việc trong khoảng thời gian chỉ định
     * 
     * @param startDate Ngày bắt đầu của khoảng thời gian
     * @param endDate Ngày kết thúc của khoảng thời gian
     */
    @Transactional
    public void deleteSchedulesInDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Schedule> existingSchedules = scheduleRepository.findByShiftStartBetween(startDateTime, endDateTime);
        
        for (Schedule schedule : existingSchedules) {
            // Xóa từng lịch làm việc
            deleteSchedule(schedule.getId());
        }
    }

    /**
     * Clone một lịch làm việc đã tồn tại với ngày làm việc mới
     * 
     * @param existingSchedule Lịch làm việc cần clone
     * @param newDate Ngày mới cho lịch làm việc
     * @param updateShiftType Có tự động cập nhật loại ca làm việc theo thời gian mới hay không
     * @return Lịch làm việc đã được clone
     */
    @Transactional
    public Schedule cloneSchedule(Schedule existingSchedule, LocalDate newDate, boolean updateShiftType) {
        // Tạo đối tượng lịch làm việc mới
        Schedule clonedSchedule = new Schedule();
        
        // Copy các thuộc tính từ lịch làm việc cũ
        clonedSchedule.setNote(existingSchedule.getNote());
        
        // Lấy thời gian bắt đầu và kết thúc từ lịch làm việc cũ
        LocalDateTime oldStartDateTime = existingSchedule.getShiftStart();
        LocalDateTime oldEndDateTime = existingSchedule.getShiftEnd();
        
        // Lấy giờ, phút, giây từ lịch làm việc cũ
        LocalTime oldStartTime = oldStartDateTime.toLocalTime();
        LocalTime oldEndTime = oldEndDateTime.toLocalTime();
        
        // Lấy ngày bắt đầu và kết thúc từ lịch làm việc cũ
        LocalDate oldStartDate = oldStartDateTime.toLocalDate();
        LocalDate oldEndDate = oldEndDateTime.toLocalDate();
        
        // Tính chênh lệch ngày giữa ngày bắt đầu và kết thúc của lịch cũ
        long daysDifference = ChronoUnit.DAYS.between(oldStartDate, oldEndDate);
        
        // Tạo ngày bắt đầu mới dựa trên ngày mới được cung cấp
        LocalDateTime newStartDateTime = LocalDateTime.of(newDate, oldStartTime);
        
        // Tạo ngày kết thúc mới, giữ nguyên số ngày chênh lệch như lịch cũ
        LocalDate newEndDate = newDate.plusDays(daysDifference);
        LocalDateTime newEndDateTime = LocalDateTime.of(newEndDate, oldEndTime);
        
        // Gán thời gian bắt đầu và kết thúc mới
        clonedSchedule.setShiftStart(newStartDateTime);
        clonedSchedule.setShiftEnd(newEndDateTime);
        
        // Xác định ShiftType mới nếu cần
        if (updateShiftType) {
            clonedSchedule.setShiftType(determineShiftType(newStartDateTime));
        } else {
            clonedSchedule.setShiftType(existingSchedule.getShiftType());
        }
        
        // Lưu schedule mới để có ID
        clonedSchedule = scheduleRepository.save(clonedSchedule);
        
        // Clone danh sách người dùng
        final Schedule finalClonedSchedule = clonedSchedule;
        List<ScheduleUser> clonedScheduleUsers = existingSchedule.getScheduleUsers().stream()
                .map(originalUser -> {
                    return new ScheduleUser(
                            null, 
                            finalClonedSchedule, 
                            originalUser.getUser(), 
                            originalUser.getIsManager(),
                            null);
                })
                .collect(Collectors.toList());
        
        // Lưu danh sách người dùng mới
        scheduleUserRepository.saveAll(clonedScheduleUsers);
        
        // Cập nhật danh sách trong đối tượng Schedule
        clonedSchedule.setScheduleUsers(clonedScheduleUsers);
        
        return clonedSchedule;
    }
    
    /**
     * Xác định loại ca làm việc dựa trên thời gian bắt đầu
     * 
     * @param shiftStart Thời gian bắt đầu ca làm việc
     * @return Loại ca làm việc phù hợp
     */
    private ShiftType determineShiftType(LocalDateTime shiftStart) {
        LocalTime startTime = shiftStart.toLocalTime();
        
        // Sáng: 6:00 - 12:00
        LocalTime morningStart = LocalTime.of(6, 0);
        LocalTime morningEnd = LocalTime.of(12, 0);
        
        // Chiều: 12:00 - 18:00
        LocalTime afternoonStart = LocalTime.of(12, 0);
        LocalTime afternoonEnd = LocalTime.of(18, 0);
        
        // Tối: 18:00 - 6:00 (ngày hôm sau)
        if (startTime.isAfter(morningStart) && startTime.isBefore(morningEnd)) {
            return ShiftType.MORNING;
        } else if (startTime.isAfter(afternoonStart) && startTime.isBefore(afternoonEnd)) {
            return ShiftType.EVENING;
        } else {
            return ShiftType.NIGHT;
        }
    }

    /**
     * Clone nhiều lịch làm việc từ một khoảng thời gian (tuần) sang khoảng thời gian mới
     * với số ngày chênh lệch tương ứng
     * 
     * @param sourceStartDate Ngày bắt đầu của khoảng thời gian nguồn (tuần trước)
     * @param sourceEndDate Ngày kết thúc của khoảng thời gian nguồn (tuần trước)
     * @param daysDifference Số ngày cần dịch chuyển (thường là 7 ngày cho 1 tuần)
     * @param updateShiftType Có tự động cập nhật loại ca làm việc theo thời gian mới hay không
     * @return Danh sách các lịch làm việc đã được clone
     */
    @Transactional
    public List<Schedule> cloneSchedulesFromPeriod(LocalDate sourceStartDate, LocalDate sourceEndDate, 
                                                  long daysDifference, boolean updateShiftType) {
        // Lấy danh sách các lịch làm việc trong khoảng thời gian nguồn
        List<Schedule> sourceSchedules = getSchedulesByDateRange(sourceStartDate, sourceEndDate);
        
        // Danh sách lưu các lịch làm việc đã clone
        List<Schedule> clonedSchedules = sourceSchedules.stream()
                .map(sourceSchedule -> {
                    // Tính toán ngày mới cho lịch làm việc
                    LocalDate originalDate = sourceSchedule.getShiftStart().toLocalDate();
                    LocalDate newDate = originalDate.plusDays(daysDifference);
                    
                    // Clone lịch làm việc với ngày mới
                    return cloneSchedule(sourceSchedule, newDate, updateShiftType);
                })
                .collect(Collectors.toList());
        
        return clonedSchedules;
    }
    
    /**
     * Clone lịch làm việc của tuần trước sang tuần này
     * 
     * @param currentWeekStartDate Ngày đầu tiên của tuần hiện tại
     * @param updateShiftType Có tự động cập nhật loại ca làm việc theo thời gian mới hay không
     * @return Danh sách các lịch làm việc đã được clone
     */
    @Transactional
    public List<Schedule> clonePreviousWeekSchedules(LocalDate currentWeekStartDate, boolean updateShiftType) {
        // Tính ngày bắt đầu và kết thúc của tuần trước
        LocalDate previousWeekStartDate = currentWeekStartDate.minusDays(7);
        LocalDate previousWeekEndDate = currentWeekStartDate.minusDays(1);
        
        // Clone lịch làm việc từ tuần trước sang tuần hiện tại (7 ngày)
        return cloneSchedulesFromPeriod(previousWeekStartDate, previousWeekEndDate, 7, updateShiftType);
    }

    /**
     * Clone lịch làm việc của tuần đã chọn sang tuần tiếp theo
     * 
     * @param selectedWeekStartDate Ngày đầu tiên của tuần đã chọn
     * @param updateShiftType Có tự động cập nhật loại ca làm việc theo thời gian mới hay không
     * @return Danh sách các lịch làm việc đã được clone
     */
    @Transactional
    public List<Schedule> cloneSelectedWeekToNextWeek(LocalDate selectedWeekStartDate, boolean updateShiftType) {
        // Gọi phương thức đầy đủ, mặc định không ghi đè
        return cloneSelectedWeekToNextWeek(selectedWeekStartDate, updateShiftType, false);
    }

    /**
     * Clone lịch làm việc từ một tuần cụ thể sang một tuần cụ thể khác,
     * với kiểm tra trùng lặp và tùy chọn ghi đè
     * 
     * @param sourceWeekStartDate Ngày đầu tiên của tuần nguồn
     * @param targetWeekStartDate Ngày đầu tiên của tuần đích
     * @param updateShiftType Có tự động cập nhật loại ca làm việc theo thời gian mới hay không
     * @param overwriteExisting Có ghi đè lịch làm việc đã tồn tại hay không
     * @return Danh sách các lịch làm việc đã được clone
     */
    @Transactional
    public List<Schedule> cloneBetweenSpecificWeeks(LocalDate sourceWeekStartDate, LocalDate targetWeekStartDate, 
                                                   boolean updateShiftType, boolean overwriteExisting) {
        // Tính ngày kết thúc của tuần nguồn (thứ 2 -> chủ nhật: 7 ngày)
        LocalDate sourceWeekEndDate = sourceWeekStartDate.plusDays(6);
        
        // Tính ngày kết thúc của tuần đích
        LocalDate targetWeekEndDate = targetWeekStartDate.plusDays(6);
        
        // Kiểm tra xem đã có lịch làm việc trong tuần đích chưa
        if (!overwriteExisting && hasSchedulesInDateRange(targetWeekStartDate, targetWeekEndDate)) {
            // Nếu đã có lịch làm việc và không được ghi đè, trả về danh sách rỗng
            return List.of();
        }
        
        // Nếu yêu cầu ghi đè, xóa tất cả lịch làm việc trong tuần đích
        if (overwriteExisting && hasSchedulesInDateRange(targetWeekStartDate, targetWeekEndDate)) {
            deleteSchedulesInDateRange(targetWeekStartDate, targetWeekEndDate);
        }
        
        // Lấy danh sách các lịch làm việc trong khoảng thời gian nguồn
        List<Schedule> sourceSchedules = getSchedulesByDateRange(sourceWeekStartDate, sourceWeekEndDate);
        
        // Tính toán sự chênh lệch ngày giữa tuần nguồn và tuần đích
        long daysDifference = ChronoUnit.DAYS.between(sourceWeekStartDate, targetWeekStartDate);
        
        // Danh sách lưu các lịch làm việc đã clone
        List<Schedule> clonedSchedules = sourceSchedules.stream()
                .map(sourceSchedule -> {
                    // Tính toán ngày mới cho lịch làm việc
                    LocalDate originalDate = sourceSchedule.getShiftStart().toLocalDate();
                    LocalDate newDate = originalDate.plusDays(daysDifference);
                    
                    // Clone lịch làm việc với ngày mới
                    return cloneSchedule(sourceSchedule, newDate, updateShiftType);
                })
                .collect(Collectors.toList());
        
        return clonedSchedules;
    }
}
