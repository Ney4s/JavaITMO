package neyas.itmo.entity.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.type.UserRole;

import java.util.UUID;

@Entity
@DiscriminatorValue("CLIENT")
@NoArgsConstructor
public class Client extends User{

    public Client(
            String username, String email, String number
    ){
        super(username, number, email);
    }

    @Override
    public UserRole role() {
        return UserRole.CLIENT;
    }
}
