package com.banking.application.port.out;

import com.banking.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port — persistence operations for Transaction.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByExternalId(String externalId);

    List<Transaction> findByAccountId(Long accountId, int page, int size);
}
