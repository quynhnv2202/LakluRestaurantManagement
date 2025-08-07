package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.VoucherPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.VoucherRequest;
import com.laklu.pos.dataObjects.response.VoucherResponse;
import com.laklu.pos.entities.*;
import com.laklu.pos.enums.DiscountType;
import com.laklu.pos.enums.VoucherStatus;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.VoucherService;
import com.laklu.pos.uiltis.Ultis;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
@Slf4j
public class VoucherController {
    private final VoucherService voucherService;
    private final VoucherPolicy voucherPolicy;

    @Operation(summary = "Tìm kiếm voucher theo code", description = "API này dùng để tìm kiếm voucher theo code")
    @GetMapping("/search")
    public ApiResponseEntity searchVoucherByCode(@RequestParam String code) throws Exception {
        List<Voucher> vouchers = voucherService.findByCodeContaining(code);
        List<VoucherResponse> responseList = vouchers.stream()
                .filter(voucher -> voucherPolicy.canView(JwtGuard.userPrincipal(), voucher))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponseEntity.success(responseList, "Tìm kiếm voucher theo code");
    }

    @GetMapping("/{id}")
    public ApiResponseEntity getVoucherById(@PathVariable int id) throws Exception {
        Voucher voucher = voucherService.findOrFail(id);
        Ultis.throwUnless(voucherPolicy.canView(JwtGuard.userPrincipal(), voucher), new ForbiddenException());

        return ApiResponseEntity.success(convertToResponse(voucher), "Lấy voucher theo id");
    }

    @GetMapping("/getAll")
    public ApiResponseEntity getAll() throws Exception {
        Ultis.throwUnless(voucherPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        List<VoucherResponse> vouchers = voucherService.getAll().stream()
                .map(this::convertToResponse).collect(Collectors.toList());

        return ApiResponseEntity.success(vouchers, "Lấy danh sách voucher");
    }

    @Operation(summary = "Tìm kiếm voucher theo status", description = "API này dùng để tìm kiếm voucher theo status")
    @GetMapping("/searchByStatus")
    public ApiResponseEntity searchVoucherByStatus(@RequestParam VoucherStatus status) throws Exception {
        List<Voucher> vouchers = voucherService.findByStatus(status);
        List<VoucherResponse> responseList = vouchers.stream()
                .filter(voucher -> voucherPolicy.canView(JwtGuard.userPrincipal(), voucher))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ApiResponseEntity.success(responseList, "Tìm kiếm voucher theo status");
    }

    @PostMapping("/create")
    public ApiResponseEntity createVoucher(@Valid @RequestBody VoucherRequest request) throws Exception {
        Ultis.throwUnless(voucherPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        Voucher voucher = new Voucher(
                null,
                request.getCode(),
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getValidFrom(),
                request.getValidUntil(),
                VoucherStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Voucher savedVoucher = voucherService.createVoucher(voucher);
        return ApiResponseEntity.success(convertToResponse(savedVoucher), "Tạo voucher thành công");
    }

    @Operation(summary = "Kiểm tra và cập nhật voucher hết hạn", description = "API này dùng để kiểm tra và cập nhật voucher hết hạn")
    @PostMapping("/checkExpired")
    public ApiResponseEntity checkAndUpdateExpiredVouchers() throws Exception {
        Ultis.throwUnless(voucherPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        voucherService.checkAndUpdateExpiredVouchers();
        return ApiResponseEntity.success("Kiểm tra và cập nhật voucher hết hạn thành công");
    }

    @PostMapping("/update/{id}")
    public ApiResponseEntity updateVoucher(@PathVariable int id, @Valid @RequestBody VoucherRequest request) throws Exception {
        Voucher existingVoucher = voucherService.findOrFail(id);

        Ultis.throwUnless(voucherPolicy.canEdit(JwtGuard.userPrincipal(), existingVoucher), new ForbiddenException());
        existingVoucher.setCode(request.getCode());
        existingVoucher.setDiscountType(request.getDiscountType());
        existingVoucher.setDiscountValue(request.getDiscountValue());
        existingVoucher.setValidFrom(request.getValidFrom());
        existingVoucher.setValidUntil(request.getValidUntil());
        existingVoucher.setStatus(request.getStatus());
        existingVoucher.setUpdatedAt(LocalDateTime.now());

        Voucher updatedVoucher = voucherService.updateVoucher(id, existingVoucher);
        return ApiResponseEntity.success(convertToResponse(updatedVoucher), "Caạp nhật voucher thành công");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponseEntity deleteVoucher(@PathVariable int id) throws Exception {
        Voucher voucher = voucherService.findOrFail(id);
        Ultis.throwUnless(voucherPolicy.canDelete(JwtGuard.userPrincipal(), voucher), new ForbiddenException());
        voucherService.deleteVoucher(id);
        return ApiResponseEntity.success("Xóa voucher thành cong");
    }

    private VoucherResponse convertToResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setDiscountType(voucher.getDiscountType());
        response.setDiscountValue(voucher.getDiscountValue());
        response.setValidFrom(voucher.getValidFrom());
        response.setValidUntil(voucher.getValidUntil());
        response.setStatus(voucher.getStatus());
        response.setCreatedAt(voucher.getCreatedAt());
        response.setUpdatedAt(voucher.getUpdatedAt());
        return response;
    }
}
