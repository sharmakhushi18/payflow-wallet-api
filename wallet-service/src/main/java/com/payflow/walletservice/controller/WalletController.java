package com.payflow.walletservice.controller;

import com.payflow.walletservice.dto.*;
import com.payflow.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // Create wallet
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String userName) {

        WalletResponse response = walletService.createWallet(userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get wallet (balance)
    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(
            @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(walletService.getWallet(userId));
    }

    // Top up wallet
    @PostMapping("/top-up")
    public ResponseEntity<WalletResponse> topUp(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TopUpRequest request) {

        return ResponseEntity.ok(walletService.topUp(userId, request));
    }

    // Transfer money
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TransferRequest request) {

        String message = walletService.transfer(userId, request);

        return ResponseEntity.ok(
                new TransferResponse("SUCCESS", message)
        );
    }
}