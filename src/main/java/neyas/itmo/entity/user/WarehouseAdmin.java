package neyas.itmo.entity.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.type.UserRole;

import java.util.UUID;

@Entity
@NoArgsConstructor
@DiscriminatorValue("WAREHOUSE_MANAGER")
public class WarehouseAdmin extends User {

    public WarehouseAdmin(String username, String email, String number){
        super( username, number, email);
    }

    @Override
    public UserRole role(){
        return UserRole.WAREHOUSE_MANAGER;
    }

}
