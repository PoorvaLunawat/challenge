package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void testPositiveTransfer() {
// Create accounts
    Account account1 = new Account("Id-1", BigDecimal.valueOf(200)); // Initialize with a positive balance
    Account account2 = new Account("Id-2", BigDecimal.valueOf(100)); // Initialize with a positive balance
    this.accountsService.createAccount(account1);
    this.accountsService.createAccount(account2);

    // Transfer money from account1 to account2
    BigDecimal amount = BigDecimal.valueOf(50);
    this.accountsService.transferMoney("Id-1", "Id-2", amount);

    // Verify balances
    assertEquals(BigDecimal.valueOf(150), account1.getBalance());
    assertEquals(BigDecimal.valueOf(150), account2.getBalance());
  }

  @Test
  public void testNegativeTransfer() {
    // Create accounts
    Account account1 = new Account("Id-3", BigDecimal.valueOf(100)); // Initialize with a positive balance
    Account account2 = new Account("Id-4", BigDecimal.valueOf(100)); // Initialize with a positive balance
    this.accountsService.createAccount(account1);
    this.accountsService.createAccount(account2);

    // Try transferring more money than available in account1
    BigDecimal amount = BigDecimal.valueOf(200);
    assertThrows(IllegalArgumentException.class, () -> this.accountsService.transferMoney("Id-3", "Id-4", amount));

    // Verify balances remain unchanged
    assertEquals(BigDecimal.valueOf(100), account1.getBalance());
    assertEquals(BigDecimal.valueOf(100), account2.getBalance());
  }

  @Test
  public void testConcurrency() throws InterruptedException {
    // Create accounts
    Account account1 = new Account("Id-5", BigDecimal.valueOf(100));
    Account account2 = new Account("Id-6");
    this.accountsService.createAccount(account1);
    this.accountsService.createAccount(account2);

    // Transfer money concurrently from multiple threads
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    for (int i = 0; i < 10; i++) {
      executorService.submit(() -> {
        try {
          this.accountsService.transferMoney("Id-5", "Id-6", BigDecimal.TEN);
        } catch (Exception e) {
          fail("Exception occurred during transfer: " + e.getMessage());
        }
      });
    }
    // Shutdown executor and wait for all tasks to complete
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);
    // Verify balances
    assertEquals(BigDecimal.ZERO, account1.getBalance());
    assertEquals(BigDecimal.valueOf(100), account2.getBalance());
  }
}
