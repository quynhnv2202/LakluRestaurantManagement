package com.laklu.pos.mappers;

import com.laklu.pos.dataObjects.response.PaymentHistoryResponse;
import com.laklu.pos.entities.PaymentHistory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentHistoryMapper {
    
    public PaymentHistoryResponse toResponse(PaymentHistory entity) {
        if (entity == null) {
            return null;
        }
        
        return new PaymentHistoryResponse(
                entity.getId(),
                entity.getPayment() != null ? entity.getPayment().getId() : null,
                entity.getPaymentType(),
                entity.getTransferType(),
                entity.getTransactionDate(),
                entity.getAmount()
        );
    }
    
    public List<PaymentHistoryResponse> toResponseList(List<PaymentHistory> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
} 