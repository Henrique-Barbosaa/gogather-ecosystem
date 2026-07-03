package gogather.framework.group.jpa.domain;

import gogather.framework.core.Participant;

import jakarta.persistence.*;

@Entity
@Table(name = "fw_base_user")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseUser implements Participant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true)
    private String email;

    @Column(name = "external_id", unique = true)
    private java.util.UUID externalId = java.util.UUID.randomUUID();

    @Column(name = "display_name")
    private String displayName;

    public java.util.UUID getExternalId() {
        return externalId;
    }

    public void setExternalId(java.util.UUID externalId) {
        this.externalId = externalId;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getIdentifier() {
        return this.id != null ? this.id.toString() : null;
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

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
}
