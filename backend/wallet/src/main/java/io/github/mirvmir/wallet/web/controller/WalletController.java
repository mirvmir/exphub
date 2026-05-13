package io.github.mirvmir.wallet.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.wallet.application.service.interfaces.WalletService;
import io.github.mirvmir.wallet.web.request.WithdrawWalletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequiresCompletedProfile
@RequestMapping("/wallet")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/withdrawals")
    public ResponseEntity<Void> withdraw(
            @RequestBody
            WithdrawWalletRequest request
    ) {
        walletService.withdrawToCard(request);

        return ResponseEntity.noContent().build();
    }
}