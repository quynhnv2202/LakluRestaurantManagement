package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.MenuPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.NewMenu;
import com.laklu.pos.dataObjects.response.DishResponse;
import com.laklu.pos.dataObjects.response.MenuItemResponse;
import com.laklu.pos.dataObjects.response.MenuResponse;
import com.laklu.pos.dataObjects.response.MenuItemWithDetailsResponse;
import com.laklu.pos.dataObjects.response.CategoryResponse;
import com.laklu.pos.dataObjects.response.PageResponse;
import com.laklu.pos.entities.Dish;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.entities.Category;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.mapper.MenuMapper;
import com.laklu.pos.repositories.MenuRepository;
import com.laklu.pos.services.AttachmentService;
import com.laklu.pos.services.MenuService;
import com.laklu.pos.services.CategoryService;
import com.laklu.pos.services.MenuItemService;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.MenuNameMustBeUnique;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/menus")
@Tag(name = "Menu Controller", description = "Quản lý thông tin thực đơn")
@AllArgsConstructor
public class MenuController {

    private final MenuPolicy menuPolicy;
    private final MenuService menuService;
    private final MenuMapper menuMapper;
    private final MenuRepository menuRepository;
    private final AttachmentService attachmentService;
    private final CategoryService categoryService;
    private final MenuItemService menuItemService;

    @Operation(summary = "Lấy thông tin tất cả thực đơn", description = "API này dùng để lấy danh sách tất cả thực đơn")
    @GetMapping("/")
    public ApiResponseEntity getAllMenus() throws Exception {
        Ultis.throwUnless(menuPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        List<MenuResponse> menus = menuService.getAll().stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(menus);
    }

    @Operation(summary = "Lấy thông tin tất cả thực đơn có trạng thái ENABLE", description = "API này dùng để lấy danh sách tất cả thực đơn có trạng thái ENABLE")
    @GetMapping("/enabled")
    public ApiResponseEntity getAllEnabledMenus() throws Exception {
        Ultis.throwUnless(menuPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());

        List<MenuResponse> menus = menuService.getAllByStatus(Menu.MenuStatus.ENABLE).stream()
                .map(MenuResponse::fromEntity)
                .collect(Collectors.toList());

        return ApiResponseEntity.success(menus);
    }

    @Operation(summary = "Tạo một thực đơn mới", description = "API này dùng để tạo một thực đơn mới. Khi tạo menu mới, tất cả menu khác sẽ bị chuyển sang trạng thái DISABLE")
    @PostMapping("/")
    @Transactional
    public ApiResponseEntity createMenu(@RequestBody NewMenu newMenu) throws Exception {
        Ultis.throwUnless(menuPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());

        validateMenuName(newMenu.getName());

        // Vô hiệu hóa tất cả menu hiện có
        menuService.disableAllMenus();

        // Tạo menu mới với trạng thái ENABLE
        Menu menu = new Menu();
        menu.setName(newMenu.getName());
        menu.setStartAt(newMenu.getStartAt());
        menu.setEndAt(newMenu.getEndAt());
        menu.setStatus(Menu.MenuStatus.ENABLE);

        Menu createdMenu = menuService.createMenu(menu);
        return ApiResponseEntity.success(MenuResponse.fromEntity(createdMenu));
    }

    @Operation(summary = "Lấy thông tin thực đơn theo ID", description = "API này dùng để lấy thông tin thực đơn theo ID")
    @GetMapping("/{id}")
    public ApiResponseEntity getMenuById(@PathVariable Integer id) throws Exception {
        Menu menu = menuService.findOrFail(id);
        Ultis.throwUnless(menuPolicy.canView(JwtGuard.userPrincipal(), menu), new ForbiddenException());

        MenuResponse menuResponse = MenuResponse.fromEntity(menu);

        menuResponse.setMenuItems(menu.getMenuItems().stream().map((menuItem)-> {
            Dish dish = menuItem.getDish();
            DishResponse dishResponse = DishResponse.fromEntity(dish);
            dishResponse.setImages(dish.getAttachments().stream().map(attachmentService::toPersistAttachmentResponse).collect(Collectors.toList()));
            MenuItemResponse menuItemResponse = MenuItemResponse.fromEntity(menuItem);
            menuItemResponse.setDish(dishResponse);
            return menuItemResponse;
        }).collect(Collectors.toList()));

        return ApiResponseEntity.success(menuResponse);
    }

    @Operation(summary = "Cập nhật một phần thông tin thực đơn", description = "API này dùng để cập nhật một phần thông tin thực đơn theo ID. Nếu status được đổi thành ENABLE, tất cả các menu khác sẽ bị chuyển sang DISABLE")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponseEntity partialUpdateMenu(@PathVariable Integer id, @RequestBody NewMenu partialUpdateMenu) throws Exception {
        Menu existingMenu = menuService.findOrFail(id);
        Ultis.throwUnless(menuPolicy.canEdit(JwtGuard.userPrincipal(), existingMenu), new ForbiddenException());

        //validateMenuName(partialUpdateMenu.getName());

        // Lưu trạng thái cũ của menu
        Menu.MenuStatus oldStatus = existingMenu.getStatus();
        
        // Cập nhật menu từ dữ liệu gửi đến
        menuMapper.updateMenuFromDto(partialUpdateMenu, existingMenu);
        
        // Nếu trạng thái được đổi từ DISABLE sang ENABLE
        if (oldStatus == Menu.MenuStatus.DISABLE && existingMenu.getStatus() == Menu.MenuStatus.ENABLE) {
            // Vô hiệu hóa tất cả các menu khác
            menuService.disableAllMenus();
            // Đảm bảo menu hiện tại được đặt lại thành ENABLE sau khi gọi disableAllMenus()
            existingMenu.setStatus(Menu.MenuStatus.ENABLE);
        }

        Menu updatedMenu = menuService.updateMenu(existingMenu);
        return ApiResponseEntity.success(MenuResponse.fromEntity(updatedMenu));
    }

    @Operation(summary = "Xóa thực đơn theo ID", description = "API này dùng để xóa thực đơn, không thể xóa thực đơn đã có đơn hàng")
    @DeleteMapping("/{id}")
    public ApiResponseEntity deleteMenu(@PathVariable Integer id) throws Exception {
        Menu menu = menuService.findOrFail(id);
        Ultis.throwUnless(menuPolicy.canDelete(JwtGuard.userPrincipal(), menu), new ForbiddenException());

        // Kiểm tra xem menu có thể xóa được không
        if (!menuService.isMenuDeletable(menu)) {
            return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, "Không thể xóa thực đơn đã có đơn hàng");
        }

        menuService.deleteMenu(menu);
        return ApiResponseEntity.success("Xóa thực đơn thành công");
    }

