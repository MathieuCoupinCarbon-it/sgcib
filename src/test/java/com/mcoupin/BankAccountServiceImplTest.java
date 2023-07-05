package com.mcoupin;

import com.mcoupin.exceptions.AccountNotFoundException;
import com.mcoupin.exceptions.InsufficientBalanceException;
import com.mcoupin.models.Operation;
import com.mcoupin.models.OperationType;
import com.mcoupin.repositories.OperationRepository;
import com.mcoupin.services.BankAccountServiceImpl;
import com.mcoupin.services.UuidProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceImplTest {
    private final Clock clock = Clock.fixed(Instant.parse("2018-04-29T10:15:30.00Z"), ZoneId.systemDefault());

    @Mock
    private OperationRepository operationRepository;
    @Mock
    private UuidProvider uuidProvider;
    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @BeforeEach
    void setUp() {
        bankAccountService = new BankAccountServiceImpl(operationRepository, clock, uuidProvider);
    }

    @Test
    @DisplayName("Performing deposit with an existing and provisioned account should add the deposit ")
    public void performDeposit_withAnExistingAndProvisionedAccount() throws AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        Operation initialOperation = new Operation(UUID.randomUUID(), accountId, initialBalance, initialBalance, LocalDateTime.now(this.clock), OperationType.DEPOSIT);

        BigDecimal depositAmount = BigDecimal.valueOf(500);

        BigDecimal expectedBalance = BigDecimal.valueOf(1500);
        UUID expectedOperationId = UUID.fromString("aa7ad925-59f1-43db-bae7-a0c2221b8a6a");
        Operation expected = new Operation(expectedOperationId, accountId, depositAmount, expectedBalance, LocalDateTime.now(this.clock), OperationType.DEPOSIT);

        when(uuidProvider.generate()).thenReturn(expectedOperationId);
        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(initialOperation));

        // Act
        bankAccountService.performDeposit(accountId, depositAmount);

        // Assert
        verify(operationRepository).getLastOperation(accountId);
        verify(operationRepository).addOperation(expected);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);

    }

    @Test
    @DisplayName("Performing deposit with non existing account should throw AccountNotFoundException")
    public void performDeposit_withNonExistingAccount() throws AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        BigDecimal depositAmount = BigDecimal.valueOf(500);

        when(operationRepository.getLastOperation(accountId)).thenThrow(new AccountNotFoundException());

        // Act
        Assertions.assertThrows(AccountNotFoundException.class, () -> bankAccountService.performDeposit(accountId, depositAmount));

        // Assert
        verify(operationRepository).getLastOperation(accountId);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("Performing deposit with existing empty account should add the deposit")
    public void performDeposit_withAnExistingEmptyAccount() throws AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        BigDecimal depositAmount = BigDecimal.valueOf(500);

        BigDecimal expectedBalance = BigDecimal.valueOf(500);
        UUID expectedOperationId = UUID.fromString("aa7ad925-59f1-43db-bae7-a0c2221b8a6a");
        Operation expected = new Operation(expectedOperationId, accountId, depositAmount, expectedBalance, LocalDateTime.now(this.clock), OperationType.DEPOSIT);

        when(uuidProvider.generate()).thenReturn(expectedOperationId);
        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.empty());

        // Act
        bankAccountService.performDeposit(accountId, depositAmount);

        // Assert
        verify(operationRepository).getLastOperation(accountId);
        verify(operationRepository).addOperation(expected);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("Performing withdrawal with sufficient balance should add the withdrawal")
    void performWithdrawal_withSufficientBalance() throws InsufficientBalanceException, AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        Operation initialOperation = new Operation(UUID.randomUUID(), accountId, initialBalance, initialBalance, LocalDateTime.now(this.clock), OperationType.DEPOSIT);

        BigDecimal withdrawalAmount = BigDecimal.valueOf(500);

        BigDecimal expectedBalance = BigDecimal.valueOf(500);
        UUID expectedOperationId = UUID.fromString("aa7ad925-59f1-43db-bae7-a0c2221b8a6a");
        Operation expected = new Operation(expectedOperationId, accountId, withdrawalAmount, expectedBalance, LocalDateTime.now(this.clock), OperationType.WITHDRAWAL);

        when(uuidProvider.generate()).thenReturn(expectedOperationId);
        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(initialOperation));

        // Act
        bankAccountService.performWithdrawal(accountId, withdrawalAmount);

        // Assert
        verify(operationRepository).getLastOperation(accountId);
        verify(operationRepository).addOperation(expected);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("Performing a withdrawal with insufficient balance should throw InsufficientBalanceException")
    void performWithdrawal_withInsufficientBalance() throws InsufficientBalanceException, AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        Operation initialOperation = new Operation(UUID.randomUUID(), accountId, initialBalance, initialBalance, LocalDateTime.now(this.clock), OperationType.DEPOSIT);

        BigDecimal withdrawalAmount = BigDecimal.valueOf(1500);

        when(operationRepository.getLastOperation(accountId)).thenReturn(Optional.of(initialOperation));

        // Act
        Assertions.assertThrows(InsufficientBalanceException.class, () -> bankAccountService.performWithdrawal(accountId, withdrawalAmount));

        // Assert
        verify(operationRepository).getLastOperation(accountId);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("performing a withdrawal with a non existing account should throw AccountNotFoundException")
    void performWithdrawal_withNonExistingAccount() throws InsufficientBalanceException, AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        BigDecimal withdrawalAmount = BigDecimal.valueOf(500);

        when(operationRepository.getLastOperation(accountId)).thenThrow(new AccountNotFoundException());

        // Act
        Assertions.assertThrows(AccountNotFoundException.class, () -> bankAccountService.performWithdrawal(accountId, withdrawalAmount));

        // Assert
        verify(operationRepository).getLastOperation(accountId);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("should display correctly with no operations")
    void displayHistory_withAnExistingAccountButNoOperations() throws AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        String expected = """
                History for account 5d9ccd9d-7050-47ab-adcf-e0fe89b913c5
                --------------------------------------------------------
                Balance: 0
                --------------------------------------------------------
                """;

        // Act
        String actual = bankAccountService.displayHistory(accountId);

        // Assert
        Assertions.assertEquals(expected, actual);

        verify(operationRepository).getOperations(accountId);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("should throw AccountNotFoundException when trying to display with a non existing account")
    void displayHistory_withNonExistingAccount() throws AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        when(operationRepository.getOperations(accountId)).thenThrow(new AccountNotFoundException());

        // Act
        Assertions.assertThrows(AccountNotFoundException.class, () -> bankAccountService.displayHistory(accountId));

        // Assert
        verify(operationRepository).getOperations(accountId);

        verifyNoMoreInteractions(operationRepository);
        verifyNoMoreInteractions(uuidProvider);
    }

    @Test
    @DisplayName("Should display correctly with operations")
    void displayHistory_withAnExistingAccountAndOperations() throws AccountNotFoundException {
        // Arrange
        UUID accountId = UUID.fromString("5d9ccd9d-7050-47ab-adcf-e0fe89b913c5");

        String expected = """
                History for account 5d9ccd9d-7050-47ab-adcf-e0fe89b913c5
                --------------------------------------------------------
                Balance: 1000.0
                --------------------------------------------------------
                04/07/2023 12:34 | -500.0       | WITHDRAWAL
                03/07/2023 21:43 | 1500.0       | DEPOSIT
                """;

        Operation firstOperation = new Operation(UUID.randomUUID(), accountId, BigDecimal.valueOf(1500.0), BigDecimal.valueOf(1500.0), LocalDateTime.of(2023, Month.JULY, 03, 21, 43), OperationType.DEPOSIT);
        Operation secondOperation = new Operation(UUID.randomUUID(), accountId, BigDecimal.valueOf(500.0), BigDecimal.valueOf(1000.0), LocalDateTime.of(2023, Month.JULY, 04, 12, 34), OperationType.WITHDRAWAL);

        when(operationRepository.getOperations(accountId)).thenReturn(List.of(firstOperation, secondOperation));

        // Act
        String actual = bankAccountService.displayHistory(accountId);

        // Assert
        Assertions.assertEquals(expected, actual);


        verify(operationRepository).getOperations(accountId);
        verifyNoMoreInteractions(operationRepository);

        verifyNoMoreInteractions(uuidProvider);
    }

}
