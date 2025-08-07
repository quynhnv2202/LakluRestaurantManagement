package com.laklu.pos.repositories;

import com.laklu.pos.dataObjects.response.TopSellingDishResponse;
import com.laklu.pos.entities.OrderItem;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.entities.Order;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @Query("SELECT new com.laklu.pos.dataObjects.response.TopSellingDishResponse(" +
            "d.id, " +  // Trả về id của món ăn
            "d.name, " + // Trả về tên của món ăn
            "d.description, " +  // Trả về mô tả của món ăn
            "mi.price, " + // Trả về giá từ menuItem
            "SUM(oi.quantity)) " +  // Tổng số lượng đã bán
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +  // Kết nối với menuItem
            "JOIN mi.dish d " + // Kết nối với dish
            "WHERE oi.status = com.laklu.pos.enums.OrderItemStatus.DELIVERED " +
            "AND d.requiresPreparation = true " + // Chỉ lấy các món cần chế biến
            "GROUP BY d.id, d.name, d.description, mi.price " + // Nhóm theo món ăn và giá menuItem
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingDishResponse> findTopSellingDishes();

    /**
     * Lấy ra các món bán chạy nhất trong vòng 1 giờ gần đây
     *
     * @param startTime Thời gian bắt đầu (1 giờ trước)
     * @return Danh sách các món bán chạy nhất trong vòng 1 giờ gần đây
     */
    @Query("SELECT new com.laklu.pos.dataObjects.response.TopSellingDishResponse(" +
            "d.id, " +  // Trả về id của món ăn
            "d.name, " + // Trả về tên của món ăn
            "d.description, " +  // Trả về mô tả của món ăn
            "mi.price, " + // Trả về giá từ menuItem
            "SUM(oi.quantity)) " +  // Tổng số lượng đã bán
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +  // Kết nối với menuItem
            "JOIN mi.dish d " + // Kết nối với dish
            "JOIN oi.order o " + // Kết nối với order để lấy thời gian
            "WHERE oi.status != com.laklu.pos.enums.OrderItemStatus.CANCELLED " + // Lấy tất cả trạng thái ngoại trừ CANCELLED
            "AND oi.createdAt >= :startTime " + // Chỉ lấy những đơn hàng trong 1 giờ gần đây
            "AND d.requiresPreparation = true " + // Chỉ lấy các món cần chế biến
            "GROUP BY d.id, d.name, d.description, mi.price " + // Nhóm theo món ăn và giá menuItem
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingDishResponse> findTopSellingDishesLastHour(@Param("startTime") LocalDateTime startTime);

    /**
     * Lấy tổng số các món bán được trong khoảng thời gian từ 4h chiều đến 3h sáng hôm sau
     *
     * @param afternoonTime Thời gian bắt đầu (4h chiều)
     * @param morningTime Thời gian kết thúc (3h sáng hôm sau)
     * @return Danh sách các món bán được và số lượng
     */
    @Query("SELECT new com.laklu.pos.dataObjects.response.TopSellingDishResponse(" +
            "d.id, " +  // Trả về id của món ăn
            "d.name, " + // Trả về tên của món ăn
            "d.description, " +  // Trả về mô tả của món ăn
            "mi.price, " + // Trả về giá từ menuItem
            "SUM(oi.quantity)) " +  // Tổng số lượng đã bán
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +  // Kết nối với menuItem
            "JOIN mi.dish d " + // Kết nối với dish
            "JOIN oi.order o " + // Kết nối với order để lấy thời gian
            "WHERE oi.status = com.laklu.pos.enums.OrderItemStatus.DELIVERED " + // Lấy tất cả trạng thái ngoại trừ CANCELLED
            "AND ((oi.createdAt >= :afternoonTime AND oi.createdAt <= :endOfDay) " + // Từ 4h chiều đến cuối ngày
            "OR (oi.createdAt >= :startOfDay AND oi.createdAt <= :morningTime)) " + // Hoặc từ đầu ngày đến 3h sáng
            "AND d.requiresPreparation = true " + // Chỉ lấy các món cần chế biến
            "GROUP BY d.id, d.name, d.description, mi.price " + // Nhóm theo món ăn và giá menuItem
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingDishResponse> findDishSoldBetweenAfternoonAndMorning(
            @Param("afternoonTime") LocalDateTime afternoonTime,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("morningTime") LocalDateTime morningTime);
    
    /**
     * Tính tổng số món đã bán trong khoảng thời gian từ 4h chiều đến 3h sáng hôm sau
     *
     * @param afternoonTime Thời gian bắt đầu (4h chiều)
     * @param endOfDay Thời điểm cuối ngày
     * @param startOfDay Thời điểm đầu ngày hôm sau
     * @param morningTime Thời gian kết thúc (3h sáng hôm sau)
     * @return Tổng số lượng món đã bán
     */
    @Query("SELECT SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +  // Kết nối với menuItem để có thể lấy dish 
            "JOIN mi.dish d " +  // Kết nối với dish để lấy requiresPreparation
            "JOIN oi.order o " +  // Kết nối với order để lấy thời gian
            "WHERE oi.status = com.laklu.pos.enums.OrderItemStatus.DELIVERED " + // Lấy tất cả trạng thái ngoại trừ CANCELLED
            "AND ((oi.createdAt >= :afternoonTime AND oi.createdAt <= :endOfDay) " + // Từ 4h chiều đến cuối ngày
            "OR (oi.createdAt >= :startOfDay AND oi.createdAt <= :morningTime)) " +  // Hoặc từ đầu ngày đến 3h sáng
            "AND d.requiresPreparation = true")  // Chỉ lấy các món cần chế biến
    Long countTotalDishSoldBetweenAfternoonAndMorning(
            @Param("afternoonTime") LocalDateTime afternoonTime,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("morningTime") LocalDateTime morningTime);
    
    /**
     * Đếm số loại món đã bán trong khoảng thời gian từ 4h chiều đến 3h sáng hôm sau
     *
     * @param afternoonTime Thời gian bắt đầu (4h chiều)
     * @param endOfDay Thời điểm cuối ngày
     * @param startOfDay Thời điểm đầu ngày hôm sau
     * @param morningTime Thời gian kết thúc (3h sáng hôm sau)
     * @return Số loại món đã bán
     */
    @Query("SELECT COUNT(DISTINCT mi.dish.id) " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +
            "JOIN mi.dish d " +  // Kết nối với dish để lấy requiresPreparation
            "JOIN oi.order o " +  // Kết nối với order để lấy thời gian
            "WHERE oi.status = com.laklu.pos.enums.OrderItemStatus.DELIVERED " + // Lấy tất cả trạng thái ngoại trừ CANCELLED
            "AND ((oi.createdAt >= :afternoonTime AND oi.createdAt <= :endOfDay) " + // Từ 4h chiều đến cuối ngày
            "OR (oi.createdAt >= :startOfDay AND oi.createdAt <= :morningTime)) " +  // Hoặc từ đầu ngày đến 3h sáng
            "AND d.requiresPreparation = true")  // Chỉ lấy các món cần chế biến
    Long countTotalDishTypesSoldBetweenAfternoonAndMorning(
            @Param("afternoonTime") LocalDateTime afternoonTime,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("morningTime") LocalDateTime morningTime);

    Optional<OrderItem> findByOrderAndMenuItem(Order order, MenuItem menuItem);
    
    /**
     * Tìm các OrderItem theo MenuItem và status
     * @param menuItem MenuItem cần tìm
     * @param status Trạng thái của OrderItem
     * @return Danh sách các OrderItem thỏa mãn điều kiện
     */
    List<OrderItem> findByMenuItemAndStatus(MenuItem menuItem, OrderItemStatus status);
    
    /**
     * Tìm các OrderItem theo MenuItem, status và thời gian tạo từ một thời điểm
     * @param menuItem MenuItem cần tìm
     * @param status Trạng thái của OrderItem
     * @param fromDate Thời điểm bắt đầu (lấy các OrderItem từ thời điểm này)
     * @return Danh sách các OrderItem thỏa mãn điều kiện
     */
    List<OrderItem> findByMenuItemAndStatusAndCreatedAtGreaterThanEqual(MenuItem menuItem, OrderItemStatus status, LocalDateTime fromDate);

    /**
     * Lấy top món ăn bán chạy nhất cho từng khung giờ trong ngày
     * 
     * @param startDate Thời gian bắt đầu của ngày
     * @param endDate Thời gian kết thúc của ngày
     * @return Danh sách các món ăn bán chạy nhất theo từng giờ
     */
    @Query("SELECT HOUR(oi.createdAt) as hour, " +
            "d.id as dishId, " +
            "d.name as dishName, " +
            "d.description as dishDescription, " +
            "mi.price as dishPrice, " +
            "SUM(oi.quantity) as totalQuantity " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +
            "JOIN mi.dish d " +
            "WHERE oi.status = com.laklu.pos.enums.OrderItemStatus.DELIVERED " +
            "AND oi.createdAt BETWEEN :startDate AND :endDate " +
            "AND d.requiresPreparation = true " +
            "GROUP BY HOUR(oi.createdAt), d.id, d.name, d.description, mi.price " +
            "ORDER BY HOUR(oi.createdAt), SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingDishesByHourOfDay(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Kiểm tra xem có tồn tại OrderItem nào sử dụng MenuItem thuộc về menu cụ thể không
     * 
     * @param menu Menu cần kiểm tra
     * @return true nếu có ít nhất một OrderItem sử dụng MenuItem thuộc về menu, ngược lại là false
     */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi JOIN oi.menuItem mi WHERE mi.menu = :menu")
    boolean existsByMenuItems_Menu(Menu menu);

    /**
     * Đếm số lượng OrderItem thuộc về một Order
     * 
     * @param order Order cần đếm số lượng OrderItem
     * @return Số lượng OrderItem
     */
    long countByOrder(Order order);
}