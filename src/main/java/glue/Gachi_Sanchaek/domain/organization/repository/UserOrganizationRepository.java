package glue.Gachi_Sanchaek.domain.organization.repository;

import glue.Gachi_Sanchaek.domain.organization.entity.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {
    boolean existsByUser_IdAndOrganization_Id(Long userId, Long organizationId);
    Optional<UserOrganization> findFirstByUser_Id(Long userId);
}
