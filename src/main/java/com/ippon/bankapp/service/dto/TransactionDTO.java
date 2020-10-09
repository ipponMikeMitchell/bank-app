package com.ippon.bankapp.service.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransactionDTO {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    public TransactionDTO() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
