package com.example.demo.service;

import com.example.demo.models.Wallet;
import com.example.demo.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements IWalletService{
    private final WalletRepository walletRepository;

    public Wallet createWallet(Long userId) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .isActive(true)
                .balance(BigDecimal.ZERO)
                .build();

        wallet = walletRepository.save(wallet);
        log.info("Wallet created for user {} with wallet id {}", userId, wallet.getId());
        return wallet;
    }

    public Wallet getWalletById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
    }

    public List<Wallet> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    @Transactional
    public void debit(Long walletId, BigDecimal amount) {
        log.info("Debiting amount {} from wallet {}", amount, walletId);
        Wallet wallet = getWalletById(walletId);
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Debited amount {} from wallet {}", amount, walletId);
    }

    @Transactional
    public void credit(Long walletId, BigDecimal amount) {
        log.info("Crediting amount {} to wallet {}", amount, walletId);
        Wallet wallet = getWalletById(walletId);
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Credited amount {} to wallet {}", amount, walletId);
    }

    public BigDecimal getWalletBalance(Long walletId) {
        Wallet wallet = getWalletById(walletId);
        return wallet.getBalance();
    }
}
