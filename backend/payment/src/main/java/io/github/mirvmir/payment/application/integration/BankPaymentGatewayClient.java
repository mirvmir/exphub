package io.github.mirvmir.payment.application.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mirvmir.payment.application.integration.dto.*;
import io.github.mirvmir.payment.application.properties.BankProperties;
import io.github.mirvmir.payment.exception.PaymentException;
import io.github.mirvmir.payment.exception.PaymentGatewayUnavailableException;
import io.github.mirvmir.payment.exception.PaymentUnavailableException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
@Component
public class BankPaymentGatewayClient {

    private final RestTemplate restTemplate;
    private final BankProperties bankProperties;
    private final ObjectMapper objectMapper;

    public void checkAvailability() {
        try {
            String bankBaseUrl = bankProperties.getBankUrl();
            restTemplate.getForEntity(bankBaseUrl + "/health", String.class);
        } catch (RestClientException e) {
            throw new PaymentUnavailableException("Оплата временно недоступна");
        }
    }

    public BankBindCardResponse bindCard(BankBindCardRequest request) {
        try {
            String bankBaseUrl = bankProperties.getBankUrl();

            ResponseEntity<BankBindCardResponse> response =
                    restTemplate.postForEntity(
                            bankBaseUrl + "/payment-gateway/cards/bind",
                            request,
                            BankBindCardResponse.class
                    );

            BankBindCardResponse body = response.getBody();

            if (body == null) {
                throw new PaymentUnavailableException("Оплата временно недоступна");
            }

            return body;

        } catch (HttpClientErrorException e) {
            try {
                BankErrorResponse error =
                        objectMapper.readValue(
                                e.getResponseBodyAsString(),
                                BankErrorResponse.class
                        );

                throw new PaymentException(
                        error.detail()
                );

            } catch (JsonProcessingException ex) {
                throw new PaymentException(
                        "Card bind error"
                );
            }

        } catch (RestClientException e) {
            throw new PaymentUnavailableException("Оплата временно недоступна");
        }
    }

    public BankPayResponse pay(BankPayRequest request) {
        try {
            String bankBaseUrl = bankProperties.getBankUrl();

            ResponseEntity<BankPayResponse> response =
                    restTemplate.postForEntity(
                            bankBaseUrl + "/payment-gateway/pay-by-token",
                            request,
                            BankPayResponse.class
                    );

            BankPayResponse body = response.getBody();

            if (body == null) {
                throw new PaymentGatewayUnavailableException("Оплата временно недоступна");
            }

            return body;

        } catch (HttpClientErrorException e) {
            try {
                BankErrorResponse error =
                        objectMapper.readValue(
                                e.getResponseBodyAsString(),
                                BankErrorResponse.class
                        );

                throw new PaymentException(
                        error.detail()
                );

            } catch (JsonProcessingException ex) {
                throw new PaymentException(
                        "Payment error"
                );
            }

        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException("Оплата временно недоступна");
        }
    }

    public BankPayoutResponse payout(BankPayoutRequest request) {
        try {
            String bankBaseUrl = bankProperties.getBankUrl();

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

        } catch (HttpClientErrorException e) {
            try {
                BankErrorResponse error =
                        objectMapper.readValue(
                                e.getResponseBodyAsString(),
                                BankErrorResponse.class
                        );

                throw new PaymentException(
                        error.detail()
                );

            } catch (JsonProcessingException ex) {
                throw new PaymentException(
                        "Payout error"
                );
            }

        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException(
                    "Вывод средств временно недоступен"
            );
        }
    }

    public BankRefundResponse refund(BankRefundRequest request) {
        try {
            String bankBaseUrl = bankProperties.getBankUrl();

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

        } catch (HttpClientErrorException e) {
            try {
                BankErrorResponse error =
                        objectMapper.readValue(
                                e.getResponseBodyAsString(),
                                BankErrorResponse.class
                        );

                throw new PaymentException(
                        error.detail()
                );

            } catch (JsonProcessingException ex) {
                throw new PaymentException(
                        "Refund error"
                );
            }

        } catch (RestClientException e) {
            throw new PaymentGatewayUnavailableException(
                    "Возврат временно недоступен"
            );
        }
    }
}