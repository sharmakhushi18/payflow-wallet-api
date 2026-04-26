package com.payflow.walletservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopUpRequest {
    @NotNull
    @DecimalMin(value = "1.0", message = "Minimum top-up is 1 rupee")
    @DecimalMax(value = "100000.0", message = "Maximum top-up is 1 lakh")
    private BigDecimal amount;
}