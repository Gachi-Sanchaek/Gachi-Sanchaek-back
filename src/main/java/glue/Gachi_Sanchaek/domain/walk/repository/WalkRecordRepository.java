package glue.Gachi_Sanchaek.domain.walk.repository;

import glue.Gachi_Sanchaek.domain.walk.entity.WalkRecord;
import glue.Gachi_Sanchaek.domain.walk.enums.VerificationMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalkRecordRepository extends JpaRepository<WalkRecord,Long> {
    Optional<WalkRecord> findTopByUser_IdAndVerificationMethodOrderByStartTimeDesc(Long userId, VerificationMethod verificationMethod);
}
