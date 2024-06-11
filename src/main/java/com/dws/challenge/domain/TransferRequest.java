package com.dws.challenge.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class TransferRequest {

  @NotNull
  private String fromAccountId;

  @NotNull
  private String toAccountId;

  @NotNull
  @Min(value = 0, message = "Transfer amount must be positive.")
  private BigDecimal amount;
}

