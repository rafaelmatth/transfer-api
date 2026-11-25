package com.somapay.transfer_api.config;

import com.somapay.transfer_api.domain.Account;
import com.somapay.transfer_api.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

	@Bean
	CommandLineRunner seedAccounts(AccountRepository accountRepository) {
		return args -> {
			if (accountRepository.count() == 0) {
				accountRepository.saveAll(List.of(
						new Account("Alice", new BigDecimal("1000.00")),
						new Account("Bob", new BigDecimal("500.00"))));
			}
		};
	}
}
