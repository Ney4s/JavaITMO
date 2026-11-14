package neyas.itmo.repository;

import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, UUID> {

    Optional<SparePart> findByCode(SparePartId id);

    boolean existsByCode(SparePartId id);

    List<SparePart> findAllByCompatibleModels(Model model);


}
