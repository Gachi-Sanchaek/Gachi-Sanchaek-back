package glue.Gachi_Sanchaek.domain.walkLocation.repository;

import glue.Gachi_Sanchaek.domain.walkLocation.entity.WalkLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<WalkLocation,Long> {
}
