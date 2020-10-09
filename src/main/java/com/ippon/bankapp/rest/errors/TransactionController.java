package com.ippon.bankapp.rest.errors;

import com.ippon.bankapp.service.AccountService;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.dto.TransactionDTO;
import com.ippon.bankapp.service.dto.TransferDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private AccountService accountService;

    public TransactionController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{lastName}/deposit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountDTO deposit(@PathVariable(name = "lastName") String lastName,
                              @Valid @RequestBody TransactionDTO transactionDTO) {
        return accountService.deposit(lastName, transactionDTO.getAmount());
    }

    @PostMapping("/{lastName}/withdraw")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountDTO withdraw(@PathVariable(name = "lastName") String lastName,
                               @Valid @RequestBody TransactionDTO transactionDTO) {
        return accountService.withdraw(lastName, transactionDTO.getAmount());
    }

    @PostMapping("/{lastName}/transfer")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountDTO transfer(@PathVariable(name = "lastName") String lastName,
                               @Valid @RequestBody TransferDTO transferDTO) {
        return accountService.transfer(lastName, transferDTO.getDestinationAccountLastName(), transferDTO.getAmount());
    }

}

