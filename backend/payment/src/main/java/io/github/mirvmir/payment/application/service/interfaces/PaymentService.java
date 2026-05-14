package io.github.mirvmir.payment.application.service.interfaces;

import io.github.mirvmir.payment.web.request.BankPaymentWebhookRequest;
import io.github.mirvmir.payment.web.request.BankPayoutWebhookRequest;
import io.github.mirvmir.payment.web.request.BankRefundWebhookRequest;
import io.github.mirvmir.payment.web.request.BindCardRequest;
import io.github.mirvmir.payment.web.response.BindCardResponse;
import io.github.mirvmir.payment.web.response.ConfirmPaymentResponse;

import java.util.List;

public interface PaymentService {
    BindCardResponse bindCard(BindCardRequest request);
    List<BindCardResponse> getMyCard();
    void setDefaultCard(Long cardId);
    ConfirmPaymentResponse startPayment(Long paymentId, Long cardId);
    void handleBankWebhook(BankPaymentWebhookRequest request);
    void handleBankPayoutWebhook(BankPayoutWebhookRequest request);
    void handleBankRefundWebhook(BankRefundWebhookRequest request);
}
