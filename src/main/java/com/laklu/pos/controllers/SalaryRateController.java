package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.SalaryRatePolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewSalaryRate;
import com.laklu.pos.entities.SalaryRate;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.SalaryRateService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.ValueExistIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salary-rates")
@AllArgsConstructor
@Tag(name = "Salary Rate Controller", description = "Quản lý mức lương")
public class SalaryRateController {

    private final SalaryRateService salaryRateService;
    private final SalaryRatePolicy salaryRatePolicy;

    @Operation(summary = "Lấy danh sách mức lương", description = "API này dùng để lấy toàn bộ các mức lương")
    @GetMapping("/")
    public ApiResponseEntity index() throws Exception {
        Ultis.throwUnless(salaryRatePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        List<SalaryRate> salaryRates = salaryRateService.getAll();
        return ApiResponseEntity.success(salaryRates);
    }

    @Operation(summary = "Tạo mức lương", description = "API này dùng để tạo mức lương mới")
    @PostMapping("/")
    public ApiResponseEntity store(@RequestBody NewSalaryRate newSalaryRate) throws Exception {
        Ultis.throwUnless(salaryRatePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        this.validateName(newSalaryRate.getLevelName());
        SalaryRate salaryRate = salaryRateService.createSalaryRate(newSalaryRate);
        return ApiResponseEntity.success(salaryRate);
    }

    @Operation(summary = "Cập nhật mức lương", description = "API này dùng để cập nhật thông tin mức lương")
    @PutMapping("/{id}")
    public ApiResponseEntity updateSalaryRate(@PathVariable Integer id, @RequestBody NewSalaryRate newSalaryRate) throws Exception{
        var salaryRate = salaryRateService.findOrFail(id);
        Ultis.throwUnless(salaryRatePolicy.canEdit(JwtGuard.userPrincipal(), salaryRate), new ForbiddenException());
        //this.validateName(newSalaryRate.getLevelName());
        SalaryRate updatedSalaryRate = salaryRateService.updateSalaryRate(id, newSalaryRate);
        return ApiResponseEntity.success(updatedSalaryRate);
    }

    @Operation(summary = "Xóa mức lương", description = "API này dùng để xóa mức lương")
    @DeleteMapping("/{id}")
    public ApiResponseEntity deleteSalaryRate(@PathVariable Integer id) throws Exception {
        SalaryRate salaryRate = salaryRateService.findOrFail(id);
        
        Ultis.throwUnless(salaryRatePolicy.canDelete(JwtGuard.userPrincipal(), salaryRate), new ForbiddenException());
        
        // Kiểm tra xem có người dùng nào đang sử dụng mức lương này không
        if (salaryRateService.isSalaryRateInUse(salaryRate)) {
            return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, "Lỗi xóa mức lương", "Không thể xóa mức lương đang được sử dụng bởi một hoặc nhiều người dùng");
        }
        
        salaryRateService.deleteSalaryRate(salaryRate);
        return ApiResponseEntity.success("Xóa mức lương thành công");
    }

    private void validateName(String name) {
        ValueExistIn<String> rule = new ValueExistIn<>(
                "Vai trò",
                name,
                (n) -> salaryRateService.findSalaryRateByName(n).isEmpty()
        );
        if (!rule.isValid()) {
            throw new IllegalArgumentException(rule.getMessage());
        }
    }

}