package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class Account {

  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }
  
  // Synchronized to ensure thread-safe access to the balance
  public synchronized BigDecimal getBalance() {
    return balance;
  }

  // Synchronized to ensure thread-safe modification of the balance
  public synchronized void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  // Synchronized to ensure atomic debit operation
  public synchronized void debit(BigDecimal amount) {
    this.balance = this.balance.subtract(amount);
  }

  // Synchronized to ensure atomic credit operation
  public synchronized void credit(BigDecimal amount) {
    this.balance = this.balance.add(amount);
  }
}
