package com.laklu.pos.services;

import com.laklu.pos.entities.Category;
import com.laklu.pos.entities.Dish;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.MenuItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItem> getAll() {
        return menuItemRepository.findAll();
    }

    public Optional<MenuItem> findById(Integer id) {
        return menuItemRepository.findById(id);
    }

    public MenuItem createMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public MenuItem updateMenuItem(MenuItem menuItem) {
        return menuItemRepository.save(menuItem);
    }

    public void deleteMenuItem(MenuItem menuItem) {
        menuItemRepository.delete(menuItem);
    }


    public MenuItem findOrFail(Integer id) {
        return findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public List<MenuItem>  findAllByIds(List<Integer> menuItemIds) {
        return menuItemRepository.findAllById(menuItemIds);
    }
    
    /**
     * Kiểm tra xem một MenuItem với Menu và Dish cụ thể đã tồn tại chưa
     * @param menu Menu cần kiểm tra
     * @param dish Dish cần kiểm tra
     * @return true nếu MenuItem với Menu và Dish này đã tồn tại, ngược lại là false
     */
    public boolean existsByMenuAndDish(Menu menu, Dish dish) {
        return menuItemRepository.existsByMenuAndDish(menu, dish);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu
     * @param menu Menu cần tìm
     * @return Danh sách các MenuItem thuộc menu
     */
    public List<MenuItem> findByMenu(Menu menu) {
        return menuItemRepository.findByMenu(menu);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu với trạng thái active
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @return Danh sách các MenuItem thuộc menu với trạng thái active
     */
    public List<MenuItem> findByMenuAndIsActive(Menu menu, Boolean isActive) {
        return menuItemRepository.findByMenuAndIsActive(menu, isActive);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu và Category
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @return Danh sách các MenuItem thuộc menu và category
     */
    public List<MenuItem> findByMenuAndCategory(Menu menu, Category category) {
        return menuItemRepository.findByMenuAndCategory(menu, category);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu, Category và trạng thái active
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @return Danh sách các MenuItem thuộc menu và category với trạng thái active
     */
    public List<MenuItem> findByMenuAndCategoryAndIsActive(Menu menu, Category category, Boolean isActive) {
        return menuItemRepository.findByMenuAndCategoryAndIsActive(menu, category, isActive);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu với phân trang
     * @param menu Menu cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu với phân trang
     */
    public Page<MenuItem> findByMenu(Menu menu, Pageable pageable) {
        return menuItemRepository.findByMenu(menu, pageable);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu với trạng thái active với phân trang
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu với trạng thái active với phân trang
     */
    public Page<MenuItem> findByMenuAndIsActive(Menu menu, Boolean isActive, Pageable pageable) {
        return menuItemRepository.findByMenuAndIsActive(menu, isActive, pageable);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu và Category với phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu và category với phân trang
     */
    public Page<MenuItem> findByMenuAndCategory(Menu menu, Category category, Pageable pageable) {
        return menuItemRepository.findByMenuAndCategory(menu, category, pageable);
    }
    
    /**
     * Lấy danh sách các MenuItem theo Menu, Category và trạng thái active với phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu và category với trạng thái active với phân trang
     */
    public Page<MenuItem> findByMenuAndCategoryAndIsActive(Menu menu, Category category, Boolean isActive, Pageable pageable) {
        return menuItemRepository.findByMenuAndCategoryAndIsActive(menu, category, isActive, pageable);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu có tên món ăn chứa dishName
     */
    public List<MenuItem> findByMenuAndDishNameContainingIgnoreCase(Menu menu, String dishName) {
        return menuItemRepository.findByMenuAndDishNameContainingIgnoreCase(menu, dishName);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu có tên món ăn chứa dishName với phân trang
     */
    public Page<MenuItem> findByMenuAndDishNameContainingIgnoreCase(Menu menu, String dishName, Pageable pageable) {
        return menuItemRepository.findByMenuAndDishNameContainingIgnoreCase(menu, dishName, pageable);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu và category có tên món ăn chứa dishName
     */
    public List<MenuItem> findByMenuAndCategoryAndDishNameContainingIgnoreCase(Menu menu, Category category, String dishName) {
        return menuItemRepository.findByMenuAndCategoryAndDishNameContainingIgnoreCase(menu, category, dishName);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu và category có tên món ăn chứa dishName với phân trang
     */
    public Page<MenuItem> findByMenuAndCategoryAndDishNameContainingIgnoreCase(Menu menu, Category category, String dishName, Pageable pageable) {
        return menuItemRepository.findByMenuAndCategoryAndDishNameContainingIgnoreCase(menu, category, dishName, pageable);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu, trạng thái active và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu có trạng thái active và tên món ăn chứa dishName
     */
    public List<MenuItem> findByMenuAndIsActiveAndDishNameContainingIgnoreCase(Menu menu, Boolean isActive, String dishName) {
        return menuItemRepository.findByMenuAndIsActiveAndDishNameContainingIgnoreCase(menu, isActive, dishName);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu, trạng thái active và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu có trạng thái active và tên món ăn chứa dishName với phân trang
     */
    public Page<MenuItem> findByMenuAndIsActiveAndDishNameContainingIgnoreCase(Menu menu, Boolean isActive, String dishName, Pageable pageable) {
        return menuItemRepository.findByMenuAndIsActiveAndDishNameContainingIgnoreCase(menu, isActive, dishName, pageable);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category, trạng thái active và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu, category có trạng thái active và tên món ăn chứa dishName
     */
    public List<MenuItem> findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(Menu menu, Category category, Boolean isActive, String dishName) {
        return menuItemRepository.findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(menu, category, isActive, dishName);
    }
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category, trạng thái active và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu, category có trạng thái active và tên món ăn chứa dishName với phân trang
     */
    public Page<MenuItem> findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(Menu menu, Category category, Boolean isActive, String dishName, Pageable pageable) {
        return menuItemRepository.findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(menu, category, isActive, dishName, pageable);
    }
}
