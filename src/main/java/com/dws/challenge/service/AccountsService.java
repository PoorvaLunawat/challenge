package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  @Getter
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transferMoney(String fromAccountId, String toAccountId, BigDecimal amount) {
    // Check if the transfer amount is positive
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive.");
    }
    
    // Retrieve accounts from the repository
    Account fromAccount = accountsRepository.getAccount(fromAccountId);
    Account toAccount = accountsRepository.getAccount(toAccountId);
    
    // Check if both accounts exist
    if (fromAccount == null || toAccount == null) {
      throw new IllegalArgumentException("One or both accounts do not exist.");
    }
    Account firstLock = fromAccountId.compareTo(toAccountId) < 0 ? fromAccount : toAccount;
    Account secondLock = fromAccountId.compareTo(toAccountId) < 0 ? toAccount : fromAccount;
    
    // Synchronize on the accounts in the determined order, by object level locking
    synchronized (firstLock) {
      synchronized (secondLock) {
        // Check if the source account has sufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
          throw new IllegalArgumentException("Insufficient funds in the source account.");
        }
        // Debit the amount from the source account
        fromAccount.debit(amount);
        // Credit the amount to the destination account
        toAccount.credit(amount);
      }
    }
    // Send notifications to both account holders about the transfer
    notificationService.notifyAboutTransfer(fromAccount, "Transferred " + amount + " to account " + toAccountId);
    notificationService.notifyAboutTransfer(toAccount, "Received " + amount + " from account " + fromAccountId);
  }
}
