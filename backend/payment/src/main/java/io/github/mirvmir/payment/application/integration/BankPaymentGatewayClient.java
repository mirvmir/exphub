package io.github.mirvmir.payment.application.integration;

import io.github.mirvmir.payment.application.integration.dto.*;
import io.github.mirvmir.payment.exception.PaymentGatewayUnavailableException;
import io.github.mirvmir.payment.exception.PaymentUnavailableException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Component
public class BankPaymentGatewayClient {

    private final RestTemplate restTemplate;

    private final String bankBaseUrl = "http://localhost:8000";

    public void checkAvailability() {
        try {
            restTemplate.getForEntity(bankBaseUrl + "/health", String.class);
        } catch (RestClientException e) {
            throw new PaymentUnavailableException("Оплата временно недоступна");
        }
    }

    public BankBindCardResponse bindCard(BankBindCardRequest request) {
        try {
            ResponseEntity<BankBindCardResponse> response = restTemplate.postForEntity(
                    bankBaseUrl + "/payment-gateway/cards/bind",
                    request,
                    BankBindCardResponse.class
            );

            BankBindCardResponse body = response.getBody();

            if (body == null) {
                throw new PaymentUnavailableException("Оплата временно недоступна");
            }

            return body;
        } catch (RestClientException e) {
            throw new PaymentUnavailableException("Оплата временно недоступна");
        }
    }

    public BankPayResponse pay(BankPayRequest request) {
        try {
            ResponseEntity<BankPayResponse> response = restTemplate.postForEntity(
                    bankBaseUrl + "/payment-gateway/pay-by-token",
                    request,
                    BankPayResponse.class
            );

            BankPayResponse body = response.getBody();

            if (body == null) {
                throw new PaymentGatewayUnavailableException("Оплата временно недоступна");
            }

            return body;

        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException("Оплата временно недоступна");
        }
    }

    public BankPayoutResponse payout(BankPayoutRequest request) {
        try {
            ResponseEntity<BankPayoutResponse> response =
                    restTemplate.postForEntity(
                            bankBaseUrl + "/payment-gateway/payout-by-token",
                            request,
                            BankPayoutResponse.class
                    );

            BankPayoutResponse body = response.getBody();

            if (body == null) {
                throw new PaymentGatewayUnavailableException(
                        "Вывод средств временно недоступен"
                );
            }

            return body;

        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException(
                    "Вывод средств временно недоступен"
            );
        }
    }

    public BankRefundResponse refund(BankRefundRequest request) {
        try {
            ResponseEntity<BankRefundResponse> response =
                    restTemplate.postForEntity(
                            bankBaseUrl + "/payment-gateway/refund",
                            request,
                            BankRefundResponse.class
                    );

            BankRefundResponse body = response.getBody();

            if (body == null) {
                throw new PaymentGatewayUnavailableException(
                        "Возврат временно недоступен"
                );
            }

            return body;

        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException(
                    "Возврат временно недоступен"
            );
        }
    }
}