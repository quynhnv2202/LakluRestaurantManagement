package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.UpdateReservationDTO;
import com.laklu.pos.entities.Reservation;
import com.laklu.pos.entities.ReservationTable;
import com.laklu.pos.entities.Table;
import com.laklu.pos.dataObjects.request.ReservationRequest;
import com.laklu.pos.enums.ReservationStatus;
import com.laklu.pos.exceptions.httpExceptions.BadRequestException;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.ReservationMapper;
import com.laklu.pos.repositories.ReservationRepository;
import com.laklu.pos.repositories.ReservationTableRepository;
import com.laklu.pos.repositories.TableRepository;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.TablesMustBeAvailable;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.laklu.pos.repositories.OrderRepository;
import com.laklu.pos.entities.Order;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReservationService implements ReservationByDateResolver {

    ReservationRepository reservationRepository;
    TableRepository tableRepository;
    ReservationTableRepository reservationTableRepository;
    ReservationMapper reservationMapper;
    OrderRepository orderRepository;

    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        Reservation reservation = reservationMapper.toEntity(request);

        List<Table> tables = tableRepository.findAllById(request.getTableIds());

        RuleValidator.validate(new TablesMustBeAvailable(tables, request.getCheckIn().toLocalDate(), this));

        reservation = reservationRepository.save(reservation);

        this.createReservationTables(reservation, tables, reservation.getReservationTime());

        return reservation;
    }

