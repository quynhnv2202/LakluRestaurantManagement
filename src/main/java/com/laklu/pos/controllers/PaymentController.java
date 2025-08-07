package com.laklu.pos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laklu.pos.auth.JwtGuard;
import com.laklu.pos.auth.policies.PaymentPolicy;
import com.laklu.pos.dataObjects.ApiResponseEntity;
import com.laklu.pos.dataObjects.request.PaymentRequest;
import com.laklu.pos.dataObjects.request.SepayWebhookRequest;
import com.laklu.pos.dataObjects.response.BillResponse;
import com.laklu.pos.dataObjects.response.CashResponse;
import com.laklu.pos.dataObjects.response.OrderItemsResponse;
import com.laklu.pos.dataObjects.response.PaymentResponse;
import com.laklu.pos.dataObjects.response.PaymentResponseV2;
import com.laklu.pos.entities.Order;
import com.laklu.pos.entities.Payment;
import com.laklu.pos.entities.User;
import com.laklu.pos.enums.PaymentType;
import com.laklu.pos.enums.TransferType;
import com.laklu.pos.exceptions.httpExceptions.ForbiddenException;
import com.laklu.pos.repositories.OrderRepository;
import com.laklu.pos.repositories.PaymentRepository;
import com.laklu.pos.repositories.ScheduleRepository;
import com.laklu.pos.services.*;
import com.laklu.pos.uiltis.Ultis;
import com.laklu.pos.validator.PaymentMustExist;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.UserMustHaveScheduleAndAttendance;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentPolicy paymentPolicy;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentHistoryService paymentHistoryService;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;
    private final CashRegisterService cashRegisterService;

    private List<OrderItemsResponse> orderItemResponses(Payment payment) {
        return payment.getOrder().getOrderItems().stream()
                .map(orderItem -> new OrderItemsResponse(
                        orderItem.getId(),
                        orderItem.getMenuItem().getDish().getName(),
                        orderItem.getQuantity(),
                        orderItem.getMenuItem().getPrice()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/order-items/{orderId}")
    public ApiResponseEntity getOrderItemsInOrder(@PathVariable Integer orderId) throws Exception {
        Order order = orderService.findOrFail(orderId);
        List<OrderItemsResponse> responses = order.getOrderItems().stream()
                .map(orderItem -> new OrderItemsResponse(
                        orderItem.getId(),
                        orderItem.getMenuItem().getDish().getName(),
                        orderItem.getQuantity(),
                        orderItem.getMenuItem().getPrice()
                )).collect(Collectors.toList());
        return ApiResponseEntity.success(responses, "Lấy danh sách món ăn trong hóa đơn");
    }

    @GetMapping("/bill/{paymentId}")
    public ApiResponseEntity getBill(@PathVariable int paymentId) throws Exception {
        Payment payment = paymentService.findOrFail(paymentId);
        Ultis.throwUnless(paymentPolicy.canView(JwtGuard.userPrincipal(), payment), new ForbiddenException());
        String tableNumber = payment.getOrder()
                .getReservation()
                .getReservationTables()
                .stream()
                .map(rt -> rt.getTable().getTableNumber())
                .collect(Collectors.joining(", "));
        BigDecimal change = BigDecimal.ZERO;
        if (payment.getReceivedAmount().compareTo(BigDecimal.ZERO) > 0) {
            change = payment.getReceivedAmount().subtract(payment.getAmountPaid());
        }

        BillResponse response = new BillResponse(
                payment.getOrder().getId(),
                tableNumber,
                payment.getPaymentDate(),
                payment.getOrder().getReservation().getCheckIn(),
                payment.getOrder().getReservation().getCheckOut(),
                orderItemResponses(payment),
                payment.getAmountPaid(),
                payment.getReceivedAmount(),
                payment.getVoucherValue(),
                change
        );
        return ApiResponseEntity.success(response, "Lấy hóa đơn");
    }

    @GetMapping("/{id}")
    public ApiResponseEntity getPaymentById(@PathVariable int id) throws Exception {
        Payment payment = paymentService.findOrFail(id);
        Ultis.throwUnless(paymentPolicy.canView(JwtGuard.userPrincipal(), payment), new ForbiddenException());
        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmountPaid(),
                payment.getReceivedAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaymentDate(),
                payment.getVoucherValue(),
                payment.getVat(),
                orderItemResponses(payment)
        );
        return ApiResponseEntity.success(response, "Lấy hóa đơn thành công");
    }

    @GetMapping("/getAll")
    public ApiResponseEntity getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) throws Exception {
        Ultis.throwUnless(paymentPolicy.canList(JwtGuard.userPrincipal()), new ForbiddenException());
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime start = (startDate != null) ? LocalDateTime.parse(startDate, formatter) : null;
        LocalDateTime end = (endDate != null) ? LocalDateTime.parse(endDate, formatter) : null;

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Payment> paymentPage = paymentService.getAllPaginated(pageable, start, end);

        List<PaymentResponse> responses = paymentPage.getContent().stream()
                .map(payment -> new PaymentResponse(
                        payment.getId(),
                        payment.getOrder().getId(),
                        payment.getAmountPaid(),
                        payment.getReceivedAmount(),
                        payment.getPaymentMethod(),
                        payment.getPaymentStatus(),
                        payment.getPaymentDate(),
                        payment.getVoucherValue(),
                        payment.getVat(),
                        orderItemResponses(payment)
                )).collect(Collectors.toList());
        Map<String, Object> responseData = Map.of(
                "payments", responses,
                "currentPage", paymentPage.getNumber() + 1,
                "totalItems", paymentPage.getTotalElements(),
                "totalPages", paymentPage.getTotalPages(),
                "sortBy", sortBy,
                "sortDirection", sortDirection
        );
        return ApiResponseEntity.success(responseData, "Lấy danh sách hóa đơn thanh toán");
    }

    @PostMapping("/create")
    public ApiResponseEntity createPayment(@Valid @RequestBody PaymentRequest request) throws Exception {
        Ultis.throwUnless(paymentPolicy.canCreate(JwtGuard.userPrincipal()), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        Payment payment = paymentService.createPayment(request);
        PaymentResponseV2 response = new PaymentResponseV2(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmountPaid(),
                payment.getReceivedAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaymentDate(),
                payment.getVat(),
                paymentService.getVoucherValue(request),
                orderItemResponses(payment)
        );
        return ApiResponseEntity.success(response, "Tạo hóa đơn thanh toán thành công");
    }

    @PostMapping("/{id}/checkout/cash")
    public ApiResponseEntity processCashPayment(@PathVariable int id, @RequestParam BigDecimal receivedAmount) throws Exception {
        RuleValidator.validate(new PaymentMustExist(id, paymentRepository));
        Payment payment = paymentService.findOrFail(id);
        Ultis.throwUnless(paymentPolicy.canEdit(JwtGuard.userPrincipal(), payment), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        
        // Kiểm tra nếu đã đăng ký két tiền chưa
        Integer cashRegisterId;
        try {
            cashRegisterId = cashRegisterService.getIdcashRegisterBySchedule(JwtGuard.userPrincipal().getPersitentUser().getId());
        } catch (IllegalArgumentException e) {
            // Trả về lỗi nếu chưa đăng ký két tiền
            return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, e.getMessage(), "Lỗi thanh toán");
        }
        
        // Xử lý thanh toán tiền mặt
        CashResponse response = paymentService.processCashPayment(id, receivedAmount);

        // Ghi lại lịch sử thanh toán
        paymentHistoryService.createPaymentHistory(payment, PaymentType.IN, TransferType.CASH, receivedAmount, cashRegisterId);

        if (response.getChange().compareTo(BigDecimal.ZERO) > 0) {
            paymentHistoryService.createPaymentHistory(payment, PaymentType.OUT, TransferType.CASH, response.getChange(), cashRegisterId);
        }

        return ApiResponseEntity.success(response, "Thanh toán tiền mặt thành công");
    }

    @GetMapping("/{id}/qr")
    public ApiResponseEntity generateQrCode(@PathVariable int id) throws Exception {
        Payment payment = paymentService.findOrFail(id);
        Ultis.throwUnless(paymentPolicy.canView(JwtGuard.userPrincipal(), payment), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);

        String qrCodeUrl = paymentService.generateQrCode(id);
        return ApiResponseEntity.success(Map.of("qrCodeUrl", qrCodeUrl), "Tạo mã QR thành công");
    }

    @PostMapping("/cancel/{id}")
    public ApiResponseEntity cancelPayment(@PathVariable int id) throws Exception {
        Payment payment = paymentService.findOrFail(id);
        Ultis.throwUnless(paymentPolicy.canEdit(JwtGuard.userPrincipal(), payment), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        paymentService.cancelPayment(id);
        return ApiResponseEntity.success(null, "Hủy thanh toán thành công");
    }

    @PostMapping("/complete/{id}")
    public ApiResponseEntity completePayment(@PathVariable int id) throws Exception {
        Payment payment = paymentService.findOrFail(id);
        Ultis.throwUnless(paymentPolicy.canEdit(JwtGuard.userPrincipal(), payment), new ForbiddenException());
        User staff = JwtGuard.userPrincipal().getPersitentUser();
        UserMustHaveScheduleAndAttendance rule = new UserMustHaveScheduleAndAttendance(staff, scheduleRepository, scheduleService, true);
        RuleValidator.validate(rule);
        paymentService.completePayment(id);
        return ApiResponseEntity.success(null, "Thanh toán hóa đơn thành công");
    }

    @PostMapping("/webhook/sepay")
    public ApiResponseEntity processSepayWebhook(HttpServletRequest request) {
        try {
            // Đọc JSON gốc từ request
            String json = request.getReader().lines().collect(Collectors.joining());
            log.info("Raw JSON từ SePay: {}", json);

            // Parse JSON thành SepayWebhookRequest
            ObjectMapper objectMapper = new ObjectMapper();
            SepayWebhookRequest payload = objectMapper.readValue(json, SepayWebhookRequest.class);
            log.info("parse payload: {}", payload);

            // Lấy paymentCode từ webhook
            String paymentCode = payload.getCode();
            if (paymentCode == null || paymentCode.trim().isEmpty()) {
                log.error("Payment code bị thiếu!");
                return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, "Mã thanh toán bị thiếu");
            }

            BigDecimal amount = payload.getTransferAmount();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Số tiền giao dịch không hợp lệ: {}", amount);
                return ApiResponseEntity.exception(HttpStatus.BAD_REQUEST, "Số tiền không hợp lệ");
            }

            log.info("Xử lý thanh toán cho paymentCode: {}, amount: {}", paymentCode, amount);
            paymentService.processPaymentWebhook("SUCCESS", paymentCode, amount);

            return ApiResponseEntity.success(Map.of(
                    "paymentCode", paymentCode,
                    "status", "PAID",
                    "amount", amount
            ), "Cập nhật trạng thái thanh toán thành công");
        } catch (Exception e) {
            log.error("Lỗi xử lý JSON: ", e);
            return ApiResponseEntity.exception(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "Lỗi xử lý webhook");
        }
    }
}
