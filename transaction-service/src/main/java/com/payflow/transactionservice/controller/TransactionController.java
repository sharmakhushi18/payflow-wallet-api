package com.payflow.transactionservice.controller;

import com.payflow.transactionservice.dto.*;
import com.payflow.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Create transaction
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    // Get history
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getHistory(userId));
    }
}