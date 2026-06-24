package gogather.framework.group.domain;

import gogather.framework.core.Group;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import gogather.framework.core.Participant;

@Entity
@Table(name = "fw_base_group")
@Inheritance(strategy = InheritanceType.JOINED)
// InheritanceType.JOINED dria uma tabela para os dados comuns e as aplicações filhas terão tabelas só com os dados especificos
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

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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

    @Override
    public String getIdentifier() {
        return this.inviteCode;
    }

    @Override
    public boolean hasMember(String participantIdentifier) {
        return false;
    }

    @Override
    public void addPendingParticipant(Participant participant, Participant addedBy) {
        //essa lógica será preenchida futuramente caso a aplicação exija aprovação de administradores para entrar.
    }
}