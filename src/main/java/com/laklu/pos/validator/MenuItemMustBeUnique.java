package com.laklu.pos.validator;

import com.laklu.pos.entities.Dish;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.services.MenuItemService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MenuItemMustBeUnique extends BaseRule {
    private final MenuItemService menuItemService;
    private final Menu menu;
    private final Dish dish;

    @Override
    public String getValidateField() {
        return null;
    }

    @Override
    public boolean isValid() {
        // Trả về true nếu không tìm thấy MenuItem với menu và dish này
        return !menuItemService.existsByMenuAndDish(menu, dish);
    }

    @Override
    public String getMessage() {
        return "Món ăn này đã tồn tại trong thực đơn này";
    }
} 