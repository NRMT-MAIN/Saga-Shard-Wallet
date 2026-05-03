package com.example.demo.controllers;

import com.example.demo.dtos.CreateWalletRequestDTO;
import com.example.demo.dtos.CreditWalletRequestDTO;
import com.example.demo.dtos.DebitWalletRequestDTO;
import com.example.demo.models.Wallet;
import com.example.demo.service.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallets")
@Slf4j
public class WalletController {
    private final IWalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody CreateWalletRequestDTO request) {
        try {
            Wallet newWallet = walletService.createWallet(request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(newWallet);
        } catch (Exception e) {
            log.error("Error creating wallet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@PathVariable Long id) {
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/{user_id}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long user_id, @RequestBody DebitWalletRequestDTO request) {
        walletService.debit(user_id, request.getAmount());
        Wallet wallet = walletService.getWalletsByUserId(user_id).get(0);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{user_id}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long user_id, @RequestBody CreditWalletRequestDTO request) {
        walletService.credit(user_id, request.getAmount());
        Wallet wallet = walletService.getWalletsByUserId(user_id).get(0);
        return ResponseEntity.ok(wallet);
    }
}
