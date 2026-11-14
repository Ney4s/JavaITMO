package neyas.itmo.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import neyas.itmo.Validation;
import neyas.itmo.entity.BaseEntity;
import neyas.itmo.entity.Identifiable;
import neyas.itmo.entity.type.UserRole;
import neyas.itmo.framework.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
public abstract class User extends BaseEntity {

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    @Column(nullable=false)
    private String username;

    @Column(nullable=false, unique=true)
    private String number;

    @Column(nullable=false, unique=true)
    private String email;

    protected User(
            @NotNull String username,
            String number,
            String email)
    {
        this.username = username;
        this.number = Validation.isAvailableNumberFormat(number);
        this.email = Validation.isAvailableMailFormat(email);
    }

    public abstract UserRole role();

    public boolean hasRole(UserRole role){
        return role() == role;
    }

    public boolean hasAnyRoles(Set<UserRole> required){
        return required.contains(role());
    }

    @Override
    public String toString(){
        return "%s { %s roles=%s }".formatted(getClass().getSimpleName(), username , role());
    }

}
