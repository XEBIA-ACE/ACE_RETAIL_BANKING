package com.bank.core.account.repository;

import com.bank.core.account.domain.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByExternalId(String externalId);

    @Query("select a from Account a join fetch a.customer where a.externalId = :externalId")
    Optional<Account> findWithCustomerByExternalId(@Param("externalId") String externalId);
}
