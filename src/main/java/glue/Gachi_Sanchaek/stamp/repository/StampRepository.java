package glue.Gachi_Sanchaek.stamp.repository;

import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface StampRepository extends CrudRepository<Stamp, Long> {
    List<Stamp> findAll();
}