    private void validateMenuName(String name) {
        MenuNameMustBeUnique rule = new MenuNameMustBeUnique(menuRepository::findByName, name);
        if (!rule.isValid()) {
            throw new IllegalArgumentException(rule.getMessage());
        }
    }
    
    @Operation(summary = "Lấy danh sách món ăn theo menu", description = "API này dùng để lấy danh sách món ăn theo menu, có thể lọc theo category và sắp xếp theo tên món ăn, hỗ trợ phân trang")
    @GetMapping("/{id}/dishes")
    public ApiResponseEntity getDishesByMenu(
            @PathVariable Integer id,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        
        Menu menu = menuService.findOrFail(id);
        Ultis.throwUnless(menuPolicy.canView(JwtGuard.userPrincipal(), menu), new ForbiddenException());
        
        // Tạo đối tượng Pageable cho phân trang, sắp xếp theo tên món ăn
        Pageable pageable = PageRequest.of(page, size, Sort.by("dish.name").ascending());
        
        Page<MenuItem> menuItemPage;
        
        if (categoryId != null) {
            // Lấy category từ categoryId
            Category category = categoryService.findOrFail(categoryId);
            
            if (activeOnly != null) {
                // Lấy danh sách món ăn theo menu, category và trạng thái active
                menuItemPage = menuItemService.findByMenuAndCategoryAndIsActive(menu, category, activeOnly, pageable);
            } else {
                // Lấy danh sách món ăn theo menu và category bất kể active hay không
                menuItemPage = menuItemService.findByMenuAndCategory(menu, category, pageable);
            }
        } else {
            if (activeOnly != null) {
                // Lấy danh sách món ăn theo menu và trạng thái active
                menuItemPage = menuItemService.findByMenuAndIsActive(menu, activeOnly, pageable);
            } else {
                // Lấy tất cả món ăn theo menu
                menuItemPage = menuItemService.findByMenu(menu, pageable);
            }
        }
        
        // Chuyển đổi từ entity sang response
        List<MenuItemWithDetailsResponse> menuItems = menuItemPage.getContent().stream()
                .map(menuItem -> {
                    Dish dish = menuItem.getDish();
                    DishResponse dishResponse = DishResponse.fromEntity(dish);
                    dishResponse.setImages(dish.getAttachments().stream()
                            .map(attachmentService::toPersistAttachmentResponse)
                            .collect(Collectors.toList()));
                    
                    CategoryResponse categoryResponse = CategoryResponse.fromEntity(menuItem.getCategory());
                    
                    return MenuItemWithDetailsResponse.fromEntity(menuItem, dishResponse, categoryResponse);
                })
                .collect(Collectors.toList());
        
        // Tạo PageResponse từ Page và danh sách đã chuyển đổi
        PageResponse<MenuItemWithDetailsResponse> pageResponse = PageResponse.fromPage(menuItemPage, menuItems);
        
        return ApiResponseEntity.success(pageResponse);
    }

