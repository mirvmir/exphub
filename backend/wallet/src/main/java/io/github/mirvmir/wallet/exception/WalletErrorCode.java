package io.github.mirvmir.wallet.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum WalletErrorCode implements ErrorCode {
    WALLET_NOT_FOUND("WALLET_NOT_FOUND",
            "Wallet not found"),
    WALLET_WITHDRAWAL_NOT_FOUND("WALLET_WITHDRAWAL_NOT_FOUND",
            "Wallet withdrawal not found");

    private final String code;
    private final String message;

    WalletErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
