package com.laklu.pos.services;

import com.laklu.pos.entities.CashRegister;
import com.laklu.pos.entities.Payment;
import com.laklu.pos.entities.PaymentHistory;
import com.laklu.pos.enums.PaymentType;
import com.laklu.pos.enums.TransferType;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.CashRegisterRepository;
import com.laklu.pos.repositories.PaymentHistoryRepository;
import com.laklu.pos.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final CashRegisterService cashRegisterService;
    private final CashRegisterRepository cashRegisterRepository;
    private final PaymentRepository paymentRepository;

    public PaymentHistory createPaymentHistory(Payment payment, PaymentType paymentType, TransferType transferType, BigDecimal amount, Integer cashRegisterId) {
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setPayment(payment);
        paymentHistory.setPaymentType(paymentType);
        paymentHistory.setTransferType(transferType);
        paymentHistory.setAmount(amount);
        paymentHistory.setTransactionDate(LocalDateTime.now());

        PaymentHistory savedHistory = paymentHistoryRepository.save(paymentHistory);

        if (transferType == TransferType.CASH) {
            // Tìm CashRegister dựa trên cashRegisterId
            CashRegister cashRegister = cashRegisterService.findOrFail(cashRegisterId);

            // Cập nhật currentAmount trong CashRegister
            cashRegister.updateCurrentAmount(amount, paymentType);

            // Lưu lại CashRegister sau khi cập nhật
            cashRegisterRepository.save(cashRegister);
        }

        return savedHistory;
    }

    public PaymentHistory getPaymentHistoryByPaymentId(Integer paymentId) {
        return paymentHistoryRepository.findByPayment_Id(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử thanh toán cho Payment ID: " + paymentId));
    }

    public PaymentHistory updatePaymentHistory(Integer historyId, PaymentType paymentType, TransferType transferType, BigDecimal amount) {
        PaymentHistory paymentHistory = this.findOrFail(historyId);

        paymentHistory.setPaymentType(paymentType);
        paymentHistory.setTransferType(transferType);
        paymentHistory.setAmount(amount);
        paymentHistory.setTransactionDate(LocalDateTime.now());
        return paymentHistoryRepository.save(paymentHistory);
    }

    public void deletePaymentHistory(Integer historyId) {
        PaymentHistory paymentHistory = this.findOrFail(historyId);
        paymentHistoryRepository.delete(paymentHistory);
    }

    public PaymentHistory findOrFail(Integer id) {
        return this.getPaymentHistoryById(id).orElseThrow(NotFoundException::new);
    }

    public Optional<PaymentHistory> getPaymentHistoryById(int paymentId) {
        return paymentHistoryRepository.findById(paymentId);
    }

    public List<PaymentHistory> getPaymentHistoriesByDateRange(LocalDate startDate, LocalDate endDate) {
        // Chuyển đổi LocalDate thành LocalDateTime để bao gồm toàn bộ ngày
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        return paymentHistoryRepository.findAllByDateRange(startDateTime, endDateTime);
    }
    
    public Page<PaymentHistory> getPaymentHistoriesPageByDateRange(LocalDate startDate, LocalDate endDate, int pageNumber, int pageSize) {
        // Chuyển đổi LocalDate thành LocalDateTime để bao gồm toàn bộ ngày
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        // Tạo Pageable với sắp xếp theo thời gian giao dịch giảm dần (mới nhất lên đầu)
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("transactionDate").descending());
        
        return paymentHistoryRepository.findPageByDateRange(startDateTime, endDateTime, pageable);
    }
}
