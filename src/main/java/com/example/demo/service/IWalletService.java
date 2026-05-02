package com.example.demo.service;

import com.example.demo.models.Wallet;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface IWalletService {
    Wallet createWallet(Long userId);

    Wallet getWalletById(Long walletId) ;

    List<Wallet> getWalletsByUserId(Long userId) ;

    void debit(Long walletId, BigDecimal amount) ;

    void credit(Long walletId, BigDecimal amount) ;

    BigDecimal getWalletBalance(Long walletId) ;

}
