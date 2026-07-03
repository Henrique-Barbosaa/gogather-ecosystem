package gogather.framework.group.jpa.domain;

import gogather.framework.core.Participant;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fw_group_member")
public class GroupMember implements Participant {

    @Override
    public String getIdentifier() {
        return this.id != null ? this.id.toString() : null;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private java.util.UUID externalId = java.util.UUID.randomUUID();

    public java.util.UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(java.util.UUID externalId) {
        this.externalId = externalId;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private BaseGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private BaseUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    private LocalDateTime joinedAt = LocalDateTime.now();

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public BaseGroup getGroup() { 
        return group; 
    }
    
    public void setGroup(BaseGroup group) { 
        this.group = group; 
    }

    public BaseUser getUser() { 
        return user; 
    }
    
    public void setUser(BaseUser user) { 
        this.user = user; 
    }

    public GroupRole getRole() { 
        return role; 
    }
    
    public void setRole(GroupRole role) { 
        this.role = role; 
    }

    public LocalDateTime getJoinedAt() { 
        return joinedAt; 
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) { 
        this.joinedAt = joinedAt; 
    }
}
