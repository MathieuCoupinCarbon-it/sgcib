package com.mcoupin.services;

import com.mcoupin.exceptions.AccountNotFoundException;
import com.mcoupin.exceptions.InsufficientBalanceException;
import com.mcoupin.models.Operation;
import com.mcoupin.models.OperationType;
import com.mcoupin.repositories.OperationRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankAccountServiceImpl implements BankAccountService {


    private final OperationRepository operationRepository;
    private final Clock clock;
    private final UuidProvider uuidProvider;

    public BankAccountServiceImpl(OperationRepository operationRepository, Clock clock, UuidProvider uuidProvider) {
        this.operationRepository = operationRepository;
        this.clock = clock;
        this.uuidProvider = uuidProvider;
    }


    @Override
    public void performDeposit(UUID accountId, BigDecimal amount) throws AccountNotFoundException {
        BigDecimal balance = this.operationRepository.getLastOperation(accountId).map(Operation::balance).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = balance.add(amount);

        Operation operation = new Operation(uuidProvider.generate(), accountId, amount, newBalance, LocalDateTime.now(this.clock), OperationType.DEPOSIT);

        this.operationRepository.addOperation(operation);
    }

    @Override
    public void performWithdrawal(UUID accountId, BigDecimal amount) throws InsufficientBalanceException, AccountNotFoundException {
        BigDecimal balance = this.operationRepository.getLastOperation(accountId).map(Operation::balance).orElse(BigDecimal.ZERO);

        BigDecimal newBalance = balance.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) == -1)
            throw new InsufficientBalanceException();

        Operation operation = new Operation(uuidProvider.generate(), accountId, amount, newBalance, LocalDateTime.now(this.clock), OperationType.WITHDRAWAL);

        this.operationRepository.addOperation(operation);
    }

    @Override
    public String displayHistory(UUID accountId) throws AccountNotFoundException {

        String customDateTimePattern = "dd/MM/yyyy HH:mm ";
        DateTimeFormatter customDateFormatter = DateTimeFormatter.ofPattern(customDateTimePattern);
        List<Operation> operations = this.operationRepository.getOperations(accountId);


        StringBuilder sb = new StringBuilder();

        sb.append("History for account ");
        sb.append(accountId);
        sb.append("\n");

        sb.append("--------------------------------------------------------\n");
        BigDecimal balance;

        if (operations.size() == 0) {
            balance = BigDecimal.ZERO;
        } else {
            balance = operations.get(operations.size() - 1).balance();
        }
        sb.append("Balance: ");
        sb.append(balance);
        sb.append("\n");

        sb.append("--------------------------------------------------------\n");


        List<Operation> orderedHistory = operations
                .stream()
                .sorted(Comparator.comparing(Operation::date, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        for (Operation op : orderedHistory) {


            String formatedDate = op.date().format(customDateFormatter);
            sb.append(formatedDate);
            sb.append("| ");
            String formatedAmount;
            if (op.operationType().equals(OperationType.WITHDRAWAL)) {
                formatedAmount = "-" + op.amount();

            } else {
                formatedAmount = op.amount().toString();
            }
            sb.append(String.format("%-" + 13 + "s", formatedAmount));
            sb.append("| ");
            sb.append(op.operationType());
            sb.append("\n");
        }
        return sb.toString();
    }
}
