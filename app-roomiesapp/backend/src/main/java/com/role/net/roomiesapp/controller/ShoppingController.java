package com.role.net.roomiesapp.controller;

import com.role.net.roomiesapp.dto.shopping.CreateShoppingItemRequest;
import com.role.net.roomiesapp.dto.shopping.ShoppingItemResponse;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.service.ShoppingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households/{groupId}/shopping")
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @PostMapping
    public ResponseEntity<ShoppingItemResponse> addItem(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateShoppingItemRequest request,
            @AuthenticationPrincipal User loggedUser) {
        ShoppingItemResponse response = shoppingService.addItem(groupId, request, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ShoppingItemResponse>> getList(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User loggedUser) {
        List<ShoppingItemResponse> responses = shoppingService.getList(groupId, loggedUser);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{itemId}/buy")
    public ResponseEntity<ShoppingItemResponse> markAsBought(
            @PathVariable Long groupId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal User loggedUser) {
        ShoppingItemResponse response = shoppingService.markAsBought(groupId, itemId, loggedUser);
        return ResponseEntity.ok(response);
    }
}
