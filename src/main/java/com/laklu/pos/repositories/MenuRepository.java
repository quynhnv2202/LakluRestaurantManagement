package com.laklu.pos.repositories;

import com.laklu.pos.entities.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ:
    Optional<Menu> findByName(String name); // Tìm thực đơn theo tên
    
    // Tìm thực đơn theo trạng thái
    List<Menu> findByStatus(Menu.MenuStatus status);
}
