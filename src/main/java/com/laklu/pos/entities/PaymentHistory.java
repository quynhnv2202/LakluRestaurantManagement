package com.laklu.pos.entities;

import com.laklu.pos.enums.PaymentType;
import com.laklu.pos.enums.TransferType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType; // Enum PaymentType: "IN" hoặc "OUT"

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type")
    private TransferType transferType; // Enum TransferType: "CASH" hoặc "BANKING"

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "amount")
    private BigDecimal amount; // Số tiền giao dịch

    @PrePersist
    public void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }

}
