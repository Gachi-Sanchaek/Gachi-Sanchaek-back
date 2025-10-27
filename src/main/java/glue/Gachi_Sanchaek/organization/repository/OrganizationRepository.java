package glue.Gachi_Sanchaek.organization.repository;

import glue.Gachi_Sanchaek.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByKakaoPlaceId(Long kakaoPlaceId);
}
