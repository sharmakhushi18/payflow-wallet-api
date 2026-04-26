package com.payflow.walletservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank
    private String toUpiId;

    @NotNull
    @DecimalMin(value = "1.0", message = "Minimum transfer is 1 rupee")
    private BigDecimal amount;
}