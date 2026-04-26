package com.payflow.walletservice.service;

import com.payflow.walletservice.dto.*;
import com.payflow.walletservice.entity.Wallet;
import com.payflow.walletservice.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    // ── Create wallet ────────────────────────────────────────────────────────
    @Transactional
    public WalletResponse createWallet(Long userId, String userName) {
        if (walletRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Wallet already exists");
        }

        String upiId = generateUpiId(userName);

        try {
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .upiId(upiId)
                    .balance(BigDecimal.ZERO)
                    .build();

            Wallet saved = walletRepository.save(wallet);
            return mapToResponse(saved);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Duplicate wallet/UPI conflict");
        }
    }

    // ── Get wallet ───────────────────────────────────────────────────────────
    @Cacheable(value = "wallet-balance", key = "#userId")
    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));
        return mapToResponse(wallet);
    }

    // ── Top up ───────────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "wallet-balance", key = "#userId")
    public WalletResponse topUp(Long userId, TopUpRequest request) {

        validateAmount(request.getAmount());

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        try {
            wallet.credit(request.getAmount()); // controlled method
            Wallet updated = walletRepository.save(wallet);
            return mapToResponse(updated);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("Concurrent update detected. Retry.");
        }
    }

    // ── Transfer ─────────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "wallet-balance", key = "#senderUserId")
    public String transfer(Long senderUserId, TransferRequest request) {

        validateAmount(request.getAmount());

        Wallet sender = walletRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new IllegalStateException("Sender not found"));

        Wallet receiver = walletRepository.findByUpiId(request.getToUpiId())
                .orElseThrow(() -> new IllegalStateException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalStateException("Self transfer not allowed");
        }

        try {
            sender.debit(request.getAmount());
            receiver.credit(request.getAmount());

            walletRepository.save(sender);
            walletRepository.save(receiver);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("Concurrent transfer conflict. Retry.");
        }

        log.info("Transfer {} -> {} : {}",
                sender.getUpiId(), receiver.getUpiId(), request.getAmount());

        return "SUCCESS";
    }

    // ── Validation ───────────────────────────────────────────────────────────
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    // ── UPI generator ────────────────────────────────────────────────────────
    private String generateUpiId(String name) {
        String clean = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (clean.length() < 3) {
            clean = clean + System.currentTimeMillis();
        }
        return clean.substring(0, Math.min(clean.length(), 8)) + "@payflow";
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .upiId(wallet.getUpiId())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}