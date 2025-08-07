package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.TablePolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewTable;
import com.laklu.pos.dataObjects.request.TableUpdateRequest;
import com.laklu.pos.dataObjects.response.TableDetailResponse;
import com.laklu.pos.dataObjects.response.TableResponse;
import com.laklu.pos.entities.Table;
import com.laklu.pos.enums.StatusTable;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.ReservationService;
import com.laklu.pos.services.TableService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.TableMustBeAvailable;
import com.laklu.pos.validator.TableMustBeUnique;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RestController
@RequestMapping("api/v1/tables")
@RequiredArgsConstructor
@Tag(name = "Table Controller", description = "Quản lý thông tin người dùng")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableController {
    TableService tableService;
    TablePolicy tablePolicy;
    ReservationService reservationService;

    @Operation(summary = "Lấy danh sách bàn", description = "API này dùng để lấy toàn bộ các bạn của quán")
    @GetMapping("/")
    public ApiResponseEntity index() throws Exception {
        Ultis.throwUnless(tablePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        LocalDate date = LocalDate.now();
        List<Table> tables = tableService.getAllTables();
        List<TableDetailResponse> tableDetailResponses = tables.stream()
            .map(table -> formatTableDetailResponseByDate(table, date))
            .toList();
        return ApiResponseEntity.success(tableDetailResponses);
    }

    @Operation(summary = "Lấy danh sách bàn theo ngày", description = "API này dùng để lấy danh sách bàn và trạng thái theo ngày")
    @GetMapping("/by-date")
    public ApiResponseEntity getTablesByDate(@RequestParam("date") String dateString) throws Exception {
        Ultis.throwUnless(tablePolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        LocalDate requestDate = LocalDate.parse(dateString);
        List<Table> tables = tableService.getAllTables();
        List<TableDetailResponse> tableDetailResponses = tables.stream()
            .map(table -> formatTableDetailResponseByDate(table, requestDate))
            .toList();
        return ApiResponseEntity.success(tableDetailResponses);
    }

    private TableDetailResponse formatTableDetailResponseByDate(Table table, LocalDate date) {
        // Nếu bàn đã ở trạng thái OCCUPIED, giữ nguyên trạng thái này
        if (table.getStatus() == StatusTable.OCCUPIED) {
            return TableDetailResponse.builder()
                    .id(table.getId())
                    .tableNumber(table.getTableNumber())
                    .capacity(table.getCapacity())
                    .status(StatusTable.OCCUPIED.toString())
                    .build();
        }
        
        // Nếu không, kiểm tra xem bàn có khả dụng không
        var isAvailable = new TableMustBeAvailable(table, date, reservationService).isValid();
        StatusTable status = isAvailable ? StatusTable.AVAILABLE : StatusTable.RESERVED;
        return TableDetailResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(status.toString())
                .build();
    }

    @Operation(summary = "Tạo bàn", description = "API này dùng để tạo bán mới")
    @PostMapping("/")
    public ApiResponseEntity store(@Valid @RequestBody NewTable request) throws Exception{
        Ultis.throwUnless(tablePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());

        Function<String, Optional<Table>> tabeResolver = tableService::findByTableName;

        RuleValidator.validate(new TableMustBeUnique(tabeResolver, request.getTableNumber()));

        Table table = tableService.createTable(request);

        return ApiResponseEntity.success(new TableResponse(table));
    }

    @Operation(summary = "Hiện thị bàn theo id bàn", description = "API này dùng để lấy thông tin bàn theo id bàn")
    @GetMapping("/{id}")
    public ApiResponseEntity show(@PathVariable Integer id) throws Exception {
        Table table = tableService.findOrFail(id);

        Ultis.throwUnless(tablePolicy.canView(JwtGuard.userPrincipal(), table), new ForbiddenException());


        return ApiResponseEntity.success(new TableResponse(table));
    }

    @Operation(summary = "Cập nhật bàn", description = "API này dùng để cập nhật thông tin bàn")
    @PutMapping("/{id}")
    public ApiResponseEntity update(@PathVariable Integer id, @Valid @RequestBody TableUpdateRequest request) throws Exception {
        Table table = tableService.findOrFail(id);

        Ultis.throwUnless(tablePolicy.canEdit(JwtGuard.userPrincipal(), table), new ForbiddenException());

        Table updatedTable = tableService.updateTable(id, request);

        return ApiResponseEntity.success(new TableResponse(updatedTable));
    }

    @Operation(summary = "Xoá bàn", description = "API này dùng để xoá bàn theo id")
    @DeleteMapping("/{id}")
    public ApiResponseEntity delete(@PathVariable Integer id) throws Exception {
        Ultis.throwUnless(tablePolicy.canDelete(JwtGuard.userPrincipal(), tableService.findOrFail(id)), new ForbiddenException());

        tableService.deleteTable(id);

        return ApiResponseEntity.success("Table deleted successfully.");
    }

    @Operation(summary = "Kiểm tra và cập nhật trạng thái bàn", description = "API này dùng để kiểm tra và cập nhật trạng thái bàn khi có người đặt bàn")
    @GetMapping("/check-table-status")
    public ApiResponseEntity checkTableStatus()  throws Exception {
        Ultis.throwUnless(tablePolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        tableService.checkAndUpdateTableStatus();
        return ApiResponseEntity.success("Table deleted successfully.");
    }
}
