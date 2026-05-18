package io.github.mirvmir.payment.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.payment.api.dto.*;
import io.github.mirvmir.payment.application.properties.BankProperties;
import io.github.mirvmir.payment.application.service.port.repository.PaymentRepository;
import io.github.mirvmir.payment.application.service.port.repository.PayoutRepository;
import io.github.mirvmir.payment.application.service.port.repository.RefundRepository;
import io.github.mirvmir.payment.application.service.port.repository.UserCardRepository;
import io.github.mirvmir.payment.domain.*;
import io.github.mirvmir.payment.exception.PaymentErrorCode;
import io.github.mirvmir.payment.exception.PaymentException;
import io.github.mirvmir.payment.application.integration.BankPaymentGatewayClient;
import io.github.mirvmir.payment.application.integration.dto.BankPayoutRequest;
import io.github.mirvmir.payment.application.integration.dto.BankPayoutResponse;
import io.github.mirvmir.payment.application.integration.dto.BankRefundRequest;
import io.github.mirvmir.payment.application.integration.dto.BankRefundResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Service
public class DefaultPaymentApi implements PaymentApi {

    private final UserCardRepository userCardRepository;
    private final PaymentRepository paymentRepository;
    private final PayoutRepository payoutRepository;
    private final RefundRepository refundRepository;

    private final BankPaymentGatewayClient bankPaymentGatewayClient;

    private final BankProperties bankProperties;

    private final Clock clock;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        Instant now = Instant.now(clock);
        Payment payment = Payment.create(
                request.userId(),
                request.amount(),
                request.currency(),
                request.description(),
                request.orderId(),
                now
        );

        Payment savedPayment = paymentRepository.save(payment);

        return new CreatePaymentResponse(
                savedPayment.getId(),
                savedPayment.getStatus().name()
        );
    }

    @Override
    @Transactional
    public CreatePayoutResponse createPayout(CreatePayoutRequest request) {
        Instant now = Instant.now(clock);

        UserCard card = request.cardId() != null
                ? userCardRepository.findById(request.cardId())
                : userCardRepository.findDefaultByUserId(request.userId());

        if (card == null
                || !card.getUserId().equals(request.userId())
                || !card.isActive()) {
            throw new NotFoundException(PaymentErrorCode.CARD_NOT_FOUND);
        }

        Payout payout = Payout.create(
                request.userId(),
                card.getId(),
                request.amount(),
                request.currency(),
                request.description(),
                request.walletWithdrawalId(),
                now
        );

        Payout savedPayout = payoutRepository.saveOrUpdate(payout);

        BankPayoutResponse bankResponse = bankPaymentGatewayClient.payout(
                new BankPayoutRequest(
                        card.getCardToken(),
                        savedPayout.getPrice().getAmount(),
                        savedPayout.getPrice().getCurrency(),
                        String.valueOf(savedPayout.getId()),
                        savedPayout.getDescription(),
                        bankProperties.getPayoutWebhookUrl()
                )
        );

        if ("PROCESSING".equals(bankResponse.status())) {
            savedPayout.markProcessing(bankResponse.externalPayoutId());
        } else if ("FAILED".equals(bankResponse.status())) {
            savedPayout.markProcessing(bankResponse.externalPayoutId());
            savedPayout.markFailedFromWebhook();
        } else {
            throw new PaymentException("Некорректный статус вывода от банка");
        }

        savedPayout = payoutRepository.saveOrUpdate(savedPayout);

        return new CreatePayoutResponse(
                savedPayout.getId(),
                savedPayout.getStatus().name()
        );
    }

    @Override
    @Transactional
    public CreateRefundResponse refundPayment(CreateRefundRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.orderId());

        if (payment == null) {
            throw new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        if (PaymentStatus.SUCCEEDED != payment.getStatus()) {
            throw new PaymentException("Возврат возможен только по успешному платежу");
        }

        if (payment.getExternalPaymentId() == null) {
            throw new PaymentException("У платежа отсутствует внешний идентификатор банка");
        }

        if (!payment.getPrice().getCurrency().equals(request.currency())) {
            throw new PaymentException("Валюта возврата не совпадает с валютой платежа");
        }

        if (request.amount() == null
                || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Сумма возврата должна быть больше 0");
        }

        if (request.amount().compareTo(payment.getPrice().getAmount()) > 0) {
            throw new PaymentException("Сумма возврата больше суммы платежа");
        }

        boolean refundAlreadyExists = refundRepository.existsByPaymentIdAndStatusIn(
                payment.getId(),
                List.of(
                        RefundStatus.CREATED,
                        RefundStatus.PROCESSING,
                        RefundStatus.SUCCEEDED
                )
        );

        if (refundAlreadyExists) {
            throw new PaymentException("Возврат по платежу уже создан");
        }

        Instant now = Instant.now(clock);

        Refund refund = Refund.create(
                payment.getId(),
                request.amount(),
                request.currency(),
                request.reason(),
                now
        );

        Refund savedRefund = refundRepository.saveOrUpdate(refund);

        BankRefundResponse bankResponse = bankPaymentGatewayClient.refund(
                new BankRefundRequest(
                        payment.getExternalPaymentId(),
                        String.valueOf(savedRefund.getId()),
                        savedRefund.getPrice().getAmount(),
                        savedRefund.getPrice().getCurrency(),
                        savedRefund.getReason(),
                        bankProperties.getRefundWebhookUrl()
                )
        );

        if ("PROCESSING".equals(bankResponse.status())) {
            savedRefund.markProcessing(bankResponse.externalRefundId());
        } else if ("FAILED".equals(bankResponse.status())) {
            savedRefund.markProcessing(bankResponse.externalRefundId());
            savedRefund.markFailedFromWebhook();
        } else {
            throw new PaymentException("Некорректный статус возврата от банка");
        }

        savedRefund = refundRepository.saveOrUpdate(savedRefund);

        return new CreateRefundResponse(
                savedRefund.getId(),
                payment.getId(),
                savedRefund.getStatus().name()
        );
    }
}
