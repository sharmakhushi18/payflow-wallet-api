package com.payflow.transactionservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {

    @NotNull
    private Long senderUserId;

    @NotNull
    private Long receiverUserId;

    @NotBlank
    private String senderUpiId;

    @NotBlank
    private String receiverUpiId;

    @NotNull
    @DecimalMin(value = "1.0", message = "Minimum amount is 1 rupee")
    private BigDecimal amount;
}