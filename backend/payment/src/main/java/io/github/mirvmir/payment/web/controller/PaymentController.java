package io.github.mirvmir.payment.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.payment.application.service.interfaces.PaymentService;
import io.github.mirvmir.payment.web.request.*;
import io.github.mirvmir.payment.web.response.BindCardResponse;
import io.github.mirvmir.payment.web.response.ConfirmPaymentResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @RequiresCompletedProfile
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/card/bind")
    public List<BindCardResponse> getCard() {
        return paymentService.getMyCard();
    }

    @RequiresCompletedProfile
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/card/bind")
    @ResponseStatus(HttpStatus.CREATED)
    public BindCardResponse bindCard(
            @Valid
            @RequestBody
            BindCardRequest request
    ) {
        return paymentService.bindCard(request);
    }

    @RequiresCompletedProfile
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PatchMapping("/card/bind")
    public void setDefaultCard(
            @Valid
            @RequestBody
            DefaultCardRequest request
    ) {
        paymentService.setDefaultCard(request.cardId());
    }

    @RequiresCompletedProfile
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/{paymentId}/start")
    public ConfirmPaymentResponse startPayment(
            @PathVariable("paymentId")
            Long paymentId,
            @Valid
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
            @Valid
            @RequestBody
            BankPaymentWebhookRequest request
    ) {
        paymentService.handleBankWebhook(request);
    }

    @PostMapping("/webhook/bank/payout")
    @ResponseStatus(HttpStatus.OK)
    public void handleBankPayoutWebhook(
            @Valid
            @RequestBody
            BankPayoutWebhookRequest request
    ) {
        paymentService.handleBankPayoutWebhook(request);
    }

    @PostMapping("/webhook/bank/refund")
    @ResponseStatus(HttpStatus.OK)
    public void handleBankRefundWebhook(
            @Valid
            @RequestBody
            BankRefundWebhookRequest request
    ) {
        paymentService.handleBankRefundWebhook(request);
    }
}