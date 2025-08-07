package com.laklu.pos.controllers;

import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.UpdateAttendanceDTO;
import com.laklu.pos.dataObjects.response.AttendanceResponse;
import com.laklu.pos.entities.Attendance;
import com.laklu.pos.entities.User;
import com.laklu.pos.repositories.UserRepository;
import com.laklu.pos.services.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance Controller", description = "Quản lý điểm danh")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    @Operation(summary = "Cập nhật thông tin điểm danh", description = "API này dùng để cập nhật thông tin điểm danh của nhân viên")
    @PutMapping("/{id}")
    public ApiResponseEntity update(@PathVariable Integer id, @RequestBody @Validated UpdateAttendanceDTO updateAttendanceDTO) {
        Attendance attendance = attendanceService.findOrFail(id);
        Attendance updatedAttendance = attendanceService.updateAttendance(attendance, updateAttendanceDTO);
        return ApiResponseEntity.success(new AttendanceResponse(updatedAttendance));
    }

//    @GetMapping("/{userId}/{month}/{year}")
//    public ResponseEntity<?> getAttendanceForUserByMonth(@PathVariable int userId, @PathVariable int month, @PathVariable int year) {
//        if (month < 1 || month > 12) {
//            return ResponseEntity.badRequest().body("Invalid month. Month must be between 1 and 12.");
//        }
//        if (year < 2000 || year > LocalDate.now().getYear()) {
//            return ResponseEntity.badRequest().body("Invalid year. Year must be between 2000 and " + LocalDate.now().getYear());
//        }
//
//        Optional<User> userOpt = userRepository.findById(userId);
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//
//        long attendanceCount = attendanceService.getTotalAttendanceForMonth(userOpt.get(), month, year);
//        return ResponseEntity.ok(attendanceCount);
//    }

//    @GetMapping("/{month}/{year}")
//    public ResponseEntity<?> getAttendanceForAllUserByMonth(@PathVariable int month, @PathVariable int year) {
//        if (month < 1 || month > 12) {
//            return ResponseEntity.badRequest().body("Invalid month. Month must be between 1 and 12.");
//        }
//        if (year < 2000 || year > LocalDate.now().getYear()) {
//            return ResponseEntity.badRequest().body("Invalid year. Year must be between 2000 and " + LocalDate.now().getYear());
//        }
//        List<User> users = userRepository.findAll();
//        Map<Integer, Long> attendanceData = users.stream()
//                .collect(Collectors.toMap(
//                        User::getId,
//                        user -> attendanceService.getTotalAttendanceForMonth(user, month, year)
//                ));
//        return ResponseEntity.ok(Map.of("month", month, "year", year, "attendanceData", attendanceData));
//    }
}