//    public Reservation updateReservationInfo(Reservation reservation, UpdateReservationDTO dto) {
//        reservationMapper.updateReservation(dto, reservation);
//
//        return reservationRepository.save(reservation);
//    }

    public Reservation updateReservationInfo(Integer reservationId, UpdateReservationDTO request) {
        Reservation reservation = findOrFail(reservationId);

        reservationMapper.updateReservation(request, reservation);

        return reservationRepository.save(reservation);
    }

    public Reservation addTablesToReservation(Reservation reservation, List<Integer> tableIds) {
        List<Table> tables = tableRepository.findAllExceptInReservation(tableIds, reservation);

        RuleValidator.validate(new TablesMustBeAvailable(tables, reservation.getCheckIn().toLocalDate(), this));

        this.createReservationTables(reservation, tables, LocalDateTime.now());

        return reservation;
    }

    @Transactional
    public void deleteTablesReservation(Reservation reservation, List<Integer> tableIds) {
        log.info("Attempting to delete tables with IDs: {} from reservation: {}", tableIds, reservation.getId());

        // Xóa bằng native query SQL trực tiếp
        int deletedCount = reservationTableRepository.deleteByReservationIdAndTableIds(reservation.getId(), tableIds);
        log.info("Deleted {} reservationTables using direct SQL", deletedCount);

        // Đảm bảo thay đổi được commit
        reservationRepository.flush();
    }

    public Optional<Reservation> findReservationById(Integer id) {
        return reservationRepository.findById(id);
    }

    public Reservation findOrFail(Integer id) {
        return this.findReservationById(id).orElseThrow(NotFoundException::new);
    }

    private void createReservationTables(Reservation reservation, List<Table> tables, LocalDateTime createdAt) {
        List<ReservationTable> reservationTables = tables.stream()
                .map(table -> ReservationTable.builder()
                        .reservation(reservation)
                        .table(table)
                        .createdAt(createdAt)
                        .build())
                .collect(Collectors.toList());

        this.reservationTableRepository.saveAll(reservationTables);
    }

    @Override
    public List<ReservationTable> resolveReservationsDate(LocalDate date, List<Table> tables) {

        return this.reservationTableRepository.findReservationsDate(date, tables);
    }

    @Transactional
    public Page<Reservation> getAllReservation(int page, int size) {
        // Sắp xếp theo checkIn giảm dần
        Sort sort = Sort.by(Sort.Order.desc("checkIn"));

        Pageable pageable = PageRequest.of(page, size, sort);
        return reservationRepository.findAllReservations(pageable);
    }

    public void updateReservationStatus(Reservation rsv, Reservation.Status status) {
        rsv.setStatus(status);
        reservationRepository.save(rsv);
    }

    public Page<Reservation> findByDateAndStatus(LocalDateTime start, LocalDateTime end, Reservation.Status status, int page, int size) {
        // Sắp xếp theo checkIn giảm dần
        Sort sort = Sort.by(Sort.Order.desc("checkIn"));

        Pageable pageable = PageRequest.of(page, size, sort);
        return reservationRepository.findByCheckInBetweenAndStatus(start, end, status, pageable);
    }

    public Page<Reservation> findByDate(LocalDateTime start, LocalDateTime end, int page, int size) {
        // Sắp xếp theo checkIn giảm dần
        Sort sort = Sort.by(Sort.Order.desc("checkIn"));

        Pageable pageable = PageRequest.of(page, size, sort);
        return reservationRepository.findByCheckInBetween(start, end, pageable);
    }

    public Page<Reservation> findByStatus(Reservation.Status status, int page, int size) {
        // Sắp xếp theo checkIn giảm dần
        Sort sort = Sort.by(Sort.Order.desc("checkIn"));

        Pageable pageable = PageRequest.of(page, size, sort);
        return reservationRepository.findByStatus(status, pageable);
    }

    public Page<Reservation> searchByNameOrPhone(String keyword, int page, int size) {
        // Sắp xếp theo checkIn giảm dần
        Sort sort = Sort.by(Sort.Order.desc("checkIn"));

        Pageable pageable = PageRequest.of(page, size, sort);
        return reservationRepository.searchByNameOrPhone(keyword, pageable);
    }

    /**
     * Lấy đặt bàn trong ngày hôm nay
     */
    public Page<Reservation> findToday(int page, int size) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);

        return findByDate(startOfDay, endOfDay, page, size);
    }

    /**
     * Lấy đặt bàn trong ngày hôm qua
     */
    public Page<Reservation> findYesterday(int page, int size) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay().minusSeconds(1);

        return findByDate(startOfDay, endOfDay, page, size);
    }

    /**
     * Lấy đặt bàn trong tuần này
     */
    public Page<Reservation> findThisWeek(int page, int size) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1); // Tuần bắt đầu từ Thứ 2
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.plusDays(1).atStartOfDay().minusSeconds(1);

        return findByDate(startDateTime, endDateTime, page, size);
    }

    /**
     * Lấy đặt bàn trong tháng này
     */
    public Page<Reservation> findThisMonth(int page, int size) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.plusDays(1).atStartOfDay().minusSeconds(1);

        return findByDate(startDateTime, endDateTime, page, size);
    }

    /**
     * Chuyển đổi bàn từ đặt bàn này sang bàn khác
     *
     * @param reservation  Đặt bàn cần chuyển đổi bàn
     * @param fromTableIds Danh sách ID của các bàn cũ cần thay thế
     * @param toTableIds   Danh sách ID của các bàn mới để thay thế
     * @throws RuntimeException Nếu một trong các bàn mới đang được sử dụng hoặc có lỗi trong yêu cầu
     */
    @Transactional
    public void transferTables(Reservation reservation, List<Integer> fromTableIds, List<Integer> toTableIds) {
        // Kiểm tra tính hợp lệ của đầu vào
        if (fromTableIds == null || fromTableIds.isEmpty()) {
            throw new RuntimeException("Danh sách bàn cần chuyển không được để trống");
        }
        if (toTableIds == null || toTableIds.isEmpty()) {
            throw new RuntimeException("Danh sách bàn mới không được để trống");
        }

        // Kiểm tra xem tất cả bàn cũ có thuộc đặt bàn không
        boolean allTablesInReservation = fromTableIds.stream()
                .allMatch(tableId -> reservation.getReservationTables().stream()
                        .anyMatch(rt -> rt.getTable().getId().equals(tableId)));

        if (!allTablesInReservation) {
            throw new RuntimeException("Một hoặc nhiều bàn không thuộc đặt bàn này");
        }

        // Tìm những bàn mới thực sự (loại bỏ các bàn đã có trong danh sách bàn cũ)
        List<Integer> actualNewTableIds = toTableIds.stream()
                .filter(tableId -> !reservation.getReservationTables().stream()
                        .anyMatch(rt -> rt.getTable().getId().equals(tableId)))
                .collect(Collectors.toList());

        // Kiểm tra xem các bàn mới có đang được sử dụng không
        if (!actualNewTableIds.isEmpty()) {
            LocalDate reservationDate = reservation.getCheckIn().toLocalDate();
            boolean anyTableInUse = reservationTableRepository.areTablesInUseByOtherReservationOnDate(
                    actualNewTableIds, reservation.getId(), reservationDate);

            if (anyTableInUse) {
                throw new RuntimeException("Một hoặc nhiều bàn mới đang được sử dụng trong đặt bàn khác vào ngày " + reservationDate);
            }
        }

        // Lấy danh sách bàn mới
        List<Table> newTables = tableRepository.findAllById(actualNewTableIds);
        if (newTables.size() != actualNewTableIds.size() && !actualNewTableIds.isEmpty()) {
            throw new RuntimeException("Một hoặc nhiều bàn mới không tồn tại");
        }

        // Lưu lại danh sách các ReservationTable hiện tại không bị ảnh hưởng
        List<ReservationTable> unchangedReservationTables = reservation.getReservationTables().stream()
                .filter(rt -> !fromTableIds.contains(rt.getTable().getId()))
                .collect(Collectors.toList());

        // Xóa các bàn cũ trong cơ sở dữ liệu
        int deletedCount = reservationTableRepository.deleteByReservationIdAndTableIds(reservation.getId(), fromTableIds);
        log.info("Đã xóa {} bàn cũ: {}", deletedCount, fromTableIds);

        // Cập nhật lại danh sách ReservationTable của reservation
        reservation.setReservationTables(unchangedReservationTables);

        // Tạo các ReservationTable mới
        if (!actualNewTableIds.isEmpty()) {
            List<ReservationTable> newReservationTables = newTables.stream()
                    .map(table -> {
                        ReservationTable rt = new ReservationTable();
                        rt.setReservation(reservation);
                        rt.setTable(table);
                        rt.setCreatedAt(LocalDateTime.now());
                        return rt;
                    })
                    .collect(Collectors.toList());

            // Lưu các ReservationTable mới
            newReservationTables = reservationTableRepository.saveAll(newReservationTables);

            // Thêm các ReservationTable mới vào danh sách
            reservation.getReservationTables().addAll(newReservationTables);

            log.info("Đã thêm {} bàn mới: {}", newReservationTables.size(), actualNewTableIds);
        }

        // Lưu đặt bàn đã cập nhật
        log.info("Đã đổi từ bàn {} sang bàn {}", fromTableIds, toTableIds);
        reservationRepository.save(reservation);
    }

    /**
     * Lấy danh sách đặt bàn có thời gian check in từ 4h chiều đến 3h sáng hôm sau
     * Phương thức này tự động tính toán "ca làm việc hiện tại" dựa vào thời điểm gọi API
     *
     * @param page Số trang
     * @param size Kích thước trang
     * @return Danh sách đặt bàn có thời gian check in từ 4h chiều đến 3h sáng hôm sau
     */
    public Page<Reservation> findEveningToEarlyMorning(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate currentDay = now.toLocalDate();
        LocalDateTime afternoonTime;
        LocalDateTime earlyMorningTime;

        // Kiểm tra xem thời điểm hiện tại có nằm trong khoảng 0h-3h sáng không
        if (now.getHour() >= 0 && now.getHour() <= 3) {
            // Nếu đang trong khoảng 0h-3h sáng, truy vấn dữ liệu từ 16h hôm trước đến 3h sáng hôm nay
            afternoonTime = currentDay.minusDays(1).atTime(16, 0);
            earlyMorningTime = currentDay.atTime(3, 0);
        } else {
            // Ngược lại, truy vấn dữ liệu từ 16h hôm nay đến 3h sáng hôm sau
            afternoonTime = currentDay.atTime(16, 0);
            earlyMorningTime = currentDay.plusDays(1).atTime(3, 0);
        }

        // Thời điểm cuối ngày
        LocalDateTime endOfDay = afternoonTime.toLocalDate().atTime(23, 59, 59);

        // Thời điểm đầu ngày
        LocalDateTime startOfDay = earlyMorningTime.toLocalDate().atStartOfDay();

        Pageable pageable = PageRequest.of(page, size, Sort.by("checkIn").ascending());

        // Lấy đặt bàn có thời gian check in nằm trong khoảng
        return reservationRepository.findByCheckInBetweenOrCheckInBetween(
                afternoonTime, endOfDay,
                startOfDay, earlyMorningTime,
                pageable);
    }

    public Page<Reservation> findEveningToEarlyMorningActiveReservations(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate currentDay = now.toLocalDate();
        LocalDateTime afternoonTime;
        LocalDateTime earlyMorningTime;

        if (now.getHour() >= 0 && now.getHour() <= 3) {
            afternoonTime = currentDay.minusDays(1).atTime(16, 0);
            earlyMorningTime = currentDay.atTime(3, 0);
        } else {
            afternoonTime = currentDay.atTime(16, 0);
            earlyMorningTime = currentDay.plusDays(1).atTime(3, 0);
        }

//        LocalDateTime endOfDay = afternoonTime.toLocalDate().atTime(23, 59, 59);
//        LocalDateTime startOfDay = earlyMorningTime.toLocalDate().atStartOfDay();

        LocalDateTime startOfDay = currentDay.atStartOfDay();
        LocalDateTime endOfDay = currentDay.atTime(23, 59, 59);

        List<Reservation.Status> statuses = Arrays.asList(Reservation.Status.PENDING, Reservation.Status.CONFIRMED);
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkIn").ascending());

        return reservationRepository.findByCheckInBetweenAndStatusInOrCheckInBetweenAndStatusIn(
                afternoonTime, endOfDay, statuses,
                startOfDay, earlyMorningTime, statuses,
                pageable);
    }

    /**
     * Kiểm tra xem đặt bàn có đơn hàng active nào không
     * @param reservation Đặt bàn cần kiểm tra
     * @return true nếu có đơn hàng active, false nếu không có
     */
    public boolean hasActiveOrders(Reservation reservation) {
        List<Order> orders = orderRepository.findByReservationIdAndStatusNotCancelledOrCompleted(reservation.getId());
        return !orders.isEmpty();
    }

    /**
     * Scheduled task để kiểm tra và tự động hủy đặt bàn nếu khách hàng đến muộn sau 30 phút
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 milliseconds
    @Transactional
    public void cancelLateReservations() {
        log.info("Bắt đầu kiểm tra đặt bàn trễ hẹn...");
        LocalDateTime now = LocalDateTime.now();
        
        // Tìm tất cả đặt bàn có trạng thái PENDING với thời gian checkin trước thời điểm hiện tại
        // và không quá 2 giờ trước để tránh hủy các đặt bàn đã quá cũ
        LocalDateTime cutoffTime = now.minusHours(2);
        List<Reservation> pendingReservations = reservationRepository.findByStatusAndCheckInBetween(
                Reservation.Status.PENDING, 
                cutoffTime, 
                now);
        
        int cancelCount = 0;
        for (Reservation reservation : pendingReservations) {
            // Kiểm tra xem đã quá 30 phút kể từ thời gian check-in chưa
            LocalDateTime lateThreshold = reservation.getCheckIn().plusMinutes(30);
            
            if (now.isAfter(lateThreshold)) {
                log.info("Hủy đặt bàn ID {} của khách hàng {} vì đến muộn. Check-in dự kiến: {}", 
                        reservation.getId(), reservation.getCustomerName(), reservation.getCheckIn());
                
                updateReservationStatus(reservation, Reservation.Status.CANCELLED);
                cancelCount++;
            }
        }
        
        log.info("Đã hoàn thành kiểm tra. Tổng số đặt bàn bị hủy do trễ hẹn: {}", cancelCount);
    }
}