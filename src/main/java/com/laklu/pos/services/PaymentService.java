package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.PaymentRequest;
import com.laklu.pos.dataObjects.response.CashResponse;
import com.laklu.pos.enums.*;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.*;
import com.laklu.pos.entities.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final VoucherRepository voucherRepository;
    private static final String SEPAY_QR_URL = "https://qr.sepay.vn/img";
    private static final String PREFIX = "LL";
    private static final BigDecimal DEFAULT_VAT_RATE = BigDecimal.ZERO;
    private static final long PAYMENT_TIMEOUT_MINUTES = 10;

    public static String generatePaymentCode(int orderId) {
        return PREFIX + String.format("%07d", orderId);
    }

    public Page<Payment> getAllPaginated(
            Pageable pageable,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (startDate != null && endDate != null) {
            return paymentRepository.findPaymentByDateBetween(startDate, endDate, pageable);
        }
        return paymentRepository.findAll(pageable);
    }

    public Payment findOrFail(Integer id) {
        return this.getPaymentById(id).orElseThrow(NotFoundException::new);
    }

    public Optional<Payment> getPaymentById(int paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    public BigDecimal getVoucherValue(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException());

        BigDecimal subTotal = calculateSubTotal(order);
        BigDecimal originalTotal = subTotal;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Optional<Voucher> voucherOpt = voucherRepository.findByCode(request.getVoucherCode());
            if (voucherOpt.isEmpty()) {
                throw new IllegalArgumentException("Voucher không tồn tại");
            }
            Voucher voucher = voucherOpt.get();
            if (voucher.getValidUntil().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Voucher đã hết hạn");
            }
            if (voucher.getStatus() == VoucherStatus.INACTIVE) {
                throw new IllegalArgumentException("Voucher không còn hiệu lực");
            }
            subTotal = applyVoucherDiscount(subTotal, voucher);
        }

        return originalTotal.subtract(subTotal);
    }

    public Payment createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException());

        List<Payment> existingPayment = paymentRepository.getPaymentByOrder(order);
        if(!existingPayment.isEmpty()) {
            if(existingPayment.get(0).getPaymentStatus() == PaymentStatus.PAID) {
                throw new IllegalArgumentException("Đơn hàng đã được thanh toán");
            }else{
                paymentRepository.deleteAll(existingPayment);
            }
        }

        BigDecimal subTotal = calculateSubTotal(order);
        BigDecimal originalTotal = subTotal;
        BigDecimal voucherValue = BigDecimal.ZERO;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Optional<Voucher> voucherOpt = voucherRepository.findByCode(request.getVoucherCode());
            if (voucherOpt.isEmpty()) {
                throw new IllegalArgumentException("Voucher không tồn tại");
            }
            Voucher voucher = voucherOpt.get();
            if (voucher.getValidUntil().isBefore(LocalDateTime.now())) {
                voucher.setStatus(VoucherStatus.INACTIVE);
                voucherRepository.save(voucher);
                throw new IllegalArgumentException("Voucher đã hết hạn");
            }
            if (voucher.getStatus() == VoucherStatus.INACTIVE) {
                throw new IllegalArgumentException("Voucher không còn hiệu lực");
            }
            subTotal = applyVoucherDiscount(subTotal, voucher);
            voucherValue = originalTotal.subtract(subTotal);
            voucherRepository.save(voucher);
        }

        BigDecimal vatRate = request.getVat() == null ? DEFAULT_VAT_RATE : request.getVat();
        BigDecimal vatAmount = subTotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal totalAmount = subTotal.add(vatAmount);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmountPaid(totalAmount);
        payment.setVat(vatRate);
        payment.setReceivedAmount(BigDecimal.ZERO);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setCode(generatePaymentCode(request.getOrderId()));
        payment.setVoucherValue(voucherValue);
        return paymentRepository.save(payment);
    }

    @Transactional
    public CashResponse processCashPayment(int paymentId, BigDecimal receivedAmount) {
        Payment payment = findOrFail(paymentId);

        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new IllegalArgumentException("Chỉ áp dụng cho thanh toán tiền mặt");
        }

        BigDecimal orderAmount = payment.getAmountPaid();
        if (receivedAmount.compareTo(orderAmount) < 0) {
            throw new IllegalArgumentException("Số tiền nhận được không đủ để thanh toán");
        }

        BigDecimal change = receivedAmount.subtract(orderAmount);

        payment.setReceivedAmount(receivedAmount);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        this.updateOrderStatus(payment.getOrder());
        this.updateReservationStatus(payment.getOrder().getReservation());
        return new CashResponse(
                payment.getOrder().getId(),
                payment.getAmountPaid(),
                payment.getReceivedAmount(),
                payment.getVat(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getPaymentDate(),
                change
        );
    }

    public String generateQrCode(int paymentId) {
        Payment payment = findOrFail(paymentId);
        String bank = "MBBank";
        String account = "0587775888";
        String description = payment.getCode();
        return SEPAY_QR_URL + "?bank=" + bank
                + "&acc=" + account
                + "&amount=" + payment.getAmountPaid()
                + "&des=" + description
                + "&paymentId=" + payment.getId();
    }

    @Transactional
    public void processPaymentWebhook(String paymentStatus, String paymentCode, BigDecimal amount) {
        Payment payment = paymentRepository.findByCode(paymentCode)
                .orElseThrow(() -> new NotFoundException());

        if (amount.compareTo(payment.getAmountPaid()) != 0) {
            log.error("Số tiền nhận được ({}) không khớp với yêu cầu ({})!", amount, payment.getAmountPaid());
            throw new IllegalArgumentException("Số tiền thanh toán không khớp");
        }

        if ("SUCCESS".equals(paymentStatus)) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setReceivedAmount(amount);
            this.updateOrderStatus(payment.getOrder());
        } else if ("FAILED".equals(paymentStatus)) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        } else {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        this.updateOrderStatus(payment.getOrder());
        this.updateReservationStatus(payment.getOrder().getReservation());
        log.info("PaymentId {} đã cập nhật thành {}", paymentCode, payment.getPaymentStatus());
    }

    private void updateOrderStatus(Order order) {
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    private void updateReservationStatus(Reservation rsv) {
        if (rsv == null) {
            return;
        }

        List<Order> orders = orderRepository.findByReservation(rsv);
        if (orders.isEmpty()) {
            return;
        }

        boolean allOrdersCompleted = orders.stream()
                .allMatch(order -> order.getStatus() == OrderStatus.COMPLETED
                        || order.getStatus() == OrderStatus.CANCELLED);

        if (allOrdersCompleted) {
            rsv.setCheckOut(LocalDateTime.now());
            rsv.setStatus(Reservation.Status.COMPLETED);
            reservationRepository.save(rsv);
        } else {
            rsv.setStatus(Reservation.Status.CONFIRMED);
            reservationRepository.save(rsv);
        }
    }

    private BigDecimal applyVoucherDiscount(BigDecimal totalAmount, Voucher voucher) {
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal percentage = voucher.getDiscountValue().divide(BigDecimal.valueOf(100));
            return totalAmount.multiply(BigDecimal.ONE.subtract(percentage));
        } else if (voucher.getDiscountType() == DiscountType.FIXEDAMOUNT) {
            return totalAmount.subtract(voucher.getDiscountValue().max(BigDecimal.ZERO));
        } else {
            return totalAmount;
        }
    }

    private BigDecimal calculateSubTotal(Order order) {
        return order.getOrderItems().stream()
                .map(orderItem -> orderItem.getMenuItem().getPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancelPayment(int paymentId) {
        Payment payment = findOrFail(paymentId);
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.getOrder().setStatus(OrderStatus.CONFIRMED);
        payment.getOrder().getReservation().setStatus(Reservation.Status.CONFIRMED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Transactional
    public void completePayment(int paymentId) {
        Payment payment = findOrFail(paymentId);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        this.updateOrderStatus(payment.getOrder());
        this.updateReservationStatus(payment.getOrder().getReservation());
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelExpiredPayment() {
        LocalDateTime timeoutRate = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<Payment> expiredPayment = paymentRepository.findByPaymentStatusAndCreatedAtBefore(PaymentStatus.PENDING, timeoutRate);
        for (Payment payment : expiredPayment) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            this.updateOrderStatus(payment.getOrder());
        }
    }
}