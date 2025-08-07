package com.laklu.pos.services;

import com.laklu.pos.dataObjects.response.CashRegisterResponse;
import com.laklu.pos.dataObjects.response.CashRegisterSummaryResponse;
import com.laklu.pos.entities.CashRegister;
import com.laklu.pos.entities.PaymentHistory;
import com.laklu.pos.entities.Schedule;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.PaymentType;
import com.laklu.pos.exceptions.httpExceptions.BadRequestException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mappers.CashRegisterMapper;
import com.laklu.pos.repositories.CashRegisterRepository;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashRegisterService {
    private final CashRegisterRepository cashRegisterRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final CashRegisterMapper cashRegisterMapper;

    @Transactional
    public CashRegister updateStartAmount(Integer userId, BigDecimal amount, String notes) {
        // Tìm ca làm việc hiện tại của user
        Optional<Schedule> currentSchedule = scheduleRepository.findCurrentScheduleByStaffId(userId);
        if (currentSchedule.isEmpty()) {
            throw new RuntimeException("Bạn không có lịch làm việc ngày hôm nay.");
        }

        // Kiểm tra xem đã có bản ghi CashRegister cho ca này chưa
        Optional<CashRegister> existingRegister = cashRegisterRepository.findByScheduleId(currentSchedule.get().getId());
        if (existingRegister.isPresent()) {
            throw new RuntimeException("Bạn đã bắt đầu ca làm việc trước đó.");
        }

        // Tạo bản ghi mới
        User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        CashRegister cashRegister = new CashRegister();
        cashRegister.setUser(user);
        cashRegister.setSchedule(currentSchedule.get());
        cashRegister.setInitialAmount(amount);
        cashRegister.setCurrentAmount(amount);
        cashRegister.setShiftStart(LocalDateTime.now());
        cashRegister.setNotes(notes);

        return cashRegisterRepository.save(cashRegister);
    }

    @Transactional
    public CashRegister updateEndAmount(Integer userId, BigDecimal amount, String notes) {
        // Tìm ca làm việc hiện tại của user
        Optional<Schedule> currentSchedule = scheduleRepository.findCurrentScheduleByStaffId(userId);
        if (currentSchedule.isEmpty()) {
            throw new RuntimeException("Bạn không có lịch làm việc ngày hôm nay.");
        }

        // Tìm bản ghi CashRegister của ca này
        CashRegister cashRegister = cashRegisterRepository.findByScheduleId(currentSchedule.get().getId())
                .orElseThrow(BadRequestException::new);

        if (cashRegister.getShiftEnd() != null) {
            throw new RuntimeException("Bạn đã kết thúc ca làm việc trước đó.");
        }

        cashRegister.setCurrentAmount(amount);
        cashRegister.setShiftEnd(LocalDateTime.now());
        if (notes != null && !notes.isEmpty()) {
            String existingNotes = cashRegister.getNotes();
            cashRegister.setNotes(existingNotes != null ? existingNotes + "\n" + notes : notes);
        }

        return cashRegisterRepository.save(cashRegister);
    }

    @Transactional
    public CashRegister withdrawAmount(Integer userId, BigDecimal amount, String notes) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Số tiền rút phải lớn hơn 0.");
        }
        
        // Tìm ca làm việc hiện tại của user
        Optional<Schedule> currentSchedule = scheduleRepository.findCurrentScheduleByStaffId(userId);
        if (currentSchedule.isEmpty()) {
            throw new RuntimeException("Bạn không có lịch làm việc ngày hôm nay.");
        }

        // Tìm bản ghi CashRegister của ca này
        CashRegister cashRegister = cashRegisterRepository.findByScheduleId(currentSchedule.get().getId())
                .orElseThrow(() -> new RuntimeException("Bạn cần bắt đầu ca làm việc trước khi rút tiền."));

        if (cashRegister.getShiftEnd() != null) {
            throw new RuntimeException("Bạn đã kết thúc ca làm việc, không thể rút tiền.");
        }

        // Kiểm tra số tiền hiện tại có đủ để rút không
        if (cashRegister.getCurrentAmount().compareTo(amount) < 0) {
            throw new RuntimeException("Số tiền trong két không đủ để rút. Hiện có: " + cashRegister.getCurrentAmount());
        }

        // Cập nhật số tiền hiện tại
        cashRegister.updateCurrentAmount(amount, PaymentType.OUT);
        
        // Cập nhật ghi chú
        String withdrawNote = "Rút tiền: " + amount + " - " + notes;
        String existingNotes = cashRegister.getNotes();
        cashRegister.setNotes(existingNotes != null ? existingNotes + " - " + withdrawNote : withdrawNote);

        return cashRegisterRepository.save(cashRegister);
    }

    public CashRegister findOrFail(Integer id) {
        return this.getCashRegister(id).orElseThrow(NotFoundException::new);
    }

    public Optional<CashRegister> getCashRegister(int paymentId) {
        return cashRegisterRepository.findById(paymentId);
    }

    public Integer getIdcashRegisterBySchedule(Integer userId){
        Optional<Schedule> currentSchedule = scheduleRepository.findCurrentScheduleByStaffId(userId);
        if (currentSchedule.isPresent()) {
            Optional<CashRegister> existingRegister = cashRegisterRepository.findByScheduleId(currentSchedule.get().getId());
            if (existingRegister.isPresent()) {
                return existingRegister.get().getId();
            } else {
                throw new IllegalArgumentException("Nhập số tiền có trong két trước khi tạo thanhh toán.");
            }
        } else {
            throw new IllegalArgumentException("Bạn không có lịch làm việc ngày hôm nay.");
        }
    }

    public List<CashRegister> getCashRegistersForToday() {
        LocalDate today = LocalDate.now();
        return cashRegisterRepository.findAllByDate(today);
    }

    public CashRegisterSummaryResponse getCashRegisterSummaryForToday() {
        LocalDate today = LocalDate.now();
        List<CashRegister> registers = cashRegisterRepository.findAllByDate(today);
        List<CashRegisterResponse> responseList = cashRegisterMapper.toResponseList(registers);
        
        BigDecimal totalInitialAmount = registers.stream()
                .map(CashRegister::getInitialAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCurrentAmount = registers.stream()
                .map(CashRegister::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal diffAmount = totalCurrentAmount.subtract(totalInitialAmount);
        
        return new CashRegisterSummaryResponse(
                today,
                totalInitialAmount,
                totalCurrentAmount,
                diffAmount,
                registers.size(),
                responseList
        );
    }

    public Page<CashRegister> getCashRegistersByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        // Nếu không có ngày bắt đầu, lấy từ đầu tháng hiện tại
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        
        // Nếu không có ngày kết thúc, lấy đến ngày hiện tại
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // Tạo đối tượng Pageable với sắp xếp theo thời gian tạo giảm dần (mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Gọi repository để lấy dữ liệu
        return cashRegisterRepository.findAllByDateRange(startDate, endDate, pageable);
    }

} 