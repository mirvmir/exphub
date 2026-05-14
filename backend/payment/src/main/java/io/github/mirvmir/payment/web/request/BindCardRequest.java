package io.github.mirvmir.payment.web.request;

import jakarta.validation.constraints.*;

public record BindCardRequest(
        @NotBlank
        @Pattern(regexp = "^\\d{13,19}$")
        String cardNumber,
        @NotBlank
        @Size(max = 100)
        String cardHolder,
        @NotBlank
        @Pattern(regexp = "^(0[1-9]|1[0-2])$")
        String expiryMonth,
        @NotBlank
        @Pattern(regexp = "^\\d{2}$")
        String expiryYear,
        @NotBlank
        @Pattern(regexp = "^\\d{3}$")
        String cvc
) {
}