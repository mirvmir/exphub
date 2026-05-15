package io.github.mirvmir.wallet.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.payment.api.dto.CreatePayoutRequest;
import io.github.mirvmir.wallet.application.service.port.repository.WalletRepository;
import io.github.mirvmir.wallet.application.service.port.repository.WalletTransactionRepository;
import io.github.mirvmir.wallet.application.service.port.repository.WalletWithdrawalRepository;
import io.github.mirvmir.wallet.application.service.interfaces.WalletService;
import io.github.mirvmir.wallet.domain.Wallet;
import io.github.mirvmir.wallet.domain.WalletTransaction;
import io.github.mirvmir.wallet.domain.WalletTransactionType;
import io.github.mirvmir.wallet.domain.WalletWithdrawal;
import io.github.mirvmir.wallet.exception.WalletErrorCode;
import io.github.mirvmir.wallet.web.request.WithdrawWalletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;

@Service
@AllArgsConstructor
public class DefaultWalletService implements WalletService {

    private final EnrollmentApi enrollmentApi;
    private final IdentityApi identityApi;
    private final PaymentApi paymentApi;

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletWithdrawalRepository walletWithdrawalRepository;

    private final Clock clock;

    @Override
    @Transactional
    public void accrueForPayment(Long paymentId,
                                 Long orderId,
                                 Currency currency,
                                 BigDecimal amount) {
        if (walletTransactionRepository.existsByPaymentId(paymentId, WalletTransactionType.TEACHER_REWARD)) {
            return;
        }

        Long teacherId = enrollmentApi.getTeacherIdByOrderId(orderId);
        Wallet wallet = walletRepository.findByUserId(teacherId);

        Instant now = Instant.now(clock);
        if (wallet == null) {
            wallet = Wallet.create(
                    teacherId,
                    currency,
                    now
            );

            wallet = walletRepository.saveOrUpdate(wallet);
        }

        BigDecimal accrualAmount = wallet.calculateTeacherAccrualAmount(amount);
        wallet.accrue(
                accrualAmount,
                currency,
                now
        );
        WalletTransaction transaction = WalletTransaction.createPayment(
                wallet.getId(),
                paymentId,
                accrualAmount,
                currency,
                WalletTransactionType.TEACHER_REWARD,
                now
        );

        walletRepository.saveOrUpdate(wallet);
        walletTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void withdrawToCard(WithdrawWalletRequest request) {
        Long teacherId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (teacherId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        Wallet wallet = walletRepository.findByUserId(teacherId);

        if (wallet == null) {
            throw new NotFoundException(
                    WalletErrorCode.WALLET_NOT_FOUND,
                    "Wallet for teacher id=" + teacherId + " not found"
            );
        }

        WalletWithdrawal withdrawal = WalletWithdrawal.create(
                teacherId,
                wallet.getId(),
                request.amount(),
                request.currency(),
                now
        );

        withdrawal = walletWithdrawalRepository.save(withdrawal);

        paymentApi.createPayout(
                new CreatePayoutRequest(
                        teacherId,
                        request.cardId(),
                        request.amount(),
                        request.currency(),
                        "Вывод средств",
                        withdrawal.getId()
                )
        );
    }

    @Override
    @Transactional
    public void completeWithdrawal(Long payoutId,
                                   Long walletWithdrawalId,
                                   Currency currency,
                                   BigDecimal amount,
                                   Instant paidAt) {
        if (walletTransactionRepository.existsByWalletWithdrawalId(
                walletWithdrawalId,
                WalletTransactionType.WITHDRAW_TO_CARD
        )) {
            return;
        }

        WalletWithdrawal withdrawal =
                walletWithdrawalRepository.findById(walletWithdrawalId);

        if (withdrawal == null) {
            throw new NotFoundException(WalletErrorCode.WALLET_WITHDRAWAL_NOT_FOUND);
        }

        Wallet wallet = walletRepository.findByUserId(withdrawal.getUserId());

        wallet.withdraw(
                withdrawal.getPrice().getAmount(),
                withdrawal.getPrice().getCurrency(),
                paidAt
        );

        WalletTransaction transaction = WalletTransaction.createWithdrawal(
                wallet.getId(),
                withdrawal.getId(),
                withdrawal.getPrice().getAmount(),
                withdrawal.getPrice().getCurrency(),
                WalletTransactionType.WITHDRAW_TO_CARD,
                paidAt
        );

        withdrawal.markSucceeded(paidAt);

        walletRepository.saveOrUpdate(wallet);
        walletWithdrawalRepository.save(withdrawal);
        walletTransactionRepository.save(transaction);
    }
}