package glue.Gachi_Sanchaek.walk.repository;

import glue.Gachi_Sanchaek.walk.entity.WalkLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<WalkLocation,Long> {
}
