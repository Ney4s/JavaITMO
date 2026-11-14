package neyas.itmo.entity.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.type.UserRole;

import java.util.UUID;

@Entity
@NoArgsConstructor
@DiscriminatorValue("ADMIN")
public class AdminUser extends User{

    public AdminUser(String username, String email, String number){
        super( username, number, email);
    }

    @Override
    public UserRole role() {
        return UserRole.ADMIN;
    }
}
