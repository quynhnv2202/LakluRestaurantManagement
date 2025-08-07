package com.laklu.pos.services;

import com.laklu.pos.entities.Menu;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.MenuRepository;
import com.laklu.pos.validator.MenuNameMustBeUnique;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final OrderItemService orderItemService;

    public List<Menu> getAll() {
        return menuRepository.findAll();
    }

    public List<Menu> getAllByStatus(Menu.MenuStatus status) {
        return menuRepository.findByStatus(status);
    }

    public Optional<Menu> findById(Integer id) {
        return menuRepository.findById(id);
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        if (menu.getStatus() == null) {
            menu.setStatus(Menu.MenuStatus.ENABLE);
        }
        return menuRepository.save(menu);
    }

    public Menu updateMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    @Transactional
    public void disableAllMenus() {
        List<Menu> menus = menuRepository.findAll();
        for (Menu menu : menus) {
            menu.setStatus(Menu.MenuStatus.DISABLE);
        }
        menuRepository.saveAll(menus);
    }

    public Menu findOrFail(Integer id) {
        return findById(id).orElseThrow(NotFoundException::new);
    }

    public void deleteMenu(Menu menu) {
        menuRepository.delete(menu);
    }

    public Optional<Menu> findByName(String name) {
        return menuRepository.findByName(name);
    }

    /**
     * Kiểm tra xem menu có thể xóa được hay không
     * @param menu Menu cần kiểm tra
     * @return true nếu menu có thể xóa được, false nếu menu đã có đơn hàng
     */
    public boolean isMenuDeletable(Menu menu) {
        return !orderItemService.existsByMenuItems_Menu(menu);
    }
}