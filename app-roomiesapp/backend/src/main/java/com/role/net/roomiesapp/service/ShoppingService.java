package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.dto.shopping.CreateShoppingItemRequest;
import com.role.net.roomiesapp.dto.shopping.ShoppingItemResponse;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.ShoppingItem;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.RoomiesGroupRepository;
import com.role.net.roomiesapp.repository.ShoppingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShoppingService {

    private final ShoppingRepository shoppingRepository;
    private final RoomiesGroupRepository groupRepository;

    public ShoppingService(ShoppingRepository shoppingRepository, RoomiesGroupRepository groupRepository) {
        this.shoppingRepository = shoppingRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public ShoppingItemResponse addItem(Long groupId, CreateShoppingItemRequest request, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada."));

        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta casa.");
        }

        ShoppingItem item = ShoppingItem.builder()
                .group(group)
                .creator(loggedUser)
                .name(request.name())
                .quantity(request.quantity())
                .bought(false)
                .build();

        ShoppingItem saved = shoppingRepository.save(item);
        return ShoppingItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ShoppingItemResponse> getList(Long groupId, User loggedUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Casa não encontrada."));

        if (!group.hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta casa.");
        }

        return shoppingRepository.findByGroupIdOrderByBoughtAscCreatedAtItemDesc(groupId).stream()
                .map(ShoppingItemResponse::from)
                .toList();
    }

    @Transactional
    public ShoppingItemResponse markAsBought(Long groupId, Long itemId, User loggedUser) {
        ShoppingItem item = shoppingRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado."));

        if (!item.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Item não pertence a esta casa.");
        }

        if (!item.getGroup().hasMember(loggedUser.getId().toString())) {
            throw new ResourceNotFoundException("Você não é membro desta casa.");
        }

        item.setBought(true);
        item.setBuyer(loggedUser);
        ShoppingItem saved = shoppingRepository.save(item);

        return ShoppingItemResponse.from(saved);
    }
}
