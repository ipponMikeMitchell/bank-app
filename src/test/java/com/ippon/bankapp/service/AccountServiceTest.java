package com.ippon.bankapp.service;

import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.repository.AccountRepository;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.exception.AccountLastNameExistsException;
import com.ippon.bankapp.service.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock(lenient = true)
    private NotificationFactory notificationFactory;

    @Mock(lenient = true)
    private EmailService emailService;

    @InjectMocks
    public AccountService subject;

    @BeforeEach
    public void before() {
        configureEmailNotificationService();
    }

    @Test
    public void itCreatesAnAccount() {
        //Given
        AccountDTO accountDto = new AccountDTO()
                .firstName("Ben")
                .lastName("Scott");

        Account account = new Account(accountDto.getFirstName(), accountDto.getLastName());
        account.setNotificationPreference("email");

        given(accountRepository.save(account)).willReturn(account);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        //act
        AccountDTO accountResult = subject.createAccount(accountDto);

        //assert
        assertThat(accountResult.getBalance(), is(BigDecimal.ZERO));
        assertThat(accountResult.getNotificationPreference(), is("email"));
        assertThat(accountResult.getFirstName(), is("Ben"));
        assertThat(accountResult.getLastName(), is("Scott"));

        verify(emailService, times(1))
                .sendMessage(message.capture(), message.capture(), message.capture(), message.capture());
        assertThat(message.getAllValues().get(0), is("bank"));
        assertThat(message.getAllValues().get(1), is(accountDto.getLastName()));
        assertThat(message.getAllValues().get(2), is("Account Created"));
        assertThat(message.getAllValues().get(3), is("Welcome aboard!"));
    }

    @Test
    public void itThrowsAccountLastNameExistsExceptionWhenCreatingAnAccountWithSameLastName() {

        Account account = new Account();
        account.setFirstName("Ben");
        account.setLastName("Scott");
        account.setBalance(BigDecimal.ZERO);
        account.setNotificationPreference("email");
        given(accountRepository.findByLastName("Scott")).willReturn(Optional.of(account));

        AccountDTO accountDto = new AccountDTO()
                .firstName("Ben")
                .lastName("Scott");

        assertThrows(AccountLastNameExistsException.class, () -> subject.createAccount(accountDto));

        verify(accountRepository, times(0)).save(account);
        verifyNoInteractions(emailService);
    }

    @Test
    public void itDepositsIntoAccount() {
        Account account = new Account();
        account.setFirstName("Ben");
        account.setLastName("Scott");
        account.setBalance(BigDecimal.ZERO);
        account.setNotificationPreference("email");
        given(accountRepository.findByLastName("Scott")).willReturn(Optional.of(account));
        given(accountRepository.save(account)).willReturn(account);

        AccountDTO result = subject.deposit("Scott", BigDecimal.valueOf(14.53));

        assertThat(result.getBalance(), is(BigDecimal.valueOf(14.53)));
        assertThat(result.getLastName(), is("Scott"));
        assertThat(result.getFirstName(), is("Ben"));
    }

    @Test
    public void itWithdrawsFromAccount() {
        Account account = new Account();
        account.setFirstName("Ben");
        account.setLastName("Scott");
        account.setBalance(BigDecimal.TEN);
        account.setNotificationPreference("email");
        given(accountRepository.findByLastName("Scott")).willReturn(Optional.of(account));
        given(accountRepository.save(account)).willReturn(account);

        AccountDTO result = subject.withdraw("Scott", BigDecimal.valueOf(1.53));

        assertThat(result.getBalance(), is(BigDecimal.valueOf(8.47)));
        assertThat(result.getLastName(), is("Scott"));
        assertThat(result.getFirstName(), is("Ben"));
    }

    @Test
    public void itThrowsInsufficientFundsWhenWithdrawingTooMuch() {

        Account account = new Account();
        account.setFirstName("Ben");
        account.setLastName("Scott");
        account.setBalance(BigDecimal.TEN);
        account.setNotificationPreference("email");
        given(accountRepository.findByLastName("Scott")).willReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () -> subject.withdraw("Scott", BigDecimal.valueOf(10.01)));

        verify(emailService, times(1)).sendMessage("Bank",
                "Scott",
                "Insufficient funds",
                "Unable to withdraw 10.01. Your current balance is 10");
        verify(accountRepository, times(0)).save(account);
    }

    private void configureEmailNotificationService() {
        given(notificationFactory.getDefaultNotification())
                .willReturn(emailService);
        given(emailService.getName()).willReturn("email");
        given(notificationFactory.getPreferredService("email"))
                .willReturn(Optional.of(emailService));
    }

}
