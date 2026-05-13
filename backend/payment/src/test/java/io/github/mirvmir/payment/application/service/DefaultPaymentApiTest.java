package io.github.mirvmir.payment.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.payment.api.dto.CreatePaymentRequest;
import io.github.mirvmir.payment.api.dto.CreatePaymentResponse;
import io.github.mirvmir.payment.api.dto.CreatePayoutRequest;
import io.github.mirvmir.payment.api.dto.CreatePayoutResponse;
import io.github.mirvmir.payment.api.dto.CreateRefundRequest;
import io.github.mirvmir.payment.api.dto.CreateRefundResponse;
import io.github.mirvmir.payment.application.integration.BankPaymentGatewayClient;
import io.github.mirvmir.payment.application.integration.dto.BankPayoutResponse;
import io.github.mirvmir.payment.application.integration.dto.BankRefundResponse;
import io.github.mirvmir.payment.application.service.port.repository.PaymentRepository;
import io.github.mirvmir.payment.application.service.port.repository.PayoutRepository;
import io.github.mirvmir.payment.application.service.port.repository.RefundRepository;
import io.github.mirvmir.payment.application.service.port.repository.UserCardRepository;
import io.github.mirvmir.payment.domain.Payment;
import io.github.mirvmir.payment.domain.PaymentStatus;
import io.github.mirvmir.payment.domain.Payout;
import io.github.mirvmir.payment.domain.PayoutStatus;
import io.github.mirvmir.payment.domain.Refund;
import io.github.mirvmir.payment.domain.RefundStatus;
import io.github.mirvmir.payment.domain.UserCard;
import io.github.mirvmir.payment.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DefaultPaymentApiTest {

    private UserCardRepository userCardRepository;
    private PaymentRepository paymentRepository;
    private PayoutRepository payoutRepository;
    private RefundRepository refundRepository;
    private BankPaymentGatewayClient bankPaymentGatewayClient;

    private DefaultPaymentApi api;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-05-12T10:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        userCardRepository = mock(UserCardRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        payoutRepository = mock(PayoutRepository.class);
        refundRepository = mock(RefundRepository.class);
        bankPaymentGatewayClient = mock(BankPaymentGatewayClient.class);

        api = new DefaultPaymentApi(
                userCardRepository,
                paymentRepository,
                payoutRepository,
                refundRepository,
                bankPaymentGatewayClient,
                clock
        );
    }

    @Test
    void createPayment_shouldSavePaymentAndReturnResponse() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                2L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Оплата занятия",
                100L
        );
        Payment savedPayment = payment(1L, PaymentStatus.CREATED, null);

        when(paymentRepository.save(any())).thenReturn(savedPayment);

        CreatePaymentResponse response = api.createPayment(request);

        assertEquals(1L, response.paymentId());
        assertEquals("CREATED", response.status());
        verify(paymentRepository).save(argThat(actual ->
                actual.getUserId().equals(2L)
                        && actual.getPrice().getAmount().compareTo(new BigDecimal("1500")) == 0
                        && actual.getOrderId().equals(100L)
                        && actual.getStatus() == PaymentStatus.CREATED
        ));
    }

    @Test
    void createPayout_shouldCreatePayoutAndMarkProcessing() {
        CreatePayoutRequest request = new CreatePayoutRequest(
                2L,
                10L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Вывод средств",
                200L
        );

        UserCard card = card(10L, 2L, true, true);

        when(userCardRepository.findById(10L)).thenReturn(card);
        when(payoutRepository.saveOrUpdate(any(Payout.class)))
                .thenAnswer(invocation -> {
                    Payout payout = invocation.getArgument(0);

                    if (payout.getId() == null) {
                        ReflectionTestUtils.setField(payout, "id", 1L);
                    }

                    return payout;
                });

        when(bankPaymentGatewayClient.payout(any()))
                .thenReturn(new BankPayoutResponse("external-payout-1"));

        CreatePayoutResponse response = api.createPayout(request);

        assertEquals(1L, response.payoutId());
        assertEquals("PROCESSING", response.status());

        ArgumentCaptor<Payout> payoutCaptor =
                ArgumentCaptor.forClass(Payout.class);

        verify(payoutRepository, times(2))
                .saveOrUpdate(payoutCaptor.capture());

        Payout savedPayout = payoutCaptor.getAllValues().get(1);

        assertEquals(PayoutStatus.PROCESSING, savedPayout.getStatus());
        assertEquals("external-payout-1", savedPayout.getExternalPayoutId());
    }

    @Test
    void createPayout_shouldThrowNotFound_whenCardDoesNotExist() {
        CreatePayoutRequest request = new CreatePayoutRequest(
                2L,
                10L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Вывод средств",
                200L
        );

        when(userCardRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> api.createPayout(request));
        verifyNoInteractions(payoutRepository, bankPaymentGatewayClient);
    }

    @Test
    void refundPayment_shouldCreateRefundAndMarkProcessing() {
        CreateRefundRequest request = new CreateRefundRequest(
                100L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Возврат по заказу"
        );
        Payment payment = payment(1L, PaymentStatus.SUCCEEDED, "external-payment-1");
        Refund createdRefund = refund(5L, payment.getId(), RefundStatus.CREATED, null);

        when(paymentRepository.findByOrderId(100L)).thenReturn(payment);
        when(refundRepository.existsByPaymentIdAndStatusIn(
                payment.getId(),
                List.of(RefundStatus.CREATED, RefundStatus.PROCESSING, RefundStatus.SUCCEEDED)
        )).thenReturn(false);
        when(refundRepository.saveOrUpdate(any())).thenReturn(createdRefund);
        when(bankPaymentGatewayClient.refund(any())).thenReturn(new BankRefundResponse("external-refund-1"));

        CreateRefundResponse response = api.refundPayment(request);

        assertEquals(5L, response.refundId());
        assertEquals(1L, response.paymentId());
        assertEquals("PROCESSING", response.status());
        assertEquals(RefundStatus.PROCESSING, createdRefund.getStatus());
        assertEquals("external-refund-1", createdRefund.getExternalRefundId());
        verify(refundRepository, times(2)).saveOrUpdate(any());
    }

    @Test
    void refundPayment_shouldThrowNotFound_whenPaymentDoesNotExist() {
        CreateRefundRequest request = new CreateRefundRequest(
                100L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Возврат по заказу"
        );

        when(paymentRepository.findByOrderId(100L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> api.refundPayment(request));
        verifyNoInteractions(refundRepository, bankPaymentGatewayClient);
    }

    @Test
    void refundPayment_shouldThrowPaymentException_whenPaymentIsNotSucceeded() {
        CreateRefundRequest request = new CreateRefundRequest(
                100L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Возврат по заказу"
        );
        Payment payment = payment(1L, PaymentStatus.PROCESSING, "external-payment-1");

        when(paymentRepository.findByOrderId(100L)).thenReturn(payment);

        PaymentException exception = assertThrows(PaymentException.class,
                () -> api.refundPayment(request));

        assertEquals("Возврат возможен только по успешному платежу", exception.getMessage());
    }

    @Test
    void refundPayment_shouldThrowPaymentException_whenAmountIsGreaterThanPaymentAmount() {
        CreateRefundRequest request = new CreateRefundRequest(
                100L,
                new BigDecimal("2000"),
                Currency.getInstance("RUB"),
                "Возврат по заказу"
        );
        Payment payment = payment(1L, PaymentStatus.SUCCEEDED, "external-payment-1");

        when(paymentRepository.findByOrderId(100L)).thenReturn(payment);

        PaymentException exception = assertThrows(PaymentException.class,
                () -> api.refundPayment(request));

        assertEquals("Сумма возврата больше суммы платежа", exception.getMessage());
    }

    @Test
    void refundPayment_shouldThrowPaymentException_whenRefundAlreadyExists() {
        CreateRefundRequest request = new CreateRefundRequest(
                100L,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "Возврат по заказу"
        );
        Payment payment = payment(1L, PaymentStatus.SUCCEEDED, "external-payment-1");

        when(paymentRepository.findByOrderId(100L)).thenReturn(payment);
        when(refundRepository.existsByPaymentIdAndStatusIn(
                payment.getId(),
                List.of(RefundStatus.CREATED, RefundStatus.PROCESSING, RefundStatus.SUCCEEDED)
        )).thenReturn(true);

        PaymentException exception = assertThrows(PaymentException.class,
                () -> api.refundPayment(request));

        assertEquals("Возврат по платежу уже создан", exception.getMessage());
        verify(refundRepository, never()).saveOrUpdate(any());
    }

    private Payment payment(Long id,
                            PaymentStatus status,
                            String externalPaymentId) {
        return Payment.load(
                id,
                2L,
                externalPaymentId,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                status,
                "Оплата занятия",
                100L,
                Instant.parse("2026-05-12T09:00:00Z"),
                status == PaymentStatus.SUCCEEDED
                        ? Instant.parse("2026-05-12T09:30:00Z")
                        : null
        );
    }

    private UserCard card(Long id,
                          Long userId,
                          boolean active,
                          boolean defaultCard) {
        return UserCard.load(
                id,
                userId,
                "bank-card-1",
                "token-1",
                "**** **** **** 1111",
                "1111",
                "MIR",
                active,
                defaultCard,
                Instant.parse("2026-05-12T09:00:00Z"),
                Instant.parse("2026-05-12T09:00:00Z")
        );
    }

    private Payout payout(Long id,
                          PayoutStatus status,
                          String externalPayoutId) {
        return Payout.load(
                id,
                2L,
                10L,
                externalPayoutId,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                status,
                "Вывод средств",
                200L,
                Instant.parse("2026-05-12T09:00:00Z"),
                null
        );
    }

    private Refund refund(Long id,
                          Long paymentId,
                          RefundStatus status,
                          String externalRefundId) {
        return Refund.load(
                id,
                paymentId,
                externalRefundId,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                status,
                "Возврат",
                Instant.parse("2026-05-12T09:00:00Z"),
                null
        );
    }
}
