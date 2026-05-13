package io.github.mirvmir.payment.api;

import io.github.mirvmir.payment.api.dto.*;

public interface PaymentApi {
    CreatePaymentResponse createPayment(CreatePaymentRequest request);
    CreatePayoutResponse createPayout(CreatePayoutRequest request);
    CreateRefundResponse refundPayment(CreateRefundRequest request);
}
