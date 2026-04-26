package com.payflow.transactionservice.service;

import com.payflow.transactionservice.dto.*;
import com.payflow.transactionservice.entity.Transaction;
import com.payflow.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // ── Create transaction ────────────────────────────────────────────────────
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {

        // Save transaction as INITIATED
        Transaction transaction = Transaction.builder()
                .senderUserId(request.getSenderUserId())
                .receiverUserId(request.getReceiverUserId())
                .senderUpiId(request.getSenderUpiId())
                .receiverUpiId(request.getReceiverUpiId())
                .amount(request.getAmount())
                .status(Transaction.TransactionStatus.INITIATED)
                .build();

        transaction = transactionRepository.save(transaction);

        // Mark as PROCESSING
        transaction.setStatus(Transaction.TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        // Mark as SUCCESS
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);

        log.info("Transaction SUCCESS: from={} to={} amount={}",
                request.getSenderUpiId(),
                request.getReceiverUpiId(),
                request.getAmount());

        return TransactionResponse.from(saved);
    }

    // ── Get transaction history ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(Long userId) {
        List<Transaction> sent = transactionRepository
                .findBySenderUserIdOrderByCreatedAtDesc(userId);
        List<Transaction> received = transactionRepository
                .findByReceiverUserIdOrderByCreatedAtDesc(userId);

        sent.addAll(received);
        return sent.stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }
}