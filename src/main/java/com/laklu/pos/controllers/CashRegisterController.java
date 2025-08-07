package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.PaymentPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.CashRegisterFilterRequest;
import com.laklu.pos.dataObjects.request.WithdrawRequest;
import com.laklu.pos.dataObjects.response.CashRegisterResponse;
import com.laklu.pos.dataObjects.response.CashRegisterSummaryResponse;
import com.laklu.pos.entities.CashRegister;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.mappers.CashRegisterMapper;
import com.laklu.pos.services.CashRegisterService;
import com.laklu.pos.uiltis.Ultis;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cash-register")
public class CashRegisterController {
    private final CashRegisterService cashRegisterService;
    private final PaymentPolicy paymentPolicy;
    private final CashRegisterMapper cashRegisterMapper;

    @PostMapping("/start-amount")
    public ApiResponseEntity updateStartAmount(@RequestParam BigDecimal amount,
                                            @RequestParam(required = false) String notes) throws Exception{
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        Integer userId = JwtGuard.userPrincipal().getPersitentUser().getId();
        CashRegister shift = cashRegisterService.updateStartAmount(userId, amount, notes);
        CashRegisterResponse response = cashRegisterMapper.toResponse(shift);
        return ApiResponseEntity.success(response, "Cập nhật số tiền đầu ca thành công");
    }

    @PostMapping("/end-amount")
    public ApiResponseEntity updateEndAmount(@RequestParam BigDecimal amount,
                                          @RequestParam(required = false) String notes) throws Exception{
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());                                        
        Integer userId = JwtGuard.userPrincipal().getPersitentUser().getId();
        CashRegister shift = cashRegisterService.updateEndAmount(userId, amount, notes);
        CashRegisterResponse response = cashRegisterMapper.toResponse(shift);
        return ApiResponseEntity.success(response, "Cập nhật số tiền cuối ca thành công");
    }
    
    @PostMapping("/withdraw")
    public ApiResponseEntity withdrawAmount(@RequestBody @Valid WithdrawRequest request) throws Exception {
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        Integer userId = JwtGuard.userPrincipal().getPersitentUser().getId();
        CashRegister cashRegister = cashRegisterService.withdrawAmount(userId, request.getAmount(), request.getNotes());
        CashRegisterResponse response = cashRegisterMapper.toResponse(cashRegister);
        return ApiResponseEntity.success(response, "Rút tiền thành công");
    }
    
    @GetMapping("/today")
    public ApiResponseEntity getTodayCashRegisters() throws Exception {
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        List<CashRegister> registers = cashRegisterService.getCashRegistersForToday();
        List<CashRegisterResponse> responseList = cashRegisterMapper.toResponseList(registers);
        return ApiResponseEntity.success(responseList, "Danh sách số tiền trong cash register ngày hôm nay");
    }
    
    @GetMapping("/today/summary")
    public ApiResponseEntity getTodayCashRegisterSummary() throws Exception {
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        CashRegisterSummaryResponse summary = cashRegisterService.getCashRegisterSummaryForToday();
        return ApiResponseEntity.success(summary, "Thông tin tổng hợp số tiền trong cash register ngày hôm nay");
    }

    @GetMapping("/search")
    public ApiResponseEntity searchCashRegisters(@Valid CashRegisterFilterRequest request) throws Exception {
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        Page<CashRegister> cashRegistersPage = cashRegisterService.getCashRegistersByDateRange(
            request.getStartDate(),
            request.getEndDate(),
            request.getPage(),
            request.getSize()
        );
        
        Page<CashRegisterResponse> responsePage = cashRegistersPage.map(cashRegisterMapper::toResponse);
        
        return ApiResponseEntity.success(responsePage, "Danh sách các ca tiền trong két theo khoảng thời gian");
    }
} 