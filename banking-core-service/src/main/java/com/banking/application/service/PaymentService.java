package com.banking.application.service;

import com.banking.application.port.in.PaymentUseCase;
import com.banking.application.port.out.AccountRepository;
import com.banking.application.port.out.PaymentRepository;
import com.banking.domain.exception.BusinessRuleException;
import com.banking.domain.exception.InsufficientFundsException;
import com.banking.domain.exception.ResourceNotFoundException;
import com.banking.domain.model.Account;
import com.banking.domain.model.Payment;
import com.banking.domain.model.PaymentStatus;
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
public class PaymentService implements PaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;

    @Override
    public Payment initiatePayment(InitiatePaymentCommand command) {
        Account source = accountRepository.findByExternalId(command.sourceAccountExternalId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", command.sourceAccountExternalId()));
        Account target = accountRepository.findByExternalId(command.targetAccountExternalId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", command.targetAccountExternalId()));

        if (source.getBalance().compareTo(command.amount()) < 0) {
            throw new InsufficientFundsException(command.sourceAccountExternalId());
        }

        Payment payment = Payment.builder()
                .externalId(UUID.randomUUID().toString())
                .sourceAccount(source.getId())
                .targetAccount(target.getId())
                .amount(command.amount())
                .currency(command.currency())
                .paymentType(command.paymentType())
                .description(command.description())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: {}", saved.getExternalId());
        return saved;
    }

    @Override
    public Payment processPayment(String externalId) {
        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", externalId));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Payment is not in PENDING status: " + externalId);
        }

        Account source = accountRepository.findById(payment.getSourceAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Account", String.valueOf(payment.getSourceAccount())));
        Account target = accountRepository.findById(payment.getTargetAccount())
                .orElseThrow(() -> new ResourceNotFoundException("Account", String.valueOf(payment.getTargetAccount())));

        // Debit source, credit target
        accountRepository.save(source.toBuilder()
                .balance(source.getBalance().subtract(payment.getAmount()))
                .updatedAt(LocalDateTime.now()).build());
        accountRepository.save(target.toBuilder()
                .balance(target.getBalance().add(payment.getAmount()))
                .updatedAt(LocalDateTime.now()).build());

        Payment processed = payment.toBuilder()
                .status(PaymentStatus.COMPLETED)
                .processedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(processed);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findPaymentById(String externalId) {
        return paymentRepository.findByExternalId(externalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> listPaymentsByAccount(String accountExternalId, int page, int size) {
        Account account = accountRepository.findByExternalId(accountExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountExternalId));
        return paymentRepository.findBySourceAccount(account.getId(), page, size);
    }
}
