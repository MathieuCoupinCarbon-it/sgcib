package com.mcoupin.repositories;

import com.mcoupin.exceptions.AccountNotFoundException;
import com.mcoupin.models.Operation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationRepository {

    Optional<Operation> getLastOperation(UUID accountId) throws AccountNotFoundException;

    List<Operation> getOperations(UUID accountID) throws AccountNotFoundException;

    void addOperation(Operation operation) throws AccountNotFoundException;
}
