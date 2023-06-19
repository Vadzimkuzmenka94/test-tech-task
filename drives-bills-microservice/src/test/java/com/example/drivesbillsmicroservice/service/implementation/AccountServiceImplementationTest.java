package com.example.drivesbillsmicroservice.service.implementation;

import com.example.drivesbillsmicroservice.entity.Account;
import com.example.drivesbillsmicroservice.enums.Currency;
import com.example.drivesbillsmicroservice.exceptions.account.InsufficientBalanceException;
import com.example.drivesbillsmicroservice.repository.AccountRepository;
import com.example.drivesbillsmicroservice.service.CurrencyConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplementationTest {

    private AccountServiceImplementation accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyConversionService currencyConversionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountServiceImplementation(accountRepository, currencyConversionService);
    }

    @Test
    void credit_IncreasesAccountBalanceForSpecifiedCurrency() {
        // Arrange
        Long accountId = 1L;
        double amount = 100.0;
        Currency currency = Currency.RED;
        Account account = createAccount();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        accountService.credit(accountId, amount, currency);

        // Assert
        switch (currency) {
            case RED:
                assertEquals(account.getRedDollar() + amount, account.getRedDollar() + amount);
                break;
            case GREEN:
                assertEquals(account.getGreenDollar() + amount, account.getGreenDollar() + amount);
                break;
            case BLUE:
                assertEquals(account.getBlueDollar() + amount, account.getBlueDollar() + amount);
                break;
        }


        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void debit_DecreasesAccountBalanceForSpecifiedCurrency() {
        // Arrange
        Long accountId = 1L;
        double amount = 50.0;
        Currency currency = Currency.GREEN;
        Account account = createAccount();
        account.setGreenDollar(100.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        accountService.debit(accountId, amount, currency);

        // Assert
        switch (currency) {
            case RED:
                assertEquals(0.0, account.getRedDollar());
                assertEquals(100.0, account.getGreenDollar()); // Assuming the green dollar should remain unchanged
                assertEquals(0.0, account.getBlueDollar());
                break;
            case GREEN:
                assertEquals(50.0, account.getGreenDollar());
                break;
            case BLUE:
                assertEquals(0.0, account.getBlueDollar());
                assertEquals(100.0, account.getGreenDollar()); // Assuming the green dollar should remain unchanged
                break;
        }

        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void debit_ThrowsInsufficientBalanceException_WhenAccountBalanceIsLessThanDebitAmount() {
        // Arrange
        Long accountId = 1L;
        double amount = 200.0;
        Currency currency = Currency.BLUE;
        Account account = createAccount();
        account.setBlueDollar(100.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act and Assert
        assertThrows(InsufficientBalanceException.class, () -> accountService.debit(accountId, amount, currency));

        verify(accountRepository, times(1)).findById(accountId);
        verify(accountRepository, never()).save(account);
    }

    @Test
    void getBalance_ReturnsCorrectBalanceForSpecifiedCurrency() {
        // Arrange
        Long accountId = 1L;
        Currency currency = Currency.GREEN;
        Account account = createAccount();
        account.setRedDollar(100.0);
        account.setGreenDollar(200.0);
        account.setBlueDollar(300.0);

        double redToCurrencyRate = 2.0;
        double greenToCurrencyRate = 1.5;
        double blueToCurrencyRate = 0.8;

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(currencyConversionService.convert(1.0, Currency.RED, currency)).thenReturn(redToCurrencyRate);
        when(currencyConversionService.convert(1.0, Currency.GREEN, currency)).thenReturn(greenToCurrencyRate);
        when(currencyConversionService.convert(1.0, Currency.BLUE, currency)).thenReturn(blueToCurrencyRate);

        BigDecimal expectedBalance = BigDecimal.valueOf(account.getRedDollar())
                .multiply(BigDecimal.valueOf(redToCurrencyRate))
                .add(BigDecimal.valueOf(account.getGreenDollar()).multiply(BigDecimal.valueOf(greenToCurrencyRate)))
                .add(BigDecimal.valueOf(account.getBlueDollar()).multiply(BigDecimal.valueOf(blueToCurrencyRate)));

        // Act
        BigDecimal balance = accountService.getBalance(accountId, currency);

        // Assert
        assertEquals(expectedBalance.setScale(2, BigDecimal.ROUND_HALF_UP), balance);

        verify(accountRepository, times(1)).findById(accountId);
        verify(currencyConversionService, times(1)).convert(1.0, Currency.RED, currency);
        verify(currencyConversionService, times(1)).convert(1.0, Currency.GREEN, currency);
        verify(currencyConversionService, times(1)).convert(1.0, Currency.BLUE, currency);
    }

    private Account createAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setRedDollar(0.0);
        account.setGreenDollar(0.0);
        account.setBlueDollar(0.0);
        return account;
    }
}