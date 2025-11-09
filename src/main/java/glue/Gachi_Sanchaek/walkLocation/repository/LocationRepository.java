package glue.Gachi_Sanchaek.walkLocation.repository;

import glue.Gachi_Sanchaek.walkLocation.entity.WalkLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<WalkLocation,Long> {
}
