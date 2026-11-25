package com.somapay.transfer_api.repository;

import com.somapay.transfer_api.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