    @Operation(summary = "Tìm kiếm món ăn theo tên trong menu", description = "API này dùng để tìm kiếm món ăn theo tên trong menu, có thể lọc theo category và trạng thái active, hỗ trợ phân trang")
    @GetMapping("/{id}/dishes/search")
    public ApiResponseEntity searchDishesByNameInMenu(
            @PathVariable Integer id,
            @RequestParam String dishName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        
        Menu menu = menuService.findOrFail(id);
        Ultis.throwUnless(menuPolicy.canView(JwtGuard.userPrincipal(), menu), new ForbiddenException());
        
        // Tạo đối tượng Pageable cho phân trang, sắp xếp theo tên món ăn
        Pageable pageable = PageRequest.of(page, size, Sort.by("dish.name").ascending());
        
        Page<MenuItem> menuItemPage;
        
        if (categoryId != null) {
            // Lấy category từ categoryId
            Category category = categoryService.findOrFail(categoryId);
            
            if (activeOnly != null) {
                // Tìm kiếm món ăn theo tên, menu, category và trạng thái active
                menuItemPage = menuItemService.findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(
                        menu, category, activeOnly, dishName, pageable);
            } else {
                // Tìm kiếm món ăn theo tên, menu và category bất kể active hay không
                menuItemPage = menuItemService.findByMenuAndCategoryAndDishNameContainingIgnoreCase(
                        menu, category, dishName, pageable);
            }
        } else {
            if (activeOnly != null) {
                // Tìm kiếm món ăn theo tên, menu và trạng thái active
                menuItemPage = menuItemService.findByMenuAndIsActiveAndDishNameContainingIgnoreCase(
                        menu, activeOnly, dishName, pageable);
            } else {
                // Tìm kiếm tất cả món ăn theo tên và menu
                menuItemPage = menuItemService.findByMenuAndDishNameContainingIgnoreCase(menu, dishName, pageable);
            }
        }
        
        // Chuyển đổi từ entity sang response
        List<MenuItemWithDetailsResponse> menuItems = menuItemPage.getContent().stream()
                .map(menuItem -> {
                    Dish dish = menuItem.getDish();
                    DishResponse dishResponse = DishResponse.fromEntity(dish);
                    dishResponse.setImages(dish.getAttachments().stream()
                            .map(attachmentService::toPersistAttachmentResponse)
                            .collect(Collectors.toList()));
                    
                    CategoryResponse categoryResponse = CategoryResponse.fromEntity(menuItem.getCategory());
                    
                    return MenuItemWithDetailsResponse.fromEntity(menuItem, dishResponse, categoryResponse);
                })
                .collect(Collectors.toList());
        
        // Tạo PageResponse từ Page và danh sách đã chuyển đổi
        PageResponse<MenuItemWithDetailsResponse> pageResponse = PageResponse.fromPage(menuItemPage, menuItems);
        
        return ApiResponseEntity.success(pageResponse);
    }
}