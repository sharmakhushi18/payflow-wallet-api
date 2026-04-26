package com.payflow.walletservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class TransferResponse {
    private String status;
    private String message;
}