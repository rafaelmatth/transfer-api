package com.somapay.transfer_api.repository;

import com.somapay.transfer_api.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
