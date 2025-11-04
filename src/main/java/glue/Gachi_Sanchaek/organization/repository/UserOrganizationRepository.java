package glue.Gachi_Sanchaek.organization.repository;

import glue.Gachi_Sanchaek.organization.entity.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {
    boolean existsByUserIdAndOrganizationId(Long userId, Long organizationId);
}
