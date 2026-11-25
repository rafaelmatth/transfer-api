package com.somapay.transfer_api;

import com.somapay.transfer_api.domain.Account;
import com.somapay.transfer_api.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransferApiApplicationTests {

	@Autowired
	private AccountRepository accountRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void seedsInitialAccounts() {
		List<Account> accounts = accountRepository.findAll();

		assertEquals(2, accounts.size());
		assertTrue(accounts.stream().anyMatch(account ->
				"Alice".equals(account.getHolderName())
						&& new BigDecimal("1000.00").equals(account.getBalance())));
		assertTrue(accounts.stream().anyMatch(account ->
				"Bob".equals(account.getHolderName())
						&& new BigDecimal("500.00").equals(account.getBalance())));
	}
}
