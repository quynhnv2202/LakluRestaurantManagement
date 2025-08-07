package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.PaymentPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.DateRangeRequest;
import com.laklu.pos.dataObjects.response.PageResponse;
import com.laklu.pos.dataObjects.response.PaymentHistoryResponse;
import com.laklu.pos.entities.PaymentHistory;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.mappers.PaymentHistoryMapper;
import com.laklu.pos.services.PaymentHistoryService;
import com.laklu.pos.uiltis.Ultis;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/payment-history")
public class PaymentHistoryController {

    private final PaymentHistoryService paymentHistoryService;
    private final PaymentHistoryMapper paymentHistoryMapper;
    private final PaymentPolicy paymentPolicy;

    @GetMapping("/range")
    public ApiResponseEntity getPaymentHistoriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {
        
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Ngày kết thúc không được trước ngày bắt đầu");
        }
        
        List<PaymentHistory> paymentHistories = paymentHistoryService.getPaymentHistoriesByDateRange(startDate, endDate);
        List<PaymentHistoryResponse> responseList = paymentHistoryMapper.toResponseList(paymentHistories);
        
        return ApiResponseEntity.success(responseList, "Danh sách lịch sử thanh toán theo khoảng thời gian");
    }
    
    @PostMapping("/search")
    public ApiResponseEntity searchPaymentHistoriesByDateRange(
            @RequestBody @Valid DateRangeRequest request) throws Exception {
        
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc không được trước ngày bắt đầu");
        }
        
        Page<PaymentHistory> paymentHistoriesPage = paymentHistoryService.getPaymentHistoriesPageByDateRange(
                request.getStartDate(), request.getEndDate(), request.getPageNumber(), request.getPageSize());
        
        List<PaymentHistoryResponse> responseList = paymentHistoryMapper.toResponseList(paymentHistoriesPage.getContent());
        PageResponse<PaymentHistoryResponse> pageResponse = PageResponse.fromPage(paymentHistoriesPage, responseList);
        
        return ApiResponseEntity.success(pageResponse, "Danh sách lịch sử thanh toán theo khoảng thời gian (phân trang)");
    }
    
    @GetMapping("/page")
    public ApiResponseEntity getPaymentHistoriesPageByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Ngày kết thúc không được trước ngày bắt đầu");
        }
        
        Page<PaymentHistory> paymentHistoriesPage = paymentHistoryService.getPaymentHistoriesPageByDateRange(
                startDate, endDate, page, size);
        
        List<PaymentHistoryResponse> responseList = paymentHistoryMapper.toResponseList(paymentHistoriesPage.getContent());
        PageResponse<PaymentHistoryResponse> pageResponse = PageResponse.fromPage(paymentHistoriesPage, responseList);
        
        return ApiResponseEntity.success(pageResponse, "Danh sách lịch sử thanh toán theo khoảng thời gian (phân trang)");
    }
} 