package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.Policy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.MenuItemStatusUpdateRequest;
import com.laklu.pos.dataObjects.request.NewMenuItem;
import com.laklu.pos.dataObjects.response.MenuItemResponse;
import com.laklu.pos.entities.Category;
import com.laklu.pos.entities.Dish;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.mapper.MenuItemMapper;
import com.laklu.pos.services.CategoryService;
import com.laklu.pos.services.DishService;
import com.laklu.pos.services.MenuItemService;
import com.laklu.pos.services.MenuService;
import com.laklu.pos.services.OrderItemService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.MenuItemMustBeUnique;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.ValidationRule;
import com.laklu.pos.validator.ValuableValidationRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/menu-items")
@Tag(name = "MenuItem Controller", description = "Quản lý thông tin mục trong thực đơn")
@AllArgsConstructor
public class MenuItemController {

    private final Policy<MenuItem> menuItemPolicy; // Giả định có Policy cho MenuItem
    private final MenuItemService menuItemService;
    private final MenuItemMapper menuItemMapper;
    private final CategoryService categoryService;
    private final DishService dishService;
    private final MenuService menuService;
    private final OrderItemService orderItemService;

    @Operation(summary = "Tạo một mục trong thực đơn mới", description = "API này dùng để tạo một mục trong thực đơn mới")
    @PostMapping("/")
    public ApiResponseEntity createMenuItem(@RequestBody NewMenuItem newMenuItem) throws Exception {
        Ultis.throwUnless(menuItemPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        Dish dish = RuleValidator.getValidatedValue(new ValuableValidationRule<>("dishId", "Món ăn không tồn tại", () -> dishService.findById(newMenuItem.getDishId())));
        Menu menu = RuleValidator.getValidatedValue(new ValuableValidationRule<>("menuId", "Thực đơn không tồn tại", () -> menuService.findById(newMenuItem.getMenuId())));
        
        // Kiểm tra xem món ăn đã tồn tại trong thực đơn chưa
        RuleValidator.validate(new MenuItemMustBeUnique(menuItemService, menu, dish));
        
        // Validate price
        this.validatePrice(newMenuItem.getPrice());

        MenuItem menuItem = menuItemMapper.toEntity(newMenuItem);

        menuItem.setDish(dish);
        menuItem.setMenu(menu);
        MenuItem createdMenuItem = menuItemService.createMenuItem(menuItem);
        return ApiResponseEntity.success(MenuItemResponse.fromEntity(createdMenuItem));
    }

    @Operation(summary = "Lấy thông tin mục trong thực đơn theo ID", description = "API này dùng để lấy thông tin mục trong thực đơn theo ID")
    @GetMapping("/{id}")
    public ApiResponseEntity getMenuItemById(@PathVariable Integer id) throws Exception {
        MenuItem menuItem = menuItemService.findOrFail(id);
        Ultis.throwUnless(menuItemPolicy.canView(JwtGuard.userPrincipal(), menuItem), new ForbiddenException());

        return ApiResponseEntity.success(MenuItemResponse.fromEntity(menuItem));
    }

    @Operation(summary = "Cập nhật thông tin mục trong thực đơn", description = "API này dùng để cập nhật thông tin mục trong thực đơn theo ID")
    @PutMapping("/{id}")
    public ApiResponseEntity updateMenuItem(@PathVariable Integer id, @RequestBody NewMenuItem menuItemDetails) throws Exception {
        MenuItem existingMenuItem = menuItemService.findOrFail(id);
        Ultis.throwUnless(menuItemPolicy.canEdit(JwtGuard.userPrincipal(), existingMenuItem), new ForbiddenException());
        Dish dish = RuleValidator.getValidatedValue(new ValuableValidationRule<>("dishId", "Món ăn không tồn tại", () -> dishService.findById(menuItemDetails.getDishId())));
        Menu menu = RuleValidator.getValidatedValue(new ValuableValidationRule<>("menuId", "Thực đơn không tồn tại", () -> menuService.findById(menuItemDetails.getMenuId())));
        Category category = RuleValidator.getValidatedValue(new ValuableValidationRule<>("categoryId", "Danh mục không tồn tại", () -> categoryService.findById(menuItemDetails.getCategoryId())));

        // Nếu người dùng đang thay đổi món ăn hoặc thực đơn, kiểm tra xem món ăn đã tồn tại trong thực đơn chưa
        if (!existingMenuItem.getDish().getId().equals(dish.getId()) ||
            !existingMenuItem.getMenu().getId().equals(menu.getId())) {
            // Chỉ kiểm tra trùng lặp khi món ăn hoặc thực đơn thay đổi
            RuleValidator.validate(new MenuItemMustBeUnique(menuItemService, menu, dish));
        }
        
        // Validate price if provided
        this.validatePrice(menuItemDetails.getPrice());

        existingMenuItem.setDish(dish);
        existingMenuItem.setMenu(menu);
        existingMenuItem.setCategory(category);

        menuItemMapper.updateMenuItemFromDto(menuItemDetails, existingMenuItem);
        MenuItem updatedMenuItem = menuItemService.updateMenuItem(existingMenuItem);
        return ApiResponseEntity.success(MenuItemResponse.fromEntity(updatedMenuItem));
    }

    @Operation(summary = "Xóa mục trong thực đơn theo ID", description = "API này dùng để xóa mục trong thực đơn")
    @DeleteMapping("/{id}")
    public ApiResponseEntity deleteMenuItem(@PathVariable Integer id) throws Exception {
        MenuItem menuItem = menuItemService.findOrFail(id);
        Ultis.throwUnless(menuItemPolicy.canDelete(JwtGuard.userPrincipal(), menuItem), new ForbiddenException());

        menuItemService.deleteMenuItem(menuItem);
        return ApiResponseEntity.success("Xóa mục trong thực đơn thành công");
    }

    @Operation(summary = "Cập nhật trạng thái mục trong thực đơn", description = "API này dùng để chuyển đổi trạng thái kích hoạt/vô hiệu hóa của mục trong thực đơn")
    @GetMapping("/{id}/toggle-status")
    public ApiResponseEntity toggleMenuItemStatus(
            @PathVariable Integer id) throws Exception {
        
        MenuItem menuItem = menuItemService.findOrFail(id);
        Ultis.throwUnless(menuItemPolicy.canEdit(JwtGuard.userPrincipal(), menuItem), new ForbiddenException());
        
        // Đảo ngược trạng thái hiện tại
        boolean newStatus = !menuItem.getIsActive();
        menuItem.setIsActive(newStatus);
        MenuItem updatedMenuItem = menuItemService.updateMenuItem(menuItem);
        
        // Nếu trạng thái mới là false (vô hiệu hóa), hủy tất cả OrderItem PENDING liên quan
        if (!newStatus) {
            orderItemService.cancelPendingOrderItemsByMenuItem(menuItem);
        }
        
        String statusMessage = newStatus ? "kích hoạt" : "vô hiệu hóa";
        return ApiResponseEntity.success(String.format("Mục trong thực đơn đã được %s thành công", statusMessage));
    }

    private void validatePrice(BigDecimal price) {
        ValidationRule priceMustBeLessThanTwoMillion = new ValidationRule(
                (v) -> price.doubleValue() <= 2000000,
                "",
                "Giá phải nhỏ hơn hoặc bằng 2 triệu"
        );
        RuleValidator.validate(priceMustBeLessThanTwoMillion);
    }
}