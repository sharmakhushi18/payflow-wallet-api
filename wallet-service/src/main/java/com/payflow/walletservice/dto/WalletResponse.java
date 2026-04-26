package com.payflow.walletservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletResponse {
    private Long id;
    private String upiId;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}