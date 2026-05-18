package io.github.mirvmir.payment.application.service.implementation;

import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.event.PaymentRefundedEvent;
import io.github.mirvmir.payment.api.event.PayoutSucceededEvent;
import io.github.mirvmir.payment.application.properties.BankProperties;
import io.github.mirvmir.payment.application.service.port.event.PaymentEventPublisher;
import io.github.mirvmir.payment.application.service.port.repository.PayoutRepository;
import io.github.mirvmir.payment.application.service.port.repository.RefundRepository;
import io.github.mirvmir.payment.application.service.interfaces.PaymentService;
import io.github.mirvmir.payment.application.service.port.repository.PaymentRepository;
import io.github.mirvmir.payment.application.service.port.repository.UserCardRepository;
import io.github.mirvmir.payment.domain.*;
import io.github.mirvmir.payment.api.event.PaymentSucceededEvent;
import io.github.mirvmir.payment.exception.PaymentErrorCode;
import io.github.mirvmir.payment.exception.PaymentException;
import io.github.mirvmir.payment.application.integration.dto.BankPayRequest;
import io.github.mirvmir.payment.application.integration.dto.BankPayResponse;
import io.github.mirvmir.payment.application.integration.dto.BankBindCardResponse;
import io.github.mirvmir.payment.application.integration.dto.BankBindCardRequest;
import io.github.mirvmir.payment.exception.CardBindingException;
import io.github.mirvmir.payment.application.integration.BankPaymentGatewayClient;
import io.github.mirvmir.payment.web.request.BankPaymentWebhookRequest;
import io.github.mirvmir.payment.web.request.BankPayoutWebhookRequest;
import io.github.mirvmir.payment.web.request.BankRefundWebhookRequest;
import io.github.mirvmir.payment.web.request.BindCardRequest;
import io.github.mirvmir.payment.web.response.BindCardResponse;
import io.github.mirvmir.payment.web.response.ConfirmPaymentResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class DefaultPaymentService implements PaymentService {

    private static final BigDecimal CARD_VERIFICATION_AMOUNT = BigDecimal.ONE;

    private final EnrollmentApi enrollmentApi;
    private final IdentityApi identityApi;
    private final BankProperties bankProperties;

    private final BankPaymentGatewayClient bankPaymentGatewayClient;

    private final UserCardRepository userCardRepository;
    private final PaymentRepository paymentRepository;
    private final PayoutRepository payoutRepository;
    private final RefundRepository refundRepository;

    private final PaymentEventPublisher eventPublisher;

    private final Clock clock;

    @Override
    @Transactional
    public BindCardResponse bindCard(BindCardRequest request) {
        bankPaymentGatewayClient.checkAvailability();

        BankBindCardRequest bankRequest = new BankBindCardRequest(
                request.cardNumber(),
                request.cardHolder(),
                request.expiryMonth(),
                request.expiryYear(),
                request.cvc(),
                CARD_VERIFICATION_AMOUNT,
                UUID.randomUUID().toString()
        );

        BankBindCardResponse bankResponse =
                bankPaymentGatewayClient.bindCard(bankRequest);

        if (!"SUCCEEDED".equals(bankResponse.status())) {
            throw new CardBindingException("Не удалось подключить карту");
        }

        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        if (userCardRepository.existsByUserIdAndCardToken(
                currentUserId,
                bankResponse.cardToken()
        )) {
            throw new CardBindingException("Карта уже подключена");
        }

        boolean isFirstCard = !userCardRepository.existsByUserId(
                currentUserId
        );

        UserCard card = UserCard.createBoundCard(
                currentUserId,
                bankResponse.bankCardId(),
                bankResponse.cardToken(),
                bankResponse.maskedPan(),
                bankResponse.last4(),
                bankResponse.paymentSystem(),
                isFirstCard,
                Instant.now(clock)
        );

        UserCard savedCard = userCardRepository.save(card);

        return new BindCardResponse(
                savedCard.getId(),
                savedCard.getMaskedPan(),
                savedCard.getPaymentSystem()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BindCardResponse> getMyCard() {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        List<UserCard> cards = userCardRepository.findByUserId(currentUserId);

        return cards.stream()
                .map(card ->
                        new BindCardResponse(
                                card.getId(),
                                card.getMaskedPan(),
                                card.getPaymentSystem()
                        )
                ).toList();
    }

    @Override
    @Transactional
    public void setDefaultCard(Long cardId) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        UserCard newDefaultCard = userCardRepository.findById(cardId);

        if (newDefaultCard == null) {
            throw new NotFoundException(PaymentErrorCode.CARD_NOT_FOUND);
        }

        if (!currentUserId.equals(newDefaultCard.getUserId())) {
            throw new ForbiddenException(PaymentErrorCode.CARD_FORBIDDEN);
        }

        if (newDefaultCard.isDefaultCard()) {
            return;
        }

        UserCard oldDefaultCard =
                userCardRepository.findDefaultByUserId(currentUserId);

        if (oldDefaultCard != null) {
            oldDefaultCard.unmarkAsDefault();
            userCardRepository.save(oldDefaultCard);
        }

        newDefaultCard.markAsDefault();
        userCardRepository.save(newDefaultCard);
    }

    @Override
    @Transactional
    public ConfirmPaymentResponse startPayment(Long paymentId, Long cardId) {
        Payment payment = paymentRepository.findById(paymentId);

        if (payment == null) {
            throw new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        if (PaymentStatus.SUCCEEDED == payment.getStatus()
                || PaymentStatus.PROCESSING == payment.getStatus()) {
            return new ConfirmPaymentResponse(
                    payment.getId(),
                    payment.getStatus().name()
            );
        }

        if (PaymentStatus.CANCELLED == payment.getStatus()) {
            throw new PaymentException("Платёж отменён");
        }

        if (PaymentStatus.CREATED != payment.getStatus()) {
            throw new PaymentException("Платёж уже обработан");
        }

        Instant now = Instant.now(clock);

        if (enrollmentApi.isOrderCancelled(payment.getOrderId())) {
            payment.cancel();
            paymentRepository.save(payment);

            throw new PaymentException("Заказ был отменён");
        }

        if (enrollmentApi.isOrderExpired(payment.getOrderId(), now)) {
            enrollmentApi.expireOrder(payment.getOrderId(), now);
            payment.expire();

            paymentRepository.save(payment);

            throw new PaymentException("Время оплаты истекло");
        }

        enrollmentApi.markPaymentProcessing(payment.getOrderId(), now);

        UserCard card = cardId != null
                ? userCardRepository.findById(cardId)
                : userCardRepository.findDefaultByUserId(payment.getUserId());

        if (card == null
                || !card.getUserId().equals(payment.getUserId())
                || !card.isActive()) {
            throw new NotFoundException(PaymentErrorCode.CARD_NOT_FOUND);
        }

        bankPaymentGatewayClient.checkAvailability();

        BankPayResponse bankResponse = bankPaymentGatewayClient.pay(
                new BankPayRequest(
                        card.getCardToken(),
                        payment.getPrice().getAmount(),
                        payment.getPrice().getCurrency(),
                        String.valueOf(payment.getOrderId()),
                        payment.getDescription(),
                        bankProperties.getPaymentWebhookUrl()
                )
        );

        if ("PROCESSING".equals(bankResponse.status())) {
            payment.markProcessing(bankResponse.paymentId());
        } else if ("FAILED".equals(bankResponse.status())) {
            payment.markFailed(bankResponse.paymentId());
        } else {
            throw new PaymentException("Некорректный статус платежа от банка");
        }

        paymentRepository.save(payment);

        return new ConfirmPaymentResponse(
                payment.getId(),
                payment.getStatus().name()
        );
    }

    @Override
    @Transactional
    public void handleBankWebhook(BankPaymentWebhookRequest request) {
        Payment payment = paymentRepository.findByExternalPaymentId(
                request.externalPaymentId()
        );

        if (payment == null) {
            throw new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        PaymentStatus bankStatus = PaymentStatus.valueOf(request.status());

        if (PaymentStatus.SUCCEEDED == bankStatus) {
            payment.markSucceededFromWebhook(
                    Instant.now(clock)
            );

            eventPublisher.publishPaymentSucceeded(
                    new PaymentSucceededEvent(
                            payment.getId(),
                            payment.getOrderId(),
                            payment.getUserId(),
                            payment.getPrice().getAmount(),
                            payment.getPrice().getCurrency(),
                            payment.getPaidAt()
                    )
            );
        } else if (PaymentStatus.FAILED == bankStatus) {
            payment.markFailedFromWebhook();
        } else {
            throw new PaymentException("Некорректный статус платежа от банка");
        }

        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void handleBankPayoutWebhook(BankPayoutWebhookRequest request) {
        Payout payout = payoutRepository.findByExternalPayoutId(
                request.externalPayoutId()
        );

        if (payout == null) {
            throw new NotFoundException(PaymentErrorCode.PAYOUT_NOT_FOUND);
        }

        PayoutStatus bankStatus = PayoutStatus.valueOf(request.status());

        if (PayoutStatus.SUCCEEDED == bankStatus) {
            payout.markSucceededFromWebhook(
                    Instant.now(clock)
            );

            eventPublisher.publishPayoutSucceeded(
                    new PayoutSucceededEvent(
                            payout.getId(),
                            payout.getWalletWithdrawalId(),
                            payout.getUserId(),
                            payout.getPrice().getAmount(),
                            payout.getPrice().getCurrency(),
                            payout.getPaidAt()
                    )
            );
        } else if (PayoutStatus.FAILED == bankStatus) {
            payout.markFailedFromWebhook();
        } else {
            throw new PaymentException("Некорректный статус вывода от банка");
        }

        payoutRepository.saveOrUpdate(payout);
    }

    @Override
    @Transactional
    public void handleBankRefundWebhook(BankRefundWebhookRequest request) {
        Refund refund = refundRepository.findByExternalRefundId(
                request.externalRefundId()
        );

        if (refund == null) {
            throw new NotFoundException(PaymentErrorCode.REFUND_NOT_FOUND);
        }
        if (refund.isSucceeded()) {
            return;
        }

        RefundStatus bankStatus = RefundStatus.valueOf(request.status());
        Instant now = Instant.now(clock);

        if (RefundStatus.SUCCEEDED == bankStatus) {
            refund.markSucceededFromWebhook(now);

            Payment payment = paymentRepository.findById(
                    refund.getPaymentId()
            );

            if (payment == null) {
                throw new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND);
            }

            eventPublisher.publishPaymentRefunded(
                    new PaymentRefundedEvent(
                            refund.getId(),
                            payment.getId(),
                            payment.getOrderId(),
                            payment.getUserId(),
                            refund.getPrice().getAmount(),
                            refund.getPrice().getCurrency(),
                            refund.getRefundedAt()
                    )
            );
        } else if (RefundStatus.FAILED == bankStatus) {
            refund.markFailedFromWebhook();
        } else {
            throw new PaymentException("Некорректный статус возврата от банка");
        }

        refundRepository.saveOrUpdate(refund);
    }
}