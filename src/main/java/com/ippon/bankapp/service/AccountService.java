package com.ippon.bankapp.service;

import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.repository.AccountRepository;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.exception.AccountLastNameExistsException;
import com.ippon.bankapp.service.exception.AccountNotFoundException;
import com.ippon.bankapp.service.exception.InsufficientFundsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private AccountRepository accountRepository;
    private NotificationFactory notificationFactory;

    public AccountService(AccountRepository accountRepository, NotificationFactory notificationFactory) {
        this.accountRepository = accountRepository;
        this.notificationFactory = notificationFactory;
    }

    public AccountDTO createAccount(AccountDTO newAccount) {
        validateLastNameUnique(newAccount.getLastName());
        Account account = new Account(newAccount.getFirstName(), newAccount.getLastName());
        account.setNotificationPreference(notificationFactory
                .getDefaultNotification()
                .getName());

        Account save = accountRepository.save(account);

        notificationFactory
                .getPreferredService(save.getNotificationPreference())
                .orElseGet(() -> notificationFactory.getDefaultNotification())
                .sendMessage("bank",
                        account.getLastName(),
                        "Account Created",
                        "Welcome aboard!");

        return mapAccountToDTO(save);
    }

    public AccountDTO getAccount(String lastName) {
        Account account = getAccountEntity(lastName);
        return mapAccountToDTO(account);
    }

    private Account getAccountEntity(String lastName) {
        return accountRepository
                .findByLastName(lastName)
                .orElseThrow(AccountNotFoundException::new);
    }

    private void validateLastNameUnique(String lastName) {
        accountRepository
                .findByLastName(lastName)
                .ifPresent(t -> {throw new AccountLastNameExistsException();});
    }

    private AccountDTO mapAccountToDTO(Account account) {
        return new AccountDTO()
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .balance(account.getBalance())
                .notificationPreference(account.getNotificationPreference());
    }

    public AccountDTO deposit(String lastName, BigDecimal amount) {
        Account account = getAccountEntity(lastName);
        account.setBalance(account.getBalance().add(amount));
        return mapAccountToDTO(saveAccount(account));
    }

    public AccountDTO withdraw(String lastName, BigDecimal amount) {
        Account account = getAccountEntity(lastName);
        if (account.getBalance().compareTo(amount) < 0) {
            notificationFactory
                    .getPreferredService(account.getNotificationPreference())
                    .orElseGet(() -> notificationFactory.getDefaultNotification())
                    .sendMessage("Bank",
                            account.getLastName(),
                            "Insufficient funds",
                            String.format("Unable to withdraw %s. Your current balance is %s", amount, account.getBalance()));
            throw new InsufficientFundsException();
        }

        account.setBalance(account.getBalance().subtract(amount));
        return mapAccountToDTO(saveAccount(account));    }

    private Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public AccountDTO transfer(String lastName, String destinationAccount, BigDecimal amount) {
        Account source = getAccountEntity(lastName);
        Account destination = getAccountEntity(destinationAccount);

        deposit(destination.getLastName(), amount);
        return withdraw(source.getLastName(), amount);
    }
}
