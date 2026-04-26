package com.payflow.transactionservice.repository;

import com.payflow.transactionservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderUserIdOrderByCreatedAtDesc(Long userId);
    List<Transaction> findByReceiverUserIdOrderByCreatedAtDesc(Long userId);
}