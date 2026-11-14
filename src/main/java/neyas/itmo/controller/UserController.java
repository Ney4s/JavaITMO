package neyas.itmo.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import neyas.itmo.dto.UserDto;
import neyas.itmo.dto.requests.CreateClientRequst;
import neyas.itmo.entity.type.UserRole;
import neyas.itmo.entity.user.User;
import neyas.itmo.mapper.DomainMapper;
import neyas.itmo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final DomainMapper domainMapper;

    @PostMapping
    @Operation
    public UserDto registerClient(@RequestBody CreateClientRequst client ){
        return domainMapper.toDto(userService.registerClient(client.username(), client.email(), client.phone()));
    }

    @GetMapping
    @Operation
    public List<UserDto> findAll(@RequestParam(required = false) UserRole role){
        List<User> users = (role == null) ?
                            userService.findAll() :
                            userService.findAllByRole(role);

        return users.stream()
                .map(domainMapper::toDto)
                .toList();
    }

}
