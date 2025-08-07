package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Integer id;
    private Integer dishId;
    private Integer menuId;
    private Long categoryId;
    private BigDecimal price;
    private Boolean isActive;
    private DishResponse dish;

    public MenuItemResponse(Integer id, Integer dishId, Integer menuId, Long categoryId, BigDecimal price, Boolean isActive) {
        this.id = id;
        this.dishId = dishId;
        this.menuId = menuId;
        this.categoryId = categoryId;
        this.price = price;
        this.isActive = isActive;
    }

    public static MenuItemResponse fromEntity(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getDish().getId(),
                menuItem.getMenu().getId(),
                menuItem.getCategory().getId(),
                menuItem.getPrice(),
                menuItem.getIsActive()
        );
    }
}