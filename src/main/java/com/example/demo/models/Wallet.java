package com.example.demo.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id ; 
	
	@Column(name = "user_id", nullable = false)
	private Long userId ; 
	
	@Column(name="is_active" , nullable = false)
	private Boolean isActive ; 
	
	@Column(name = "balance", nullable = false)
	private BigDecimal balance = BigDecimal.ZERO ; 
	
	public boolean hasSufficientBalance(BigDecimal amount) {
		return balance.compareTo(amount) >= 0  ; 
	}
	
	public void debit(BigDecimal amount) {
		if(!hasSufficientBalance(amount)) {
			throw new IllegalArgumentException("Insufficient Balance") ; 
		}
		balance = balance.subtract(amount) ; 
	}
	
	public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
