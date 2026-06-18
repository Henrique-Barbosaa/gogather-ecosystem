package com.role.net.gogather.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.role.net.gogather.dto.expense.ExpenseDistributionPixResponse;
import com.role.net.gogather.entity.ExpenseDistribution;
import com.role.net.gogather.entity.PixInfo;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.exception.UnauthorizedRequestException;
import com.role.net.gogather.service.ExpenseService;
import com.role.net.gogather.service.PixService;

@RestController
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final PixService pixService;

    public ExpenseController(
        ExpenseService expenseService,
        PixService pixService
    ) {
        this.expenseService = expenseService;
        this.pixService = pixService;
    }

    @GetMapping("/split/{splitId}/pix-code")
    public ResponseEntity<ExpenseDistributionPixResponse> generatePixCode(
        @AuthenticationPrincipal User user,
        @PathVariable("splitId") UUID splitId
    ) {
        ExpenseDistribution expenseDistribution = expenseService.findExpenseDistributionByExternalId(splitId);

        if(!expenseDistribution.getDebtor().getUser().getId().equals(user.getId()))
            throw new UnauthorizedRequestException("Essa dívida não é sua!");

        PixInfo pixInfo = expenseDistribution.getCreditor().getUser().getPixInfo();
        String pixCopyAndPaste = pixService.gerarPixCopiaECola(
            pixInfo.getPixKey(),
            pixInfo.getMerchantName(),
            pixInfo.getMerchantCity(),
            expenseDistribution.getValue()
        );

        return ResponseEntity.ok(
            ExpenseDistributionPixResponse.from(
                expenseDistribution.getCreditor(),
                pixCopyAndPaste
            )
        );
    }

    @PatchMapping("/split/{splitId}/mark-as-paid")
    public ResponseEntity<Void> markSplitAsPaid(
        @AuthenticationPrincipal User user,
        @PathVariable("splitId") UUID splitExternalId
    ) {
        expenseService.markAsPaid(
            user.getId(),
            splitExternalId
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/split/{splitId}/confirm-receipt")
    public ResponseEntity<Void> markSplitAsSettled(
        @AuthenticationPrincipal User user,
        @PathVariable("splitId") UUID splitExternalId
    ) {
        expenseService.confirmReceipt(
            user.getId(),
            splitExternalId
        );
        return ResponseEntity.noContent().build();
    }

}
