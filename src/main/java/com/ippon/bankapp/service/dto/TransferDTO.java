package com.ippon.bankapp.service.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


public class TransferDTO {

    @NotEmpty
    private String destinationAccountLastName;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    public String getDestinationAccountLastName() {
        return destinationAccountLastName;
    }

    public void setDestinationAccountLastName(String destinationAccountLastName) {
        this.destinationAccountLastName = destinationAccountLastName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
