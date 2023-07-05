package com.mcoupin.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Operation(UUID id, UUID accountId,
                        BigDecimal amount, BigDecimal balance,
                        LocalDateTime date, OperationType operationType) {
}
