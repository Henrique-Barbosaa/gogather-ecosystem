package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.billing.*;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.RoomiesBillingService;
import gogather.framework.billing.dto.DebtStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private final RoomiesBillingService billingService;

    public BillingController(RoomiesBillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/groups/{groupExternalId}/bills")
    public ResponseEntity<BillResponse> createBill(
            @PathVariable UUID groupExternalId,
            @Valid @RequestBody CreateBillRequest request,
            @AuthenticationPrincipal User user) {
        BillResponse response = billingService.createBill(groupExternalId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/groups/{groupExternalId}/bills")
    public ResponseEntity<List<BillResponse>> getBillsByGroup(@PathVariable UUID groupExternalId) {
        List<BillResponse> response = billingService.getBillsByGroup(groupExternalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bills/{billExternalId}")
    public ResponseEntity<BillResponse> getBillDetails(@PathVariable UUID billExternalId) {
        BillResponse response = billingService.getBillDetails(billExternalId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/bills/{billExternalId}")
    public ResponseEntity<BillResponse> updateBill(
            @PathVariable UUID billExternalId,
            @RequestBody UpdateBillRequest request) {
        BillResponse response = billingService.updateBill(billExternalId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bills/{billExternalId}/debts")
    public ResponseEntity<List<DebtResponse>> getDebtsByBill(@PathVariable UUID billExternalId) {
        List<DebtResponse> response = billingService.getDebtsByBill(billExternalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debts/my")
    public ResponseEntity<List<DebtResponse>> getMyDebts(@AuthenticationPrincipal User user) {
        List<DebtResponse> response = billingService.getMyDebts(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/credits/my")
    public ResponseEntity<List<DebtResponse>> getMyCredits(@AuthenticationPrincipal User user) {
        List<DebtResponse> response = billingService.getMyCredits(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debts/{debtExternalId}/pix")
    public ResponseEntity<PixCodeResponse> getPixForDebt(@PathVariable UUID debtExternalId) {
        PixCodeResponse response = billingService.generatePixForDebt(debtExternalId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/debts/{debtExternalId}/status")
    public ResponseEntity<DebtResponse> updateDebtStatus(
            @PathVariable UUID debtExternalId,
            @RequestParam DebtStatus status) {
        DebtResponse response = billingService.updateDebtStatus(debtExternalId, status);
        return ResponseEntity.ok(response);
    }
}
