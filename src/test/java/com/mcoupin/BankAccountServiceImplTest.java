package com.mcoupin;

import com.mcoupin.exceptions.AccountNotFoundException;
import com.mcoupin.models.Operation;
import com.mcoupin.models.OperationType;
import com.mcoupin.repositories.OperationRepository;
import com.mcoupin.services.BankAccountServiceImpl;
import com.mcoupin.services.UuidProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

}
