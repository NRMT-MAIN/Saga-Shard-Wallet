package com.example.demo.service;

import com.example.demo.helpers.enums.TransactionStatus;
import com.example.demo.models.Transaction;
import com.example.demo.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements ITransactionService{
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount , String description) {
        log.info("Creating transaction from wallet {} to wallet {} with amount {}", fromWalletId, toWalletId, amount);

        Transaction transaction = Transaction.builder()
                .fromWalletId(fromWalletId)
                .toWalletId(toWalletId)
                .amount(amount)
                .description(description)
                .status(TransactionStatus.PENDING)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with id {}", savedTransaction.getId());
        return savedTransaction;
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    public List<Transaction> getTransactionByWalletId(Long walletId) {
        return transactionRepository.findByWalletId(walletId) ;
    }

    public List<Transaction> getTransactionByFromWalletId(Long fromWalletId) {
        return transactionRepository.findByFromWalletId(fromWalletId) ;
    }

    public List<Transaction> getTransactionByToWalletId(Long toWalletId) {
        return transactionRepository.findByToWalletId(toWalletId) ;
    }

    public List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId) {
        return transactionRepository.findBySagaInstanceId(sagaInstanceId) ;
    }

    public List<Transaction> getTransactionByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status) ;
    }
}
