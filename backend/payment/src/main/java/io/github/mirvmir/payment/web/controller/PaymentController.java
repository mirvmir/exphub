package io.github.mirvmir.payment.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.payment.application.service.interfaces.PaymentService;
import io.github.mirvmir.payment.web.request.*;
import io.github.mirvmir.payment.web.response.BindCardResponse;
import io.github.mirvmir.payment.web.response.ConfirmPaymentResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @RequiresCompletedProfile
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/bind")
    @ResponseStatus(HttpStatus.CREATED)
    public BindCardResponse bindCard(
            @RequestBody
            BindCardRequest request
    ) {
        return paymentService.bindCard(request);
    }

    @RequiresCompletedProfile
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/{paymentId}/start")
    public ConfirmPaymentResponse startPayment(
            @PathVariable("paymentId")
            Long paymentId,
            @RequestBody(required = false)
            ConfirmPaymentRequest request
    ) {
        Long cardId = request == null
                ? null
                : request.cardId();
        return paymentService.startPayment(paymentId, cardId);
    }

    @PostMapping("/webhook/bank")
    @ResponseStatus(HttpStatus.OK)
    public void handleBankWebhook(
            @RequestBody
            BankPaymentWebhookRequest request
    ) {
        paymentService.handleBankWebhook(request);
    }

    @PostMapping("/webhook/bank/payout")
    @ResponseStatus(HttpStatus.OK)
    public void handleBankPayoutWebhook(
            @RequestBody
            BankPayoutWebhookRequest request
    ) {
        paymentService.handleBankPayoutWebhook(request);
    }

    @PostMapping("/webhook/bank/refund")
    @ResponseStatus(HttpStatus.OK)
    public void handleBankRefundWebhook(
            @RequestBody
            BankRefundWebhookRequest request
    ) {
        paymentService.handleBankRefundWebhook(request);
    }
}