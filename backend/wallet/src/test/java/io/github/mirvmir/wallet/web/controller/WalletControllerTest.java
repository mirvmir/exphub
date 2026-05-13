package io.github.mirvmir.wallet.web.controller;

import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.wallet.application.service.interfaces.WalletService;
import io.github.mirvmir.wallet.exception.WalletErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WalletControllerTest {

    private WalletService walletService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        walletService = mock(WalletService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new WalletController(walletService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void withdraw_shouldReturn204() throws Exception {
        mockMvc.perform(post("/wallet/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10,
                                  "amount": 1500,
                                  "currency": "RUB"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(walletService).withdrawToCard(any());
    }

    @Test
    void withdraw_shouldReturn404_whenWalletNotFound() throws Exception {
        doThrow(new NotFoundException(
                WalletErrorCode.WALLET_NOT_FOUND,
                "Wallet for teacher id=2 not found"
        )).when(walletService).withdrawToCard(any());

        mockMvc.perform(post("/wallet/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10,
                                  "amount": 1500,
                                  "currency": "RUB"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Wallet for teacher id=2 not found"));
    }

    @Test
    void withdraw_shouldReturn503_whenDatabaseUnavailable() throws Exception {
        doThrow(new CannotGetJdbcConnectionException("Database unavailable"))
                .when(walletService).withdrawToCard(any());

        mockMvc.perform(post("/wallet/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10,
                                  "amount": 1500,
                                  "currency": "RUB"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Временная ошибка подключения к базе данных"));
    }

    @Test
    void withdraw_shouldReturn500_whenDatabaseError() throws Exception {
        doThrow(new DataAccessResourceFailureException("Database error"))
                .when(walletService).withdrawToCard(any());

        mockMvc.perform(post("/wallet/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10,
                                  "amount": 1500,
                                  "currency": "RUB"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.message").value("Ошибка при обращении к базе данных"));
    }
}