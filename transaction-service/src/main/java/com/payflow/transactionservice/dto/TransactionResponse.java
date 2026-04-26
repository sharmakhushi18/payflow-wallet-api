package com.payflow.transactionservice.dto;

import com.payflow.transactionservice.entity.Transaction;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .senderUpiId(t.getSenderUpiId())
                .receiverUpiId(t.getReceiverUpiId())
                .amount(t.getAmount())
                .status(t.getStatus().name())
                .failureReason(t.getFailureReason())
                .createdAt(t.getCreatedAt())
                .build();
    }
}