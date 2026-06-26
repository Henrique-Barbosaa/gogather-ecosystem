package gogather.framework.group.web.controller;

import gogather.framework.group.jpa.domain.BaseGroup;
import gogather.framework.group.jpa.domain.BaseUser;
import gogather.framework.group.jpa.service.GroupService;
import gogather.framework.group.orchestrator.GroupMembershipOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public abstract class AbstractGroupController<T extends BaseGroup, REQ> {

    protected final GroupService groupService;
    // 1. Injetamos o Orquestrador do núcleo!
    protected final GroupMembershipOrchestrator orchestrator;

    public AbstractGroupController(GroupService groupService, GroupMembershipOrchestrator orchestrator) {
        this.groupService = groupService;
        this.orchestrator = orchestrator;
    }

    protected abstract T mapToEntity(REQ request);
    protected abstract BaseUser getAuthenticatedUser();

    @PostMapping
    public ResponseEntity<T> createGroup(@RequestBody REQ request) {
        T groupToCreate = mapToEntity(request);
        BaseUser creator = getAuthenticatedUser();
        T createdGroup = groupService.createGroup(groupToCreate, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<Void> joinGroup(@PathVariable String inviteCode) {
        BaseUser invitee = getAuthenticatedUser();
        
        //princípio de hollywood: Passamos a responsabilidade para a black-box
        // O Orchestrator vai validar se o usuário já existe e disparar a estratégia de validação
        orchestrator.inviteUserToGroup(inviteCode, invitee.getId().toString(), null);
        
        return ResponseEntity.ok().build();
    }
}