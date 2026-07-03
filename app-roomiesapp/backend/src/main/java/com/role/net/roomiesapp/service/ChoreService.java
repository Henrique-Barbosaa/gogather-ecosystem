package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.entity.Chore;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.repository.ChoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChoreService {

    private final ChoreRepository choreRepository;

    public ChoreService(ChoreRepository choreRepository) {
        this.choreRepository = choreRepository;
    }

    @Transactional
    public Chore createChore(Group group, User creator, String description) {
        Chore chore = Chore.builder()
                .group(group)
                .creator(creator)
                .description(description)
                .completed(false)
                .build();
        return choreRepository.save(chore);
    }
}
