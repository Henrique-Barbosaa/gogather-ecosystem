package com.role.net.tripmaker.service;

import com.role.net.tripmaker.dto.packing.CreatePackingItemRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.PackingItem;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.PackingRepository;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackingService {

    private final PackingRepository packingRepository;
    private final TripGroupRepository groupRepository;
    private final UserRepository userRepository;

    public PackingService(PackingRepository packingRepository, TripGroupRepository groupRepository, UserRepository userRepository) {
        this.packingRepository = packingRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    private Group validateMembership(Long groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Viagem não encontrada."));
        if (!group.hasMember(user.getId().toString())) {
            throw new IllegalArgumentException("Usuário não é membro desta viagem.");
        }
        return group;
    }

    @Transactional
    public PackingItem addItem(Long groupId, CreatePackingItemRequest request, User loggedUser) {
        Group group = validateMembership(groupId, loggedUser);

        PackingItem item = PackingItem.builder()
                .group(group)
                .creator(loggedUser)
                .name(request.name())
                .packed(false)
                .build();

        return packingRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<PackingItem> getList(Long groupId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        return packingRepository.findByGroupIdOrderByIdDesc(groupId);
    }

    @Transactional
    public PackingItem assignItem(Long groupId, Long itemId, Long userId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        validateMembership(groupId, assignee);

        PackingItem item = packingRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado."));
        
        if (!item.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Item não pertence a esta viagem.");
        }

        item.setAssignee(assignee);
        return packingRepository.save(item);
    }

    @Transactional
    public PackingItem markAsPacked(Long groupId, Long itemId, User loggedUser) {
        validateMembership(groupId, loggedUser);
        PackingItem item = packingRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado."));
        
        if (!item.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Item não pertence a esta viagem.");
        }

        item.setPacked(true);
        return packingRepository.save(item);
    }
}
