package com.banking.application.service;

import com.banking.application.port.in.TransactionUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.TransactionRepository;
import com.banking.domain.exception.InsufficientFundsException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Account;
import com.banking.domain.model.Transaction;
import com.banking.domain.model.TransactionStatus;
import com.banking.domain.model.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public Transaction recordTransaction(RecordTransactionCommand command) {
        Account account = accountRepository.findByExternalId(command.accountExternalId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", command.accountExternalId()));

        if (command.transactionType() == TransactionType.DEBIT) {
            if (account.getBalance().compareTo(command.amount()) < 0) {
                throw new InsufficientFundsException(command.accountExternalId());
            }
        }

        Transaction transaction = Transaction.builder()
                .externalId(UUID.randomUUID().toString())
                .accountId(account.getId())
                .transactionType(command.transactionType())
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .reference(command.reference())
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Update account balance
        BigDecimal newBalance = command.transactionType() == TransactionType.DEBIT
                ? account.getBalance().subtract(command.amount())
                : account.getBalance().add(command.amount());
        accountRepository.save(account.toBuilder().balance(newBalance).updatedAt(LocalDateTime.now()).build());

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction recorded: {} on account: {}", saved.getExternalId(), command.accountExternalId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findTransactionById(String externalId) {
        return transactionRepository.findByExternalId(externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> listTransactionsByAccount(String accountExternalId, int page, int size) {
        Account account = accountRepository.findByExternalId(accountExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountExternalId));
        return transactionRepository.findByAccountId(account.getId(), page, size);
    }
}
