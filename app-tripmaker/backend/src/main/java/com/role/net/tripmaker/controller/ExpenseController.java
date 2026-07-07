package com.role.net.tripmaker.controller;

import com.role.net.tripmaker.dto.expense.CreateExpenseRequest;
import com.role.net.tripmaker.dto.expense.PixCodeResponse;
import com.role.net.tripmaker.dto.expense.TripDebtResponse;
import com.role.net.tripmaker.dto.expense.TripExpenseResponse;
import com.role.net.tripmaker.dto.expense.UpdateDebtStatusRequest;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.service.billing.TripBillingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups/{tripIdOrCode}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final TripBillingService billingService;

    @PostMapping
    public ResponseEntity<TripExpenseResponse> createExpense(
        @PathVariable String tripIdOrCode,
        @RequestBody @Valid CreateExpenseRequest request,
        @AuthenticationPrincipal User user
    ) {
        TripExpenseResponse response = billingService.createExpense(tripIdOrCode, request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TripExpenseResponse>> getTripExpenses(
        @PathVariable String tripIdOrCode,
        @AuthenticationPrincipal User user
    ) {
        List<TripExpenseResponse> response = billingService.getTripExpenses(tripIdOrCode, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debts")
    public ResponseEntity<List<TripDebtResponse>> getTripDebts(
        @PathVariable String tripIdOrCode,
        @AuthenticationPrincipal User user
    ) {
        List<TripDebtResponse> response = billingService.getTripDebts(tripIdOrCode, user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/debts/{debtId}/status")
    public ResponseEntity<TripDebtResponse> updateDebtStatus(
        @PathVariable String tripIdOrCode,
        @PathVariable Long debtId,
        @RequestBody @Valid UpdateDebtStatusRequest request,
        @AuthenticationPrincipal User user
    ) {
        TripDebtResponse response = billingService.updateDebtStatus(tripIdOrCode, debtId, request.status(), user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debts/{debtId}/pix-code")
    public ResponseEntity<PixCodeResponse> generatePixCodeForDebt(
        @PathVariable String tripIdOrCode,
        @PathVariable Long debtId,
        @AuthenticationPrincipal User user
    ) {
        PixCodeResponse response = billingService.generatePixCodeForDebt(tripIdOrCode, debtId, user);
        return ResponseEntity.ok(response);
    }
}
