package com.mcoupin.services;

import com.mcoupin.exceptions.AccountNotFoundException;
import com.mcoupin.exceptions.InsuficientBalanceException;
import com.mcoupin.models.Operation;
import com.mcoupin.models.OperationType;
import com.mcoupin.repositories.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BankAccountServiceImpl implements BankAccountService {


    private final OperationRepository operationRepository;

    public BankAccountServiceImpl(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }


    @Override
    public void performDeposit(UUID accountId, BigDecimal amount) throws AccountNotFoundException {
        BigDecimal balance = this.operationRepository.getLastOperation(accountId).map(Operation::balance).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = balance.add(amount);

        Operation operation = new Operation(UUID.randomUUID(), accountId, amount, newBalance, LocalDateTime.now(), OperationType.DEPOSIT);

        this.operationRepository.addOperation(operation);
    }

    @Override
    public void performWithdrawal(UUID accountId, BigDecimal amount) throws AccountNotFoundException, InsuficientBalanceException {

    }

    @Override
    public String displayHistory(UUID accountId) throws AccountNotFoundException {
        return null;
    }
}
