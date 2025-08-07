package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.response.DailyRevenueResponse;
import com.laklu.pos.dataObjects.response.DishSummaryResponse;
import com.laklu.pos.dataObjects.response.HourlyTopSellingDishResponse;
import com.laklu.pos.dataObjects.response.MonthlyRevenueResponse;
import com.laklu.pos.dataObjects.response.ScheduleDetailDTO;
import com.laklu.pos.dataObjects.response.TopSellingDishResponse;
import com.laklu.pos.entities.CalendarResponseDTO;
import com.laklu.pos.entities.Schedule;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.services.StatisticsService;
import com.laklu.pos.uiltis.Ultis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/statistics")
@Tag(name = "Statistics Controller", description = "Thống kê")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "Lấy ra 3 món bán chạy nhất")
    @GetMapping("/top-selling-dishes")
    public ApiResponseEntity topDishes() throws Exception {

        List<TopSellingDishResponse> topSelling = statisticsService.getTop3SellingDishes();

        return ApiResponseEntity.success(topSelling);
    }

    @Operation(summary = "Lấy ra các món bán chạy nhất trong vòng 1 giờ gần đây")
    @GetMapping("/top-selling-dishes/last-hour")
    public ApiResponseEntity topDishesLastHour() throws Exception {

        List<TopSellingDishResponse> topSelling = statisticsService.getTopSellingDishesLastHour();

        return ApiResponseEntity.success(topSelling, "Món bán chạy nhất trong vòng 1 giờ qua");
    }

    @Operation(summary = "Lấy danh sách các món bán được từ 4h chiều đến 3h sáng hôm sau")
    @GetMapping("/dishes/evening-to-morning/details")
    public ApiResponseEntity getDishSoldBetweenAfternoonAndMorning() throws Exception {

        List<TopSellingDishResponse> dishes = statisticsService.getDishSoldBetweenAfternoonAndMorning();

        return ApiResponseEntity.success(dishes, "Danh sách món bán được từ 4h chiều đến 3h sáng hôm sau");
    }

    @Operation(summary = "Lấy tổng số món bán được từ 4h chiều đến 3h sáng hôm sau")
    @GetMapping("/dishes/evening-to-morning")
    public ApiResponseEntity getTotalDishSoldBetweenAfternoonAndMorning() throws Exception {

        DishSummaryResponse summary = statisticsService.getTotalDishSoldBetweenAfternoonAndMorning();

        return ApiResponseEntity.success(summary, "Tổng số món bán được từ 4h chiều đến 3h sáng hôm sau");
    }

    @Operation(summary = "Lấy ra doanh thu theo tháng")
    @GetMapping("/revenue/monthly")
    public ApiResponseEntity getMonthlyRevenue(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) throws Exception {

        if (yearMonth == null) {
            yearMonth = YearMonth.now();
        }

        MonthlyRevenueResponse result = statisticsService.getMonthlyRevenueDetails(yearMonth);

        return ApiResponseEntity.success(result, "Thống kê doanh thu tháng " + yearMonth.getMonthValue() + "/" + yearMonth.getYear());
    }

    @Operation(summary = "Lấy tổng doanh thu hôm nay")
    @GetMapping("/revenue/today")
    public ApiResponseEntity getTodayRevenue() throws Exception {
        BigDecimal totalRevenue = statisticsService.getTodayRevenue();
        return ApiResponseEntity.success(totalRevenue, "Tổng doanh thu hôm nay");
    }

    @Operation(summary = "Lấy doanh thu theo ngày trong tháng")
    @GetMapping("/revenue/daily")
    public ApiResponseEntity getDailyRevenue(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) throws Exception {

        if (yearMonth == null) {
            yearMonth = YearMonth.now();
        }

        List<DailyRevenueResponse> dailyRevenues = statisticsService.getDailyRevenueInMonth(yearMonth);
        return ApiResponseEntity.success(dailyRevenues, "Doanh thu theo ngày trong tháng " + yearMonth.getMonthValue() + "/" + yearMonth.getYear());
    }

    @Operation(summary = "Lấy doanh thu trong vòng 1 tuần kể từ ngày hiện tại")
    @GetMapping("/revenue/weekly")
    public ApiResponseEntity getWeeklyRevenue() throws Exception {
        List<DailyRevenueResponse> weeklyRevenue = statisticsService.getWeeklyRevenue();
        return ApiResponseEntity.success(weeklyRevenue, "Doanh thu trong 7 ngày gần nhất");
    }

    @Operation(summary = "Lấy doanh thu trong 3 tháng gần nhất")
    @GetMapping("/revenue/last-three-months")
    public ApiResponseEntity getLastThreeMonthsRevenue() throws Exception {
        List<DailyRevenueResponse> results = statisticsService.getLastThreeMonthsRevenue();
        return ApiResponseEntity.success(results, "Doanh thu 3 tháng gần nhất");
    }

    @Operation(summary = "Lấy doanh thu của 3 năm gần nhất")
    @GetMapping("/revenue/last-three-years")
    public ApiResponseEntity getLastThreeYearsRevenue() throws Exception {
        List<DailyRevenueResponse> results = statisticsService.getLastThreeYearsRevenue();
        return ApiResponseEntity.success(results, "Doanh thu 3 năm gần nhất");
    }

    @Operation(summary = "Lấy top 3 món ăn bán chạy nhất theo từng khung giờ trong ngày", 
              description = "API này dùng để lấy top 3 món ăn bán chạy nhất cho mỗi khung giờ trong khoảng thời gian chỉ định")
    @GetMapping("/top-selling-dishes/hourly")
    public ApiResponseEntity getTopSellingDishesByHourOfDay(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) throws Exception {
        
        List<HourlyTopSellingDishResponse> hourlyTopDishes = statisticsService.getTopSellingDishesByHourOfDay(startDate, endDate);
        
        String message = "Top 3 món ăn bán chạy nhất theo từng khung giờ";
        if (startDate != null && endDate != null && !startDate.equals(endDate)) {
            message += " từ ngày " + startDate + " đến ngày " + endDate;
        } else if (startDate != null) {
            message += " ngày " + startDate;
        } else {
            message += " hôm nay";
        }
        
        return ApiResponseEntity.success(hourlyTopDishes, message);
    }

}
