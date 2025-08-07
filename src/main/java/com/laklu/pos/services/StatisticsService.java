package com.laklu.pos.services;

import com.laklu.pos.dataObjects.response.DailyRevenueResponse;
import com.laklu.pos.dataObjects.response.DishSummaryResponse;
import com.laklu.pos.dataObjects.response.HourlyTopSellingDishResponse;
import com.laklu.pos.dataObjects.response.MonthlyRevenueResponse;
import com.laklu.pos.dataObjects.response.TopSellingDishResponse;
import com.laklu.pos.entities.Payment;
import com.laklu.pos.enums.PaymentStatus;
import com.laklu.pos.repositories.OrderItemRepository;
import com.laklu.pos.repositories.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class StatisticsService {

    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public List<TopSellingDishResponse> getTop3SellingDishes() {
        List<TopSellingDishResponse> allTopDishes = orderItemRepository.findTopSellingDishes();
        return allTopDishes.stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<TopSellingDishResponse> getTopSellingDishesLastHour() {
        // Tính thời điểm 1 giờ trước hiện tại
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // Lấy tất cả các món bán chạy trong 1 giờ gần đây
        List<TopSellingDishResponse> topDishes = orderItemRepository.findTopSellingDishesLastHour(oneHourAgo);
        
        // Trả về tất cả các món (không giới hạn số lượng)
        return topDishes.stream()
                .limit(3)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách các món bán được từ 4h chiều đến 3h sáng hôm sau
     *
     * @return Danh sách các món bán được và số lượng
     */
    public List<TopSellingDishResponse> getDishSoldBetweenAfternoonAndMorning() {
        LocalDate today = LocalDate.now();
        
        // Thời điểm 4h chiều hôm nay
        LocalDateTime afternoonTime = today.atTime(16, 0);
        
        // Thời điểm cuối ngày hôm nay
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // Thời điểm đầu ngày hôm sau
        LocalDateTime startOfDay = today.plusDays(1).atStartOfDay();
        
        // Thời điểm 3h sáng hôm sau
        LocalDateTime morningTime = today.plusDays(1).atTime(3, 0);
        
        // Nếu hiện tại là buổi sáng (trước 16h), lấy khoảng thời gian từ 16h hôm trước đến 3h sáng hôm nay
        if (LocalDateTime.now().isBefore(afternoonTime)) {
            afternoonTime = today.minusDays(1).atTime(16, 0);
            endOfDay = today.minusDays(1).atTime(23, 59, 59);
            startOfDay = today.atStartOfDay();
            morningTime = today.atTime(3, 0);
        }
        
        List<TopSellingDishResponse> dishes = orderItemRepository.findDishSoldBetweenAfternoonAndMorning(
                afternoonTime, endOfDay, startOfDay, morningTime);
                
        // Trả về tất cả các món
        return dishes.stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * Tính tổng số món đã bán từ 4h chiều đến 3h sáng hôm sau
     *
     * @return DishSummaryResponse chứa tổng số món đã bán và số loại món
     */
    public DishSummaryResponse getTotalDishSoldBetweenAfternoonAndMorning() {
        LocalDate today = LocalDate.now();
        
        // Thời điểm 4h chiều hôm nay
        LocalDateTime afternoonTime = today.atTime(16, 0);
        
        // Thời điểm cuối ngày hôm nay
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // Thời điểm đầu ngày hôm sau
        LocalDateTime startOfDay = today.plusDays(1).atStartOfDay();
        
        // Thời điểm 3h sáng hôm sau
        LocalDateTime morningTime = today.plusDays(1).atTime(3, 0);
        
        // Nếu hiện tại là buổi sáng (trước 16h), lấy khoảng thời gian từ 16h hôm trước đến 3h sáng hôm nay
        if (LocalDateTime.now().isBefore(afternoonTime)) {
            afternoonTime = today.minusDays(1).atTime(16, 0);
            endOfDay = today.minusDays(1).atTime(23, 59, 59);
            startOfDay = today.atStartOfDay();
            morningTime = today.atTime(3, 0);
        }
        
        // Lấy tổng số món đã bán
        Long totalDishSold = orderItemRepository.countTotalDishSoldBetweenAfternoonAndMorning(
                afternoonTime, endOfDay, startOfDay, morningTime);
        
        // Lấy số loại món đã bán
        Long totalDishTypes = orderItemRepository.countTotalDishTypesSoldBetweenAfternoonAndMorning(
                afternoonTime, endOfDay, startOfDay, morningTime);
        
        // Nếu không có món nào được bán
        if (totalDishSold == null) {
            totalDishSold = 0L;
        }
        
        if (totalDishTypes == null) {
            totalDishTypes = 0L;
        }
        
        return new DishSummaryResponse(totalDishSold, totalDishTypes);
    }

    public MonthlyRevenueResponse getMonthlyRevenueDetails(YearMonth yearMonth) {
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueByDateRange(startDate, endDate);

        return new MonthlyRevenueResponse(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                startDate,
                endDate
        );
    }

    public BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueByDateRange(startOfDay, endOfDay);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public List<DailyRevenueResponse> getDailyRevenueInMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Lấy tất cả các payment đã thanh toán thành công trong tháng
        List<Payment> payments = paymentRepository.findByPaymentStatusAndPaymentDateBetween(
                PaymentStatus.PAID,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        // Tạo map để tổng hợp doanh thu theo ngày
        Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>();

        // Khởi tạo tất cả các ngày trong tháng với doanh thu bằng 0
        for (int day = 1; day <= endDate.getDayOfMonth(); day++) {
            revenueByDate.put(yearMonth.atDay(day), BigDecimal.ZERO);
        }

        // Tính tổng doanh thu cho mỗi ngày
        for (Payment payment : payments) {
            LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
            BigDecimal currentRevenue = revenueByDate.getOrDefault(paymentDate, BigDecimal.ZERO);
            revenueByDate.put(paymentDate, currentRevenue.add(payment.getAmountPaid()));
        }

        // Chuyển đổi map thành danh sách DailyRevenueResponse
        return revenueByDate.entrySet().stream()
                .map(entry -> new DailyRevenueResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyRevenueResponse::getDate))
                .collect(Collectors.toList());
    }

    public List<DailyRevenueResponse> getWeeklyRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // 6 ngày trước + ngày hiện tại = 7 ngày

        // Lấy tất cả các payment đã thanh toán thành công trong khoảng thời gian
        List<Payment> payments = paymentRepository.findByPaymentStatusAndPaymentDateBetween(
                PaymentStatus.PAID,
                startDate.atStartOfDay(),
                today.atTime(23, 59, 59)
        );

        // Tạo map để tổng hợp doanh thu theo ngày
        Map<LocalDate, BigDecimal> revenueByDate = new HashMap<>();

        // Khởi tạo tất cả các ngày trong khoảng với doanh thu bằng 0
        for (int i = 0; i <= 6; i++) {
            revenueByDate.put(startDate.plusDays(i), BigDecimal.ZERO);
        }

        for (Payment payment : payments) {
            LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
            if (revenueByDate.containsKey(paymentDate)) {
                BigDecimal currentRevenue = revenueByDate.get(paymentDate);
                revenueByDate.put(paymentDate, currentRevenue.add(payment.getAmountPaid()));
            }
        }

        return revenueByDate.entrySet().stream()
                .map(entry -> new DailyRevenueResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyRevenueResponse::getDate))
                .collect(Collectors.toList());
    }

    public List<DailyRevenueResponse> getLastThreeMonthsRevenue() {
        List<DailyRevenueResponse> results = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        // Lấy doanh thu cho 3 tháng gần nhất
        for (int i = 0; i < 3; i++) {
            YearMonth month = currentMonth.minusMonths(i);

            // Lấy tổng doanh thu của tháng này
            MonthlyRevenueResponse monthlyRevenue = getMonthlyRevenueDetails(month);

            // Sử dụng ngày đầu tiên của tháng làm ngày đại diện
            LocalDate representativeDate = month.atDay(1);

            // Thêm vào kết quả với định dạng DailyRevenueResponse để đồng nhất trên chart
            results.add(new DailyRevenueResponse(
                    representativeDate,
                    monthlyRevenue.getTotalRevenue()
            ));
        }

        return results.stream()
                .sorted(Comparator.comparing(DailyRevenueResponse::getDate))
                .collect(Collectors.toList());
    }

    public List<DailyRevenueResponse> getLastThreeYearsRevenue() {
        List<DailyRevenueResponse> results = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        // Lấy doanh thu cho 3 năm gần nhất
        for (int i = 0; i < 3; i++) {
            int year = currentYear - i;

            // Lấy doanh thu của năm
            BigDecimal yearlyRevenue = getYearRevenue(year);

            // Sử dụng ngày đầu tiên của năm làm ngày đại diện
            LocalDate representativeDate = LocalDate.of(year, 1, 1);

            // Thêm vào kết quả với định dạng DailyRevenueResponse để đồng nhất trên chart
            results.add(new DailyRevenueResponse(
                    representativeDate,
                    yearlyRevenue
            ));
        }

        return results.stream()
                .sorted(Comparator.comparing(DailyRevenueResponse::getDate))
                .collect(Collectors.toList());
    }

    private BigDecimal getYearRevenue(int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        List<Payment> payments = paymentRepository.findByPaymentStatusAndPaymentDateBetween(
                PaymentStatus.PAID,
                startOfYear,
                endOfYear
        );

        return payments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Lấy món ăn bán chạy nhất theo từng khung giờ trong khoảng ngày
     * 
     * @param startDate Ngày bắt đầu khoảng thời gian, mặc định là ngày hiện tại
     * @param endDate Ngày kết thúc khoảng thời gian, mặc định bằng startDate
     * @return Danh sách món ăn bán chạy nhất theo từng khung giờ
     */
    public List<HourlyTopSellingDishResponse> getTopSellingDishesByHourOfDay(LocalDate startDate, LocalDate endDate) {
        // Nếu không cung cấp ngày bắt đầu, sử dụng ngày hiện tại
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        
        // Nếu không cung cấp ngày kết thúc, sử dụng ngày bắt đầu
        if (endDate == null) {
            endDate = startDate;
        }
        
        // Đảm bảo endDate không nhỏ hơn startDate
        if (endDate.isBefore(startDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1);
        
        // Lấy dữ liệu từ repository
        List<Object[]> results = orderItemRepository.findTopSellingDishesByHourOfDay(startDateTime, endDateTime);
        
        // Tạo Map để lưu các món ăn bán chạy nhất cho mỗi khung giờ
        Map<Integer, List<TopSellingDishResponse>> topDishesByHour = new HashMap<>();
        
        // Xử lý kết quả từ truy vấn SQL
        for (Object[] result : results) {
            Integer hour = ((Number) result[0]).intValue();
            Integer dishId = (Integer) result[1];
            String dishName = (String) result[2];
            String dishDescription = (String) result[3];
            BigDecimal dishPrice = (BigDecimal) result[4];
            Long totalQuantity = ((Number) result[5]).longValue();
            
            TopSellingDishResponse dish = new TopSellingDishResponse(dishId, dishName, dishDescription, dishPrice, totalQuantity);
            
            // Khởi tạo danh sách nếu chưa có
            if (!topDishesByHour.containsKey(hour)) {
                topDishesByHour.put(hour, new ArrayList<>());
            }
            
            // Thêm món ăn vào danh sách của khung giờ tương ứng
            List<TopSellingDishResponse> dishes = topDishesByHour.get(hour);
            dishes.add(dish);
        }
        
        // Chuyển đổi Map thành danh sách kết quả, giới hạn mỗi giờ chỉ có tối đa 3 món
        List<HourlyTopSellingDishResponse> hourlyTopDishes = new ArrayList<>();
        
        for (Map.Entry<Integer, List<TopSellingDishResponse>> entry : topDishesByHour.entrySet()) {
            List<TopSellingDishResponse> topDishes = entry.getValue().stream()
                .limit(3)
                .collect(Collectors.toList());
            
            hourlyTopDishes.add(HourlyTopSellingDishResponse.of(entry.getKey(), topDishes));
        }
        
        // Sắp xếp theo giờ
        return hourlyTopDishes.stream()
                .sorted(Comparator.comparing(HourlyTopSellingDishResponse::getHour))
                .collect(Collectors.toList());
    }
}
