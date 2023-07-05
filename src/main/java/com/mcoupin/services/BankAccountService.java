package com.mcoupin.services;


import com.mcoupin.exceptions.AccountNotFoundException;
import com.mcoupin.exceptions.InsuficientBalanceException;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankAccountService {
    void performDeposit(UUID accountId, BigDecimal amount) throws AccountNotFoundException;

    void performWithdrawal(UUID accountId, BigDecimal amount) throws AccountNotFoundException, InsuficientBalanceException;

    String displayHistory(UUID accountId) throws AccountNotFoundException;
}
