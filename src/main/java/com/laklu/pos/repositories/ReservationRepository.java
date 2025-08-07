package com.laklu.pos.repositories;

import com.laklu.pos.entities.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer>, JpaSpecificationExecutor<Reservation> {
    Optional<Reservation> findById(Integer id);
    
    Page<Reservation> findByStatus(Reservation.Status status, Pageable pageable);
    
    Page<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<Reservation> findByReservationTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, Reservation.Status status, Pageable pageable);
    
    @Query("SELECT r FROM Reservation r WHERE " +
           "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "r.customerPhone LIKE CONCAT('%', :keyword, '%')")
    Page<Reservation> searchByNameOrPhone(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT r FROM Reservation r ORDER BY r.checkIn DESC")
    Page<Reservation> findAllReservations(Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.status IN ('PENDING', 'CONFIRMED') ORDER BY r.checkIn DESC")
    Page<Reservation> findAllActiveReservations(Pageable pageable);
    
    // Tìm theo checkIn
    Page<Reservation> findByCheckInBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Tìm theo checkIn và status
    Page<Reservation> findByCheckInBetweenAndStatus(LocalDateTime start, LocalDateTime end, Reservation.Status status, Pageable pageable);

    /**
     * Tìm đặt bàn có thời gian check in nằm trong một trong hai khoảng thời gian
     * 
     * @param start1 Thời gian bắt đầu khoảng 1
     * @param end1 Thời gian kết thúc khoảng 1
     * @param start2 Thời gian bắt đầu khoảng 2
     * @param end2 Thời gian kết thúc khoảng 2
     * @param pageable Phân trang
     * @return Danh sách đặt bàn
     */
    Page<Reservation> findByCheckInBetweenOrCheckInBetween(
            LocalDateTime start1, LocalDateTime end1, 
            LocalDateTime start2, LocalDateTime end2, 
            Pageable pageable);


    Page<Reservation> findByCheckInBetweenAndStatusInOrCheckInBetweenAndStatusIn(
            LocalDateTime start1, LocalDateTime end1, List<Reservation.Status> statuses,
            LocalDateTime start2, LocalDateTime end2, List<Reservation.Status> statuses2,
            Pageable pageable);
            
    /**
     * Tìm đặt bàn theo trạng thái và khoảng thời gian check-in
     * Sử dụng cho việc kiểm tra đặt bàn trễ hẹn
     */
    List<Reservation> findByStatusAndCheckInBetween(
            Reservation.Status status, 
            LocalDateTime startTime,
            LocalDateTime endTime);
}
