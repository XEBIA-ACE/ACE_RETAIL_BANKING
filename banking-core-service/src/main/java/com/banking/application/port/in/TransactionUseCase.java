package com.banking.application.port.in;

import com.banking.domain.model.Transaction;
import com.banking.domain.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Inbound port — use cases for transaction management.
 */
public interface TransactionUseCase {

    Transaction recordTransaction(RecordTransactionCommand command);

    Optional<Transaction> findTransactionById(String externalId);

    List<Transaction> listTransactionsByAccount(String accountExternalId, int page, int size);

    record RecordTransactionCommand(
            String accountExternalId,
            TransactionType transactionType,
            BigDecimal amount,
            String currency,
            String description,
            String reference
    ) {}
}
