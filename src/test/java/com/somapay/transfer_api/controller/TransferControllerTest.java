package com.somapay.transfer_api.controller;

import com.somapay.transfer_api.dto.TransferRequest;
import com.somapay.transfer_api.dto.TransferResponse;
import com.somapay.transfer_api.domain.TransactionStatus;
import com.somapay.transfer_api.exception.GlobalExceptionHandler;
import com.somapay.transfer_api.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class TransferControllerTest {

	private TransferService transferService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		transferService = mock(TransferService.class);
		mockMvc = standaloneSetup(new TransferController(transferService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void createsTransferAndReturnsResponse() throws Exception {
		TransferResponse response = new TransferResponse(
				12L, 1L, 2L, new BigDecimal("25.00"),
				TransactionStatus.COMPLETED, Instant.parse("2026-01-01T00:00:00Z"));
		when(transferService.executeTransfer(any(TransferRequest.class))).thenReturn(response);

		mockMvc.perform(post("/transfers")
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "sourceAccountId": 1,
								  "targetAccountId": 2,
								  "amount": 25.00,
								  "idempotencyKey": "request-1"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.transactionId").value(12))
				.andExpect(jsonPath("$.status").value("COMPLETED"));

		verify(transferService).executeTransfer(
				new TransferRequest(1L, 2L, new BigDecimal("25.00"), "request-1"));
	}

	@Test
	void rejectsInvalidTransferRequest() throws Exception {
		mockMvc.perform(post("/transfers")
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "sourceAccountId": 1,
								  "targetAccountId": 2,
								  "amount": 0,
								  "idempotencyKey": " "
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors").isArray());

		verifyNoInteractions(transferService);
	}
}
