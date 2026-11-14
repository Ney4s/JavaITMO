package neyas.itmo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.type.UserRole;
import neyas.itmo.entity.user.*;
import neyas.itmo.exception.EntityNotFoundException;
import neyas.itmo.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @PreAuthorize("permitAll()")
    @Transactional
    public Client registerClient(String userName, String email, String number) {
        return registerUser(new Client(userName, email, number ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AdminUser registerAdminUser(String userName, String email, String number) {
        return registerUser(new AdminUser(userName, email, number ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public WarehouseAdmin registerWarehouseAdmin(String userName, String email, String number) {
        return registerUser(new WarehouseAdmin(userName, email, number ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Manager registerManagerUser(String userName, String email, String number) {
        return registerUser(new Manager(userName, email, number ));
    }

    @Transactional
    public <T extends User> T registerUser(@NonNull T user) {
        Validation.require(userRepository.findByEmail(user.getEmail()).isEmpty(),
                "Пользователь с этой почтой уже зарегистрирован");
        userRepository.save(user);
        return user;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<User> findAll(){
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<User> findAllByRole(UserRole role){
        return userRepository.findAll().stream()
                .filter(user -> user.hasRole(role))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUserById(UUID id){
        requireUserById(id).markRemoved();
    }

    @Transactional(readOnly = true)
    public User requireUserById(UUID id){
        User user = userRepository.findById(id).orElseThrow(
                () -> EntityNotFoundException.of(User.class, id)
        );
        return user;
    }
}
