package com.somapay.transfer_api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long sourceAccountId;

	private Long targetAccountId;

	private BigDecimal amount;

	@Column(unique = true, nullable = false)
	private String idempotencyKey;

	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	private Instant createdAt;

	protected Transaction() {
	}

	public Transaction(Long sourceAccountId, Long targetAccountId, BigDecimal amount,
			String idempotencyKey, TransactionStatus status, Instant createdAt) {
		this.sourceAccountId = sourceAccountId;
		this.targetAccountId = targetAccountId;
		this.amount = amount;
		this.idempotencyKey = idempotencyKey;
		this.status = status;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSourceAccountId() {
		return sourceAccountId;
	}

	public void setSourceAccountId(Long sourceAccountId) {
		this.sourceAccountId = sourceAccountId;
	}

	public Long getTargetAccountId() {
		return targetAccountId;
	}

	public void setTargetAccountId(Long targetAccountId) {
		this.targetAccountId = targetAccountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
