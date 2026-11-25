package com.somapay.transfer_api.service;

import com.somapay.transfer_api.domain.Account;
import com.somapay.transfer_api.domain.Transaction;
import com.somapay.transfer_api.domain.TransactionStatus;
import com.somapay.transfer_api.dto.TransferRequest;
import com.somapay.transfer_api.dto.TransferResponse;
import com.somapay.transfer_api.repository.AccountRepository;
import com.somapay.transfer_api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransferServiceTest {

	private AccountRepository accountRepository;
	private TransactionRepository transactionRepository;
	private TransferService transferService;

	@BeforeEach
	void setUp() {
		accountRepository = mock(AccountRepository.class);
		transactionRepository = mock(TransactionRepository.class);
		transferService = new TransferService(accountRepository, transactionRepository);
	}

	@Test
	void returnsExistingTransactionWithoutLoadingAccounts() {
		Transaction existing = new Transaction(
				1L, 2L, new BigDecimal("25.00"), "key-1",
				TransactionStatus.COMPLETED, java.time.Instant.parse("2026-01-01T00:00:00Z"));
		existing.setId(42L);
		when(transactionRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));

		TransferResponse response = transferService.executeTransfer(
				new TransferRequest(1L, 2L, new BigDecimal("25.00"), "key-1"));

		assertEquals(42L, response.transactionId());
		assertEquals(1L, response.sourceAccountId());
		assertEquals(2L, response.targetAccountId());
		assertEquals(TransactionStatus.COMPLETED, response.status());
		verifyNoInteractions(accountRepository);
	}

	@Test
	void transfersAmountAndPersistsCompletedTransaction() {
		Account source = mock(Account.class);
		Account target = mock(Account.class);
		when(source.getBalance()).thenReturn(new BigDecimal("100.00"));
		when(target.getBalance()).thenReturn(new BigDecimal("10.00"));
		when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
		when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
		when(transactionRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
			Transaction transaction = invocation.getArgument(0);
			transaction.setId(55L);
			return transaction;
		});

		TransferResponse response = transferService.executeTransfer(
				new TransferRequest(1L, 2L, new BigDecimal("25.00"), "key-2"));

		verify(source).setBalance(new BigDecimal("75.00"));
		verify(target).setBalance(new BigDecimal("35.00"));
		verify(accountRepository).save(source);
		verify(accountRepository).save(target);
		assertEquals(55L, response.transactionId());
		assertEquals(new BigDecimal("25.00"), response.amount());
		assertEquals(TransactionStatus.COMPLETED, response.status());
		assertNotNull(response.createdAt());
	}
}
