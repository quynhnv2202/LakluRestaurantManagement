package com.laklu.pos.services;

import com.laklu.pos.dataObjects.response.AttendanceStatsDTO;
import com.laklu.pos.entities.Attendance;
import com.laklu.pos.entities.Payslip;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.SalaryType;
import com.laklu.pos.repositories.AttendanceRepository;
import com.laklu.pos.repositories.PayslipRepository;
import com.laklu.pos.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayslipService {
    private final PayslipRepository payslipRepository;
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private YearMonth parseSalaryMonth(String salaryMonth) {
        try {
            return YearMonth.parse(salaryMonth);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng tháng lương không hợp lệ, yêu cầu định dạng yyyy-MM!");
        }
    }

    @Transactional
    public List<Payslip> calculatePayslipForAllStaff(String salaryMonth) {
        YearMonth month = parseSalaryMonth(salaryMonth);
        String formattedMonth = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        payslipRepository.deleteAllBySalaryMonth(formattedMonth);

        Map<Integer, AttendanceStatsDTO> statsMap = attendanceService.getAttendanceStatsForAllStaff(salaryMonth);

        List<Payslip> payslips = statsMap.entrySet().stream()
                .map(entry -> {
                    Integer staffId = entry.getKey();
                    AttendanceStatsDTO stats = entry.getValue();
                    User staff = userService.findOrFail(staffId);
                    SalaryRate salaryRate = staff.getSalaryRate();
                    if (salaryRate == null) {
                        return null;
                    }
                    Double totalWorkingHours = attendanceService.getTotalWorkingHours(staffId, salaryMonth);
                    BigDecimal totalSalary;
                    if(salaryRate.getType() == SalaryType.HOURLY) {
                        totalSalary = salaryRate.getAmount().multiply(BigDecimal.valueOf(totalWorkingHours));
                    } else if (salaryRate.getType() == SalaryType.SHIFTLY) {
                        totalSalary = salaryRate.getAmount().multiply(BigDecimal.valueOf(stats.getTotalWorkingDays()));
                    }else{
                        totalSalary = salaryRate.getAmount();
                    }

                    Payslip payslip = new Payslip();
                    payslip.setStaff(staff);
                    payslip.setSalaryMonth(month);
                    payslip.setTotalSalary(totalSalary);
                    payslip.setTotalWorkingDays(stats.getTotalWorkingDays());
                    payslip.setTotalWorkingHours(totalWorkingHours);
                    payslip.setLateCount(stats.getLateCount());
                    payslip.setLateHours(stats.getLateHours());

                    return payslip;
                }).filter(payslip -> payslip != null)
                .collect(Collectors.toList());

        return payslipRepository.saveAll(payslips);
    }

    public Payslip getPayslipForStaff(int staffId, String salaryMonth){
        User staff = userService.findOrFail(staffId);
        YearMonth month = parseSalaryMonth(salaryMonth);
        String formattedMonth = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return payslipRepository.findByStaffAndSalaryMonth(staff, formattedMonth)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu lương cho nhân viên này vào tháng " + salaryMonth));
    }

    public List<Payslip> getAllPayslips(String salaryMonth) {
        YearMonth month = parseSalaryMonth(salaryMonth);
        String formattedMonth = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return payslipRepository.findAllBySalaryMonth(formattedMonth);
    }
}
