package com.somapay.transfer_api.dto;

import com.somapay.transfer_api.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
		Long transactionId,
		Long sourceAccountId,
		Long targetAccountId,
		BigDecimal amount,
		TransactionStatus status,
		Instant createdAt) {
}
