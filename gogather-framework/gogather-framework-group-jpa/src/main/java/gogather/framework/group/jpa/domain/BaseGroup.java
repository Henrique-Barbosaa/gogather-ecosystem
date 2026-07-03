package gogather.framework.group.jpa.domain;

import gogather.framework.core.Group;
import gogather.framework.core.Participant;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fw_base_group")
@Inheritance(strategy = InheritanceType.JOINED)
// InheritanceType.JOINED cria uma tabela para os dados comuns e as aplicações filhas terão tabelas só com os dados específicos
public abstract class BaseGroup implements Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(unique = true, updatable = false, nullable = false, length = 8)
    private String inviteCode;

    @Column(name = "external_id", unique = true)
    private java.util.UUID externalId = java.util.UUID.randomUUID();

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 1. Cria a lista de membros gerenciada em Cascata!
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();

    public java.util.UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(java.util.UUID externalId) {
        this.externalId = externalId;
    }

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getInviteCode() { 
        return inviteCode; 
    }
    
    public void setInviteCode(String inviteCode) { 
        this.inviteCode = inviteCode; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    @Override
    public String getIdentifier() {
        return this.inviteCode;
    }

    @Override
    public boolean hasMember(String participantIdentifier) {
        if (this.members == null) return false;
        
        return this.members.stream()
                .anyMatch(m -> m.getUser().getId().toString().equals(participantIdentifier));
    }

    @Override
    public void addPendingParticipant(Participant participant, Participant addedBy) {
        // O Orquestrador chama isso, e a própria Entidade cria a relação para o JPA salvar
        GroupMember novoMembro = new GroupMember();
        novoMembro.setGroup(this);
        novoMembro.setUser((BaseUser) participant);
        novoMembro.setRole(GroupRole.MEMBER);
        
        if (this.members == null) {
            this.members = new ArrayList<>();
        }
        this.members.add(novoMembro);
    }
}