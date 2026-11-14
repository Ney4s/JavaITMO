package neyas.itmo.service;

import lombok.RequiredArgsConstructor;
import neyas.itmo.entity.user.User;
import neyas.itmo.exception.AccessDeniedException;
import neyas.itmo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrectUserService {

    private final UserRepository userRepository;

    @Transactional
    public User currectUser(){
        Jwt jwt = currectUserJwt();
        return userRepository.findByKeycloakId(jwt.getSubject()).orElseGet(
                () -> link(jwt)
        );
    }

    @Transactional
    public UUID currectUserId(){
        return currectUser().getId();
    }


    private User link(Jwt jwt){
        String email = jwt.getClaimAsString("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new AccessDeniedException("User not found"));
        user.setKeycloakId(jwt.getSubject());
        return userRepository.save(user);
    }


    private Jwt currectUserJwt(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || !(authentication.getPrincipal() instanceof Jwt jwt)){
            throw new AccessDeniedException("Access denied");
        }
        return jwt;
    }



}
