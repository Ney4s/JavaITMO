package neyas.itmo.repository;

import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelRepository extends JpaRepository<Model, UUID>, JpaSpecificationExecutor<Model> {

    Optional<Model> findByCode(ModelId id);
    boolean existsByCode(ModelId id);

    Model getById(UUID id);
}
