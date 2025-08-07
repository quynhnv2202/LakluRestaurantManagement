package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.PayslipPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.response.PayslipResponse;
import com.laklu.pos.entities.Payslip;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.AttendanceService;
import com.laklu.pos.services.PayslipService;
import com.laklu.pos.uiltis.Ultis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payslips")
@RequiredArgsConstructor
@Slf4j
public class PayslipController {
    private final PayslipService payslipService;
    private final PayslipPolicy payslipPolicy;
    private final AttendanceService attendanceService;

    private PayslipResponse toPayslipResponse(Payslip payslip) {
        return new PayslipResponse(
                payslip.getId(),
                payslip.getStaff().getId(),
                payslip.getStaff().getUsername(),
                payslip.getSalaryMonth(),
                payslip.getTotalWorkingDays(),
                payslip.getTotalWorkingHours(),
                payslip.getTotalSalary(),
                payslip.getLateCount(),
                payslip.getLateHours()
        );
    }

    @PostMapping("/{salaryMonth}")
    public ApiResponseEntity calculatePayslip(@PathVariable String salaryMonth) throws Exception {
        Ultis.throwUnless(payslipPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        List<Payslip> payslips = payslipService.calculatePayslipForAllStaff(salaryMonth);
        List<PayslipResponse> payslipResponses = payslips.stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
        return ApiResponseEntity.success(payslipResponses, "Tính lương thành công");
    }

    @GetMapping("/{staffId}/{salaryMonth}")
    public ApiResponseEntity getPayslipForStaff(
            @PathVariable int staffId,
            @PathVariable String salaryMonth) throws Exception {
        Payslip payslip = payslipService.getPayslipForStaff(staffId, salaryMonth);
        Ultis.throwUnless(payslipPolicy.canView(JwtGuard.userPrincipal(), payslip), new ForbiddenException());
        PayslipResponse payslipResponse = toPayslipResponse(payslip);
        return ApiResponseEntity.success(payslipResponse, "Lấy phiếu lương thành công");
    }

    @GetMapping("{salaryMonth}")
    public ApiResponseEntity getAllPayslips(@PathVariable String salaryMonth) throws Exception {
        Ultis.throwUnless(payslipPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        List<Payslip> payslips = payslipService.getAllPayslips(salaryMonth);
        List<PayslipResponse> payslipResponses = payslips.stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
        return ApiResponseEntity.success(payslipResponses, "Lấy tất cả phiếu lương thành công");
    }
}
