package repository;

import models.FineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FineTypeRepository extends JpaRepository<FineType, Long> {

    Optional<FineType> findByName(FineType.Name name);
}