package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.entity.Chore;
import com.role.net.roomiesapp.entity.Group;
import com.role.net.roomiesapp.entity.User;
import com.role.net.roomiesapp.repository.ChoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChoreService {

    private final ChoreRepository choreRepository;

    public ChoreService(ChoreRepository choreRepository) {
        this.choreRepository = choreRepository;
    }

    @Transactional
    public Chore createChore(Group group, User creator, String title, String description, LocalDate dueDate) {
        Chore chore = Chore.builder()
                .group(group)
                .creator(creator)
                .title(title)
                .description(description != null ? description : "")
                .dueDate(dueDate)
                .completed(false)
                .build();
        return choreRepository.save(chore);
    }

    @Transactional(readOnly = true)
    public List<Chore> getChores(Long groupId) {
        return choreRepository.findByGroupId(groupId);
    }

    @Transactional
    public Chore assignChore(Long choreId, User assignee) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));
        chore.setAssignee(assignee);
        return choreRepository.save(chore);
    }

    /** Alterna o status da tarefa (concluída <-> pendente). */
    @Transactional
    public Chore completeChore(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));
        chore.setCompleted(!chore.isCompleted());
        return choreRepository.save(chore);
    }
}
