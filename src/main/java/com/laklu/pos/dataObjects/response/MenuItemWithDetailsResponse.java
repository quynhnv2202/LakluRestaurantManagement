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
public class MenuItemWithDetailsResponse {
    private Integer id;
    private Integer menuId;
    private BigDecimal price;
    private Boolean isActive;
    
    private DishResponse dish;
    private CategoryResponse category;
    
    public static MenuItemWithDetailsResponse fromEntity(MenuItem menuItem, DishResponse dishResponse, CategoryResponse categoryResponse) {
        return MenuItemWithDetailsResponse.builder()
                .id(menuItem.getId())
                .menuId(menuItem.getMenu().getId())
                .price(menuItem.getPrice())
                .isActive(menuItem.getIsActive())
                .dish(dishResponse)
                .category(categoryResponse)
                .build();
    }
} 