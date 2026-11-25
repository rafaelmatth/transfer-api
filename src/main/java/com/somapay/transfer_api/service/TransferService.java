package com.somapay.transfer_api.service;

import com.somapay.transfer_api.domain.Account;
import com.somapay.transfer_api.domain.Transaction;
import com.somapay.transfer_api.domain.TransactionStatus;
import com.somapay.transfer_api.dto.TransferRequest;
import com.somapay.transfer_api.dto.TransferResponse;
import com.somapay.transfer_api.exception.AccountNotFoundException;
import com.somapay.transfer_api.exception.InsufficientBalanceException;
import com.somapay.transfer_api.repository.AccountRepository;
import com.somapay.transfer_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
public class TransferService {

	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public TransferService(AccountRepository accountRepository,
			TransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public TransferResponse executeTransfer(TransferRequest request) {
		var existingTransaction = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
		if (existingTransaction.isPresent()) {
			return toResponse(existingTransaction.get());
		}

		if (Objects.equals(request.sourceAccountId(), request.targetAccountId())) {
			throw new IllegalArgumentException("Source and target accounts must be different");
		}

		Account sourceAccount = accountRepository.findById(request.sourceAccountId())
				.orElseThrow(() -> new AccountNotFoundException(request.sourceAccountId()));
		Account targetAccount = accountRepository.findById(request.targetAccountId())
				.orElseThrow(() -> new AccountNotFoundException(request.targetAccountId()));

		if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
			throw new InsufficientBalanceException("Insufficient balance");
		}

		sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
		targetAccount.setBalance(targetAccount.getBalance().add(request.amount()));
		accountRepository.save(sourceAccount);
		accountRepository.save(targetAccount);

		Transaction transaction = new Transaction(
				request.sourceAccountId(),
				request.targetAccountId(),
				request.amount(),
				request.idempotencyKey(),
				TransactionStatus.COMPLETED,
				Instant.now());
		return toResponse(transactionRepository.save(transaction));
	}

	private static TransferResponse toResponse(Transaction transaction) {
		return new TransferResponse(
				transaction.getId(),
				transaction.getSourceAccountId(),
				transaction.getTargetAccountId(),
				transaction.getAmount(),
				transaction.getStatus(),
				transaction.getCreatedAt());
	}
}
