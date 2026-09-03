```java
package com.bank.core.account.repository;

import com.bank.core.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a JOIN Customer c ON a.customerId = c.id WHERE c.externalId = :customerExternalId")
    List<Account> findAllByCustomerExternalId(@Param("customerExternalId") String customerExternalId);
}
```