package io.github.mirvmir.wallet.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.payment.api.dto.CreatePayoutRequest;
import io.github.mirvmir.wallet.application.service.port.repository.WalletRepository;
import io.github.mirvmir.wallet.application.service.port.repository.WalletTransactionRepository;
import io.github.mirvmir.wallet.application.service.port.repository.WalletWithdrawalRepository;
import io.github.mirvmir.wallet.domain.*;
import io.github.mirvmir.wallet.web.request.WithdrawWalletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultWalletServiceTest {

    private EnrollmentApi enrollmentApi;
    private IdentityApi identityApi;
    private PaymentApi paymentApi;
    private WalletRepository walletRepository;
    private WalletTransactionRepository walletTransactionRepository;
    private WalletWithdrawalRepository walletWithdrawalRepository;
    private DefaultWalletService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");
    private final Currency rub = Currency.getInstance("RUB");

    @BeforeEach
    void setUp() {
        enrollmentApi = mock(EnrollmentApi.class);
        identityApi = mock(IdentityApi.class);
        paymentApi = mock(PaymentApi.class);
        walletRepository = mock(WalletRepository.class);
        walletTransactionRepository = mock(WalletTransactionRepository.class);
        walletWithdrawalRepository = mock(WalletWithdrawalRepository.class);

        Clock clock = Clock.fixed(now, ZoneOffset.UTC);

        service = new DefaultWalletService(
                enrollmentApi,
                identityApi,
                paymentApi,
                walletRepository,
                walletTransactionRepository,
                walletWithdrawalRepository,
                clock
        );
    }

    @Test
    void accrueForPayment_shouldDoNothing_whenTransactionAlreadyExists() {
        when(walletTransactionRepository.existsByPaymentId(
                1L,
                WalletTransactionType.TEACHER_REWARD
        )).thenReturn(true);

        service.accrueForPayment(
                1L,
                100L,
                rub,
                new BigDecimal("1000")
        );

        verify(walletTransactionRepository).existsByPaymentId(
                1L,
                WalletTransactionType.TEACHER_REWARD
        );
        verifyNoInteractions(enrollmentApi);
        verifyNoInteractions(walletRepository);
    }

    @Test
    void accrueForPayment_shouldCreateWalletAndAccrueTeacherReward() {
        when(walletTransactionRepository.existsByPaymentId(
                1L,
                WalletTransactionType.TEACHER_REWARD
        )).thenReturn(false);

        when(enrollmentApi.getTeacherIdByOrderId(100L))
                .thenReturn(2L);

        when(walletRepository.findByUserId(2L))
                .thenReturn(null);

        when(walletRepository.saveOrUpdate(any(Wallet.class)))
                .thenAnswer(invocation -> {
                    Wallet wallet = invocation.getArgument(0);

                    if (wallet.getId() == null) {
                        return Wallet.load(
                                10L,
                                wallet.getTeacherId(),
                                wallet.getBalance(),
                                wallet.getCurrency(),
                                wallet.getCreatedAt(),
                                wallet.getUpdatedAt()
                        );
                    }

                    return wallet;
                });

        service.accrueForPayment(
                1L,
                100L,
                rub,
                new BigDecimal("1000")
        );

        ArgumentCaptor<Wallet> walletCaptor =
                ArgumentCaptor.forClass(Wallet.class);

        verify(walletRepository, times(2))
                .saveOrUpdate(walletCaptor.capture());

        Wallet accruedWallet = walletCaptor.getAllValues().get(1);

        assertEquals(10L, accruedWallet.getId());
        assertEquals(2L, accruedWallet.getTeacherId());
        assertEquals(new BigDecimal("900.00"), accruedWallet.getBalance());
        assertEquals(rub, accruedWallet.getCurrency());

        ArgumentCaptor<WalletTransaction> transactionCaptor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletTransactionRepository).save(transactionCaptor.capture());

        WalletTransaction transaction = transactionCaptor.getValue();

        assertEquals(10L, transaction.getWalletId());
        assertEquals(1L, transaction.getPaymentId());
        assertNull(transaction.getWalletWithdrawalId());
        assertEquals(new BigDecimal("900.00"), transaction.getPrice().getAmount());
        assertEquals(rub, transaction.getPrice().getCurrency());
        assertEquals(WalletTransactionType.TEACHER_REWARD, transaction.getType());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void accrueForPayment_shouldUseExistingWallet() {
        Wallet wallet = Wallet.load(
                10L,
                2L,
                new BigDecimal("100"),
                rub,
                now.minusSeconds(100),
                now.minusSeconds(100)
        );

        when(walletTransactionRepository.existsByPaymentId(
                1L,
                WalletTransactionType.TEACHER_REWARD
        )).thenReturn(false);

        when(enrollmentApi.getTeacherIdByOrderId(100L))
                .thenReturn(2L);

        when(walletRepository.findByUserId(2L))
                .thenReturn(wallet);

        service.accrueForPayment(
                1L,
                100L,
                rub,
                new BigDecimal("1000")
        );

        assertEquals(new BigDecimal("1000.00"), wallet.getBalance());

        verify(walletRepository, times(1)).saveOrUpdate(wallet);
        verify(walletTransactionRepository).save(any(WalletTransaction.class));
    }

    @Test
    void withdrawToCard_shouldCreateWithdrawalAndCreatePayout() {
        Wallet wallet = Wallet.load(
                10L,
                2L,
                new BigDecimal("5000"),
                rub,
                now,
                now
        );

        WithdrawWalletRequest request = new WithdrawWalletRequest(
                100L,
                new BigDecimal("1500"),
                rub
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(walletRepository.findByUserId(2L)).thenReturn(wallet);

        when(walletWithdrawalRepository.save(any(WalletWithdrawal.class)))
                .thenAnswer(invocation -> {
                    WalletWithdrawal withdrawal = invocation.getArgument(0);

                    return WalletWithdrawal.load(
                            20L,
                            withdrawal.getUserId(),
                            withdrawal.getWalletId(),
                            withdrawal.getPrice().getAmount(),
                            withdrawal.getPrice().getCurrency(),
                            withdrawal.getStatus(),
                            withdrawal.getCreatedAt(),
                            withdrawal.getCompletedAt()
                    );
                });

        service.withdrawToCard(request);

        ArgumentCaptor<WalletWithdrawal> withdrawalCaptor =
                ArgumentCaptor.forClass(WalletWithdrawal.class);

        verify(walletWithdrawalRepository).save(withdrawalCaptor.capture());

        WalletWithdrawal withdrawal = withdrawalCaptor.getValue();

        assertEquals(2L, withdrawal.getUserId());
        assertEquals(10L, withdrawal.getWalletId());
        assertEquals(new BigDecimal("1500"), withdrawal.getPrice().getAmount());
        assertEquals(rub, withdrawal.getPrice().getCurrency());
        assertEquals(WalletWithdrawalStatus.CREATED, withdrawal.getStatus());

        ArgumentCaptor<CreatePayoutRequest> payoutCaptor =
                ArgumentCaptor.forClass(CreatePayoutRequest.class);

        verify(paymentApi).createPayout(payoutCaptor.capture());

        CreatePayoutRequest payoutRequest = payoutCaptor.getValue();

        assertEquals(2L, payoutRequest.userId());
        assertEquals(100L, payoutRequest.cardId());
        assertEquals(new BigDecimal("1500"), payoutRequest.amount());
        assertEquals(rub, payoutRequest.currency());
        assertEquals("Вывод средств", payoutRequest.description());
        assertEquals(20L, payoutRequest.walletWithdrawalId());
    }

    @Test
    void withdrawToCard_shouldThrowNotFound_whenWalletNotFound() {
        WithdrawWalletRequest request = new WithdrawWalletRequest(
                100L,
                new BigDecimal("1500"),
                rub
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(walletRepository.findByUserId(2L)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.withdrawToCard(request)
        );

        assertEquals("Wallet for teacher id=2 not found", exception.getMessage());

        verifyNoInteractions(walletWithdrawalRepository);
        verifyNoInteractions(paymentApi);
    }

    @Test
    void completeWithdrawal_shouldDoNothing_whenWithdrawalTransactionAlreadyExists() {
        when(walletTransactionRepository.existsByWalletWithdrawalId(
                20L,
                WalletTransactionType.WITHDRAW_TO_CARD
        )).thenReturn(true);

        service.completeWithdrawal(
                30L,
                20L,
                rub,
                new BigDecimal("1500"),
                now
        );

        verify(walletTransactionRepository).existsByWalletWithdrawalId(
                20L,
                WalletTransactionType.WITHDRAW_TO_CARD
        );
        verifyNoInteractions(walletWithdrawalRepository);
        verifyNoInteractions(walletRepository);
    }

    @Test
    void completeWithdrawal_shouldWithdrawMoneyAndMarkWithdrawalSucceeded() {
        WalletWithdrawal withdrawal = WalletWithdrawal.load(
                20L,
                2L,
                10L,
                new BigDecimal("1500"),
                rub,
                WalletWithdrawalStatus.CREATED,
                now.minusSeconds(100),
                null
        );

        Wallet wallet = Wallet.load(
                10L,
                2L,
                new BigDecimal("5000"),
                rub,
                now.minusSeconds(100),
                now.minusSeconds(100)
        );

        when(walletTransactionRepository.existsByWalletWithdrawalId(
                20L,
                WalletTransactionType.WITHDRAW_TO_CARD
        )).thenReturn(false);

        when(walletWithdrawalRepository.findById(20L))
                .thenReturn(withdrawal);

        when(walletRepository.findByUserId(2L))
                .thenReturn(wallet);

        service.completeWithdrawal(
                30L,
                20L,
                rub,
                new BigDecimal("1500"),
                now
        );

        assertEquals(new BigDecimal("3500"), wallet.getBalance());
        assertEquals(WalletWithdrawalStatus.SUCCEEDED, withdrawal.getStatus());
        assertEquals(now, withdrawal.getCompletedAt());

        verify(walletRepository).saveOrUpdate(wallet);
        verify(walletWithdrawalRepository).save(withdrawal);

        ArgumentCaptor<WalletTransaction> transactionCaptor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(walletTransactionRepository).save(transactionCaptor.capture());

        WalletTransaction transaction = transactionCaptor.getValue();

        assertEquals(10L, transaction.getWalletId());
        assertNull(transaction.getPaymentId());
        assertEquals(20L, transaction.getWalletWithdrawalId());
        assertEquals(new BigDecimal("1500"), transaction.getPrice().getAmount());
        assertEquals(rub, transaction.getPrice().getCurrency());
        assertEquals(WalletTransactionType.WITHDRAW_TO_CARD, transaction.getType());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void completeWithdrawal_shouldThrowNotFound_whenWithdrawalNotFound() {
        when(walletTransactionRepository.existsByWalletWithdrawalId(
                20L,
                WalletTransactionType.WITHDRAW_TO_CARD
        )).thenReturn(false);

        when(walletWithdrawalRepository.findById(20L))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.completeWithdrawal(
                        30L,
                        20L,
                        rub,
                        new BigDecimal("1500"),
                        now
                )
        );

        assertEquals("Wallet withdrawal not found", exception.getMessage());

        verifyNoInteractions(walletRepository);
    }
}