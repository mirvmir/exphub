package io.github.mirvmir.payment.application.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BankProperties {

    @Value("${bank.base.url}")
    private String bankUrl;

    @Value("${bank.public.base.url}")
    private String publicBaseUrl;

    public String getPaymentWebhookUrl() {
        return publicBaseUrl + "/payments/webhook/bank";
    }

    public String getPayoutWebhookUrl() {
        return publicBaseUrl + "/payments/webhook/bank/payout";
    }

    public String getRefundWebhookUrl() {
        return publicBaseUrl + "/payments/webhook/bank/refund";
    }
}