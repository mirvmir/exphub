package io.github.mirvmir.payment.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.payment.application.service.interfaces.PaymentService;
import io.github.mirvmir.payment.exception.CardBindingException;
import io.github.mirvmir.payment.exception.PaymentErrorCode;
import io.github.mirvmir.payment.exception.PaymentException;
import io.github.mirvmir.payment.exception.PaymentGatewayUnavailableException;
import io.github.mirvmir.payment.exception.PaymentUnavailableException;
import io.github.mirvmir.payment.web.handler.PaymentExceptionHandler;
import io.github.mirvmir.payment.web.request.BankPaymentWebhookRequest;
import io.github.mirvmir.payment.web.request.BankPayoutWebhookRequest;
import io.github.mirvmir.payment.web.request.BankRefundWebhookRequest;
import io.github.mirvmir.payment.web.request.BindCardRequest;
import io.github.mirvmir.payment.web.response.BindCardResponse;
import io.github.mirvmir.payment.web.response.ConfirmPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private PaymentService paymentService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaymentController(paymentService))
                .setControllerAdvice(
                        new GlobalExceptionHandler(),
                        new PaymentExceptionHandler()
                )
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void bindCard_shouldReturn201() throws Exception {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "30",
                "123"
        );
        BindCardResponse response = new BindCardResponse(
                10L,
                "**** **** **** 1111",
                "MIR"
        );

        when(paymentService.bindCard(any())).thenReturn(response);

        mockMvc.perform(post("/payments/card/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value(10L))
                .andExpect(jsonPath("$.maskedPan").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.paymentSystem").value("MIR"));

        verify(paymentService).bindCard(argThat(actual ->
                actual != null
                        && actual.cardNumber().equals("4111111111111111")
                        && actual.cardHolder().equals("IVAN IVANOV")
        ));
    }

    @Test
    void confirmPayment_shouldReturn200() throws Exception {
        ConfirmPaymentResponse response = new ConfirmPaymentResponse(
                1L,
                "PROCESSING"
        );

        when(paymentService.startPayment(1L, 10L))
                .thenReturn(response);

        mockMvc.perform(post("/payments/1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "cardId": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentId").value(1L))
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        verify(paymentService).startPayment(1L, 10L);
    }

    @Test
    void confirmPayment_shouldReturn404_whenPaymentNotFound() throws Exception {
        when(paymentService.startPayment(1L, 10L))
                .thenThrow(new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        mockMvc.perform(post("/payments/1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment card not found"));
    }

    @Test
    void confirmPayment_shouldReturn409_whenPaymentAlreadyProcessed() throws Exception {
        when(paymentService.startPayment(1L, 10L))
                .thenThrow(new PaymentException("Платёж уже обработан"));

        mockMvc.perform(post("/payments/1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAYMENT_ERROR"))
                .andExpect(jsonPath("$.message").value("Платёж уже обработан"));
    }

    @Test
    void bindCard_shouldReturn409_whenCardBindingError() throws Exception {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "30",
                "123"
        );

        when(paymentService.bindCard(any()))
                .thenThrow(new CardBindingException("Карта уже подключена"));

        mockMvc.perform(post("/payments/card/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CARD_BINDING_ERROR"))
                .andExpect(jsonPath("$.message").value("Карта уже подключена"));
    }

    @Test
    void bindCard_shouldReturn503_whenPaymentUnavailable() throws Exception {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "30",
                "123"
        );

        when(paymentService.bindCard(any()))
                .thenThrow(new PaymentUnavailableException("Оплата временно недоступна"));

        mockMvc.perform(post("/payments/card/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PAYMENT_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Оплата временно недоступна"));
    }

    @Test
    void confirmPayment_shouldReturn503_whenPaymentGatewayUnavailable() throws Exception {
        when(paymentService.startPayment(1L, 10L))
                .thenThrow(new PaymentGatewayUnavailableException("Оплата временно недоступна"));

        mockMvc.perform(post("/payments/1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 10
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PAYMENT_GATEWAY_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Оплата временно недоступна"));
    }

    @Test
    void bindCard_shouldReturn503_whenDatabaseUnavailable() throws Exception {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "30",
                "123"
        );

        when(paymentService.bindCard(any()))
                .thenThrow(new CannotGetJdbcConnectionException("db unavailable"));

        mockMvc.perform(post("/payments/card/bind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Временная ошибка подключения к базе данных"));
    }

    @Test
    void handleBankWebhook_shouldReturn200() throws Exception {
        BankPaymentWebhookRequest request = new BankPaymentWebhookRequest(
                "payment-ext-1",
                "SUCCEEDED",
                null
        );

        mockMvc.perform(post("/payments/webhook/bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(paymentService).handleBankWebhook(argThat(actual ->
                actual != null
                        && actual.externalPaymentId().equals("payment-ext-1")
                        && actual.status().equals("SUCCEEDED")
        ));
    }

    @Test
    void handleBankPayoutWebhook_shouldReturn200() throws Exception {
        BankPayoutWebhookRequest request = new BankPayoutWebhookRequest(
                "payout-ext-1",
                "SUCCEEDED"
        );

        mockMvc.perform(post("/payments/webhook/bank/payout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(paymentService).handleBankPayoutWebhook(argThat(actual ->
                actual != null
                        && actual.externalPayoutId().equals("payout-ext-1")
                        && actual.status().equals("SUCCEEDED")
        ));
    }

    @Test
    void handleBankRefundWebhook_shouldReturn200() throws Exception {
        BankRefundWebhookRequest request = new BankRefundWebhookRequest(
                "refund-ext-1",
                "SUCCEEDED"
        );

        mockMvc.perform(post("/payments/webhook/bank/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(paymentService).handleBankRefundWebhook(argThat(actual ->
                actual != null
                        && actual.externalRefundId().equals("refund-ext-1")
                        && actual.status().equals("SUCCEEDED")
        ));
    }
}
