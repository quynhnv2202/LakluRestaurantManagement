package com.laklu.pos.repositories;

import com.laklu.pos.entities.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer> {

    Optional<Dish> findByName(String name); // Tìm món ăn theo tên chính xác
    
    Page<Dish> findAll(Pageable pageable); // Lấy danh sách món ăn có phân trang
    
    List<Dish> findByNameContainingIgnoreCase(String name); // Tìm món ăn theo tên (tìm kiếm mờ)
    
    Page<Dish> findByNameContainingIgnoreCase(String name, Pageable pageable); // Tìm món ăn theo tên có phân trang
}
