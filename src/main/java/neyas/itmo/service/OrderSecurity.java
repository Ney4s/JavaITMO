package neyas.itmo.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import neyas.itmo.entity.order.BasicOrder;
import neyas.itmo.entity.user.User;
import neyas.itmo.repository.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    @NonNull private final OrderRepository orderRepository;
    @NonNull private final CurrectUserService currectUserService;

    @Transactional(readOnly = true)
    public boolean isOwner(UUID orderId) {
        UUID currectUserId = currectUserService.currectUserId();

        return orderRepository.findById(orderId)
                .map(BasicOrder::getClient)
                .map(User::getId)
                .map(currectUserId::equals)
                .orElse(false);
    }
}
