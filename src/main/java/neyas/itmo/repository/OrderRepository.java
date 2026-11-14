package neyas.itmo.repository;

import neyas.itmo.entity.order.BasicOrder;
import neyas.itmo.entity.type.OrderType;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<BasicOrder, UUID>, JpaSpecificationExecutor<BasicOrder> {

    List<BasicOrder> findAllByClient(Client client);

    @Query("select o from BasicOrder o where type(o) = :type")
    <T extends BasicOrder> List<T> findAllbyType(@Param("type") Class<T> type);
}
