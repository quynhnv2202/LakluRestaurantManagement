package com.laklu.pos.repositories;

import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.entities.Category;
import com.laklu.pos.entities.Dish;
import com.laklu.pos.entities.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    boolean existsByCategory(Category category);
    
    /**
     * Kiểm tra xem một MenuItem có tồn tại với Menu và Dish cụ thể không
     * @param menu Menu cần kiểm tra
     * @param dish Dish cần kiểm tra
     * @return true nếu MenuItem với Menu và Dish này đã tồn tại, ngược lại là false
     */
    boolean existsByMenuAndDish(Menu menu, Dish dish);
    
    /**
     * Tìm tất cả các MenuItem theo Menu
     * @param menu Menu cần tìm
     * @return Danh sách các MenuItem thuộc menu
     */
    List<MenuItem> findByMenu(Menu menu);
    
    /**
     * Tìm tất cả các MenuItem theo Menu và trạng thái active
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @return Danh sách các MenuItem thuộc menu với trạng thái active cụ thể
     */
    List<MenuItem> findByMenuAndIsActive(Menu menu, Boolean isActive);
    
    /**
     * Tìm tất cả các MenuItem theo Menu và Category
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @return Danh sách các MenuItem thuộc menu và category
     */
    List<MenuItem> findByMenuAndCategory(Menu menu, Category category);
    
    /**
     * Tìm tất cả các MenuItem theo Menu, Category và trạng thái active
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @return Danh sách các MenuItem thuộc menu và category với trạng thái active cụ thể
     */
    List<MenuItem> findByMenuAndCategoryAndIsActive(Menu menu, Category category, Boolean isActive);
    
    /**
     * Tìm tất cả các MenuItem theo Menu với phân trang
     * @param menu Menu cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu với phân trang
     */
    Page<MenuItem> findByMenu(Menu menu, Pageable pageable);
    
    /**
     * Tìm tất cả các MenuItem theo Menu và trạng thái active với phân trang
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu với trạng thái active với phân trang
     */
    Page<MenuItem> findByMenuAndIsActive(Menu menu, Boolean isActive, Pageable pageable);
    
    /**
     * Tìm tất cả các MenuItem theo Menu và Category với phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu và category với phân trang
     */
    Page<MenuItem> findByMenuAndCategory(Menu menu, Category category, Pageable pageable);
    
    /**
     * Tìm tất cả các MenuItem theo Menu, Category và trạng thái active với phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu và category với trạng thái active với phân trang
     */
    Page<MenuItem> findByMenuAndCategoryAndIsActive(Menu menu, Category category, Boolean isActive, Pageable pageable);
    
    /**
     * Tìm kiếm các MenuItem theo Menu và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu có tên món ăn chứa dishName
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    List<MenuItem> findByMenuAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("dishName") String dishName);
    
    /**
     * Tìm kiếm các MenuItem theo Menu và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu có tên món ăn chứa dishName với phân trang
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    Page<MenuItem> findByMenuAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("dishName") String dishName, Pageable pageable);
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu và category có tên món ăn chứa dishName
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND mi.category = :category AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    List<MenuItem> findByMenuAndCategoryAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("category") Category category, @Param("dishName") String dishName);
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu và category có tên món ăn chứa dishName với phân trang
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND mi.category = :category AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    Page<MenuItem> findByMenuAndCategoryAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("category") Category category, @Param("dishName") String dishName, Pageable pageable);
    
    /**
     * Tìm kiếm các MenuItem theo Menu, trạng thái active và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu có trạng thái active và tên món ăn chứa dishName
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND mi.isActive = :isActive AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    List<MenuItem> findByMenuAndIsActiveAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("isActive") Boolean isActive, @Param("dishName") String dishName);
    
    /**
     * Tìm kiếm các MenuItem theo Menu, trạng thái active và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu có trạng thái active và tên món ăn chứa dishName với phân trang
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND mi.isActive = :isActive AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    Page<MenuItem> findByMenuAndIsActiveAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("isActive") Boolean isActive, @Param("dishName") String dishName, Pageable pageable);
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category, trạng thái active và tên món ăn (Dish)
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @return Danh sách các MenuItem thuộc menu, category có trạng thái active và tên món ăn chứa dishName
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND mi.category = :category AND mi.isActive = :isActive AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    List<MenuItem> findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("category") Category category, @Param("isActive") Boolean isActive, @Param("dishName") String dishName);
    
    /**
     * Tìm kiếm các MenuItem theo Menu, Category, trạng thái active và tên món ăn (Dish) có phân trang
     * @param menu Menu cần tìm
     * @param category Category cần tìm
     * @param isActive Trạng thái active cần tìm
     * @param dishName Tên món ăn cần tìm
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách MenuItem thuộc menu, category có trạng thái active và tên món ăn chứa dishName với phân trang
     */
    @Query("SELECT mi FROM MenuItem mi JOIN mi.dish d WHERE mi.menu = :menu AND mi.category = :category AND mi.isActive = :isActive AND LOWER(d.name) LIKE LOWER(CONCAT('%', :dishName, '%'))")
    Page<MenuItem> findByMenuAndCategoryAndIsActiveAndDishNameContainingIgnoreCase(@Param("menu") Menu menu, @Param("category") Category category, @Param("isActive") Boolean isActive, @Param("dishName") String dishName, Pageable pageable);
}
