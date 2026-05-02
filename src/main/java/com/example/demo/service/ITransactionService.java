package com.example.demo.service;

import com.example.demo.helpers.enums.TransactionStatus;
import com.example.demo.models.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface ITransactionService {
    Transaction createTransaction(Long fromWalletId, Long toWalletId, BigDecimal amount, String description);

    Transaction getTransactionById(Long transactionId);

    List<Transaction> getTransactionByWalletId(Long walletId) ;

    List<Transaction> getTransactionByFromWalletId(Long fromWalletId) ;

    List<Transaction> getTransactionByToWalletId(Long toWalletId) ;

    List<Transaction> getTransactionBySagaInstanceId(Long sagaInstanceId) ;

    List<Transaction> getTransactionByStatus(TransactionStatus status) ;

    void updateTransactionWithSagaInstanceId(Long transactionId, Long sagaInstanceId) ;
}
