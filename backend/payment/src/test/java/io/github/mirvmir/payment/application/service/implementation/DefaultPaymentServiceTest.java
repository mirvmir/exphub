package io.github.mirvmir.payment.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import io.github.mirvmir.payment.application.integration.BankPaymentGatewayClient;
import io.github.mirvmir.payment.application.integration.dto.BankBindCardResponse;
import io.github.mirvmir.payment.application.integration.dto.BankPayResponse;
import io.github.mirvmir.payment.application.properties.BankProperties;
import io.github.mirvmir.payment.application.service.port.event.PaymentEventPublisher;
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
import io.github.mirvmir.payment.exception.CardBindingException;
import io.github.mirvmir.payment.exception.PaymentException;
import io.github.mirvmir.payment.web.request.BankPaymentWebhookRequest;
import io.github.mirvmir.payment.web.request.BankPayoutWebhookRequest;
import io.github.mirvmir.payment.web.request.BankRefundWebhookRequest;
import io.github.mirvmir.payment.web.request.BindCardRequest;
import io.github.mirvmir.payment.web.response.BindCardResponse;
import io.github.mirvmir.payment.web.response.ConfirmPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DefaultPaymentServiceTest {

    private EnrollmentApi enrollmentApi;
    private IdentityApi identityApi;
    private BankProperties bankProperties;
    private BankPaymentGatewayClient bankPaymentGatewayClient;
    private UserCardRepository userCardRepository;
    private PaymentRepository paymentRepository;
    private PayoutRepository payoutRepository;
    private RefundRepository refundRepository;
    private PaymentEventPublisher eventPublisher;

    private DefaultPaymentService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-05-12T10:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        enrollmentApi = mock(EnrollmentApi.class);
        identityApi = mock(IdentityApi.class);
        bankProperties = mock(BankProperties.class);
        bankPaymentGatewayClient = mock(BankPaymentGatewayClient.class);
        userCardRepository = mock(UserCardRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        payoutRepository = mock(PayoutRepository.class);
        refundRepository = mock(RefundRepository.class);
        eventPublisher = mock(PaymentEventPublisher.class);

        when(bankProperties.getPaymentWebhookUrl()).thenReturn("http://localhost/payments/webhook/bank");

        service = new DefaultPaymentService(
                enrollmentApi,
                identityApi,
                bankProperties,
                bankPaymentGatewayClient,
                userCardRepository,
                paymentRepository,
                payoutRepository,
                refundRepository,
                eventPublisher,
                clock
        );
    }

    @Test
    void bindCard_shouldSaveCardAndReturnResponse() {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "2030",
                "123"
        );
        BankBindCardResponse bankResponse = new BankBindCardResponse(
                "bank-card-1",
                "token-1",
                "**** **** **** 1111",
                "1111",
                "MIR",
                "SUCCEEDED"
        );
        UserCard savedCard = card(10L, 1L, true, true);

        when(bankPaymentGatewayClient.bindCard(any())).thenReturn(bankResponse);
        when(userCardRepository.existsByUserIdAndCardToken(1L, "token-1")).thenReturn(false);
        when(userCardRepository.existsByUserId(1L)).thenReturn(false);
        when(userCardRepository.save(any())).thenReturn(savedCard);
        when(identityApi.getCurrentUserId()).thenReturn(1L);

        BindCardResponse response = service.bindCard(request);

        assertEquals(10L, response.cardId());
        assertEquals("**** **** **** 1111", response.maskedPan());
        assertEquals("MIR", response.paymentSystem());
        verify(bankPaymentGatewayClient).checkAvailability();
        verify(userCardRepository).save(argThat(actual ->
                actual.getUserId().equals(1L)
                        && actual.getBankCardId().equals("bank-card-1")
                        && actual.getCardToken().equals("token-1")
                        && actual.isDefaultCard()
        ));
    }

    @Test
    void bindCard_shouldThrowCardBindingException_whenBankStatusIsNotSucceeded() {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "2030",
                "123"
        );
        BankBindCardResponse bankResponse = new BankBindCardResponse(
                "bank-card-1",
                "token-1",
                "**** **** **** 1111",
                "1111",
                "MIR",
                "FAILED"
        );

        when(bankPaymentGatewayClient.bindCard(any())).thenReturn(bankResponse);
        when(identityApi.getCurrentUserId()).thenReturn(1L);

        CardBindingException exception = assertThrows(CardBindingException.class,
                () -> service.bindCard(request));

        assertEquals("Не удалось подключить карту", exception.getMessage());
        verify(userCardRepository, never()).save(any());
    }

    @Test
    void bindCard_shouldThrowCardBindingException_whenCardAlreadyBound() {
        BindCardRequest request = new BindCardRequest(
                "4111111111111111",
                "IVAN IVANOV",
                "12",
                "2030",
                "123"
        );
        BankBindCardResponse bankResponse = new BankBindCardResponse(
                "bank-card-1",
                "token-1",
                "**** **** **** 1111",
                "1111",
                "MIR",
                "SUCCEEDED"
        );

        when(bankPaymentGatewayClient.bindCard(any())).thenReturn(bankResponse);
        when(userCardRepository.existsByUserIdAndCardToken(1L, "token-1")).thenReturn(true);
        when(identityApi.getCurrentUserId()).thenReturn(1L);

        CardBindingException exception = assertThrows(CardBindingException.class,
                () -> service.bindCard(request));

        assertEquals("Карта уже подключена", exception.getMessage());
        verify(userCardRepository, never()).save(any());
    }

    @Test
    void startPayment_shouldReturnCurrentStatus_whenPaymentAlreadyProcessing() {
        Payment payment = payment(1L, 2L, PaymentStatus.PROCESSING, "external-payment-1");

        when(paymentRepository.findById(1L)).thenReturn(payment);

        ConfirmPaymentResponse response = service.startPayment(1L, 10L);

        assertEquals(1L, response.paymentId());
        assertEquals("PROCESSING", response.status());
        verifyNoInteractions(enrollmentApi, userCardRepository, bankPaymentGatewayClient);
    }

    @Test
    void startPayment_shouldThrowNotFound_whenPaymentDoesNotExist() {
        when(paymentRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.startPayment(1L, 10L));
        verifyNoInteractions(enrollmentApi, userCardRepository, bankPaymentGatewayClient);
    }

    @Test
    void startPayment_shouldThrowPaymentException_whenPaymentCancelled() {
        Payment payment = payment(1L, 2L, PaymentStatus.CANCELLED, null);

        when(paymentRepository.findById(1L)).thenReturn(payment);

        PaymentException exception = assertThrows(PaymentException.class,
                () -> service.startPayment(1L, 10L));

        assertEquals("Платёж отменён", exception.getMessage());
    }

    @Test
    void startPayment_shouldExpireOrderAndThrowPaymentException_whenOrderExpired() {
        Payment payment = payment(1L, 2L, PaymentStatus.CREATED, null);

        when(paymentRepository.findById(1L)).thenReturn(payment);
        when(enrollmentApi.isOrderCancelled(100L)).thenReturn(false);
        when(enrollmentApi.isOrderExpired(100L, Instant.now(clock))).thenReturn(true);

        PaymentException exception = assertThrows(PaymentException.class,
                () -> service.startPayment(1L, 10L));

        assertEquals("Время оплаты истекло", exception.getMessage());
        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        verify(enrollmentApi).expireOrder(100L, Instant.now(clock));
        verify(paymentRepository).save(payment);
        verifyNoInteractions(userCardRepository, bankPaymentGatewayClient);
    }

    @Test
    void startPayment_shouldThrowNotFound_whenCardDoesNotBelongToPaymentUser() {
        Payment payment = payment(1L, 2L, PaymentStatus.CREATED, null);
        UserCard card = card(10L, 999L, true, false);

        when(paymentRepository.findById(1L)).thenReturn(payment);
        when(enrollmentApi.isOrderCancelled(100L)).thenReturn(false);
        when(enrollmentApi.isOrderExpired(100L, Instant.now(clock))).thenReturn(false);
        when(userCardRepository.findById(10L)).thenReturn(card);

        assertThrows(NotFoundException.class,
                () -> service.startPayment(1L, 10L));

        verify(enrollmentApi).markPaymentProcessing(100L, Instant.now(clock));
        verify(bankPaymentGatewayClient, never()).pay(any());
    }

    @Test
    void startPayment_shouldStartPaymentAndReturnProcessing() {
        Payment payment = payment(1L, 2L, PaymentStatus.CREATED, null);
        UserCard card = card(10L, 2L, true, true);

        when(paymentRepository.findById(1L)).thenReturn(payment);
        when(enrollmentApi.isOrderCancelled(100L)).thenReturn(false);
        when(enrollmentApi.isOrderExpired(100L, Instant.now(clock))).thenReturn(false);
        when(userCardRepository.findById(10L)).thenReturn(card);
        when(bankPaymentGatewayClient.pay(any())).thenReturn(new BankPayResponse("external-payment-1", "PROCESSING"));

        ConfirmPaymentResponse response = service.startPayment(1L, 10L);

        assertEquals(1L, response.paymentId());
        assertEquals("PROCESSING", response.status());
        assertEquals(PaymentStatus.PROCESSING, payment.getStatus());
        assertEquals("external-payment-1", payment.getExternalPaymentId());
        verify(enrollmentApi).markPaymentProcessing(100L, Instant.now(clock));
        verify(bankPaymentGatewayClient).checkAvailability();
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleBankWebhook_shouldMarkPaymentSucceededAndPublishEvent() {
        Payment payment = payment(1L, 2L, PaymentStatus.PROCESSING, "external-payment-1");
        BankPaymentWebhookRequest request = new BankPaymentWebhookRequest(
                "external-payment-1",
                "SUCCEEDED",
                null
        );

        when(paymentRepository.findByExternalPaymentId("external-payment-1")).thenReturn(payment);

        service.handleBankWebhook(request);

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertEquals(Instant.now(clock), payment.getPaidAt());
        verify(eventPublisher).publishPaymentSucceeded(any(PaymentSucceededEvent.class));
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleBankWebhook_shouldMarkPaymentFailed() {
        Payment payment = payment(1L, 2L, PaymentStatus.PROCESSING, "external-payment-1");
        BankPaymentWebhookRequest request = new BankPaymentWebhookRequest(
                "external-payment-1",
                "FAILED",
                "declined"
        );

        when(paymentRepository.findByExternalPaymentId("external-payment-1")).thenReturn(payment);

        service.handleBankWebhook(request);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(eventPublisher, never()).publishPaymentSucceeded(any());
        verify(paymentRepository).save(payment);
    }

    @Test
    void handleBankWebhook_shouldThrowPaymentException_whenStatusIsIncorrect() {
        Payment payment = payment(1L, 2L, PaymentStatus.PROCESSING, "external-payment-1");
        BankPaymentWebhookRequest request = new BankPaymentWebhookRequest(
                "external-payment-1",
                "CREATED",
                null
        );

        when(paymentRepository.findByExternalPaymentId("external-payment-1")).thenReturn(payment);

        PaymentException exception = assertThrows(PaymentException.class,
                () -> service.handleBankWebhook(request));

        assertEquals("Некорректный статус платежа от банка", exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleBankPayoutWebhook_shouldMarkPayoutSucceeded() {
        Payout payout = payout(1L, PayoutStatus.PROCESSING, "external-payout-1");
        BankPayoutWebhookRequest request = new BankPayoutWebhookRequest(
                "external-payout-1",
                "SUCCEEDED",
                null
        );

        when(payoutRepository.findByExternalPayoutId("external-payout-1")).thenReturn(payout);

        service.handleBankPayoutWebhook(request);

        assertEquals(PayoutStatus.SUCCEEDED, payout.getStatus());
        verify(eventPublisher).publishPayoutSucceeded(any());
        verify(payoutRepository).saveOrUpdate(payout);
    }

    @Test
    void handleBankRefundWebhook_shouldMarkRefundSucceededAndPublishEvent() {
        Refund refund = refund(1L, 1L, RefundStatus.PROCESSING, "external-refund-1");
        Payment payment = payment(1L, 2L, PaymentStatus.SUCCEEDED, "external-payment-1");
        BankRefundWebhookRequest request = new BankRefundWebhookRequest(
                "external-refund-1",
                "SUCCEEDED",
                null
        );

        when(refundRepository.findByExternalRefundId("external-refund-1")).thenReturn(refund);
        when(paymentRepository.findById(1L)).thenReturn(payment);

        service.handleBankRefundWebhook(request);

        assertEquals(RefundStatus.SUCCEEDED, refund.getStatus());
        assertEquals(Instant.now(clock), refund.getRefundedAt());
        verify(eventPublisher).publishPaymentRefunded(any());
        verify(refundRepository).saveOrUpdate(refund);
    }

    @Test
    void handleBankRefundWebhook_shouldDoNothing_whenRefundAlreadySucceeded() {
        Refund refund = refund(1L, 1L, RefundStatus.SUCCEEDED, "external-refund-1");
        BankRefundWebhookRequest request = new BankRefundWebhookRequest(
                "external-refund-1",
                "SUCCEEDED",
                null
        );

        when(refundRepository.findByExternalRefundId("external-refund-1")).thenReturn(refund);

        service.handleBankRefundWebhook(request);

        verifyNoInteractions(eventPublisher);
        verify(refundRepository, never()).saveOrUpdate(any());
    }

    private Payment payment(Long id,
                            Long userId,
                            PaymentStatus status,
                            String externalPaymentId) {
        return Payment.load(
                id,
                userId,
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
