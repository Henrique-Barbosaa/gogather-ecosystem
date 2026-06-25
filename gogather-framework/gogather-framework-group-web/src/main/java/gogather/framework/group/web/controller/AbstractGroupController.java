package gogather.framework.group.web.controller;

import gogather.framework.group.jpa.domain.BaseGroup;
import gogather.framework.group.jpa.domain.BaseUser;
import gogather.framework.group.jpa.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public abstract class AbstractGroupController<T extends BaseGroup, REQ> {

    protected final GroupService groupService;

    public AbstractGroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // Método abstrato: a aplicação filha vai saber como transformar o JSON (REQ) na Entidade (T)
    protected abstract T mapToEntity(REQ request);
    
    // Método abstrato: a aplicação filha resolve quem é o usuário logado via JWT
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
        BaseUser user = getAuthenticatedUser();
        groupService.joinGroupByInviteCode(inviteCode, user);
        return ResponseEntity.ok().build();
    }
}
