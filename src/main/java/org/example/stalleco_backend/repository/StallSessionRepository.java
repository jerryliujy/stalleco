package org.example.stalleco_backend.repository;

import org.example.stalleco_backend.model.StallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StallSessionRepository extends JpaRepository<StallSession, Long> {
    Optional<StallSession> findByVendorIdAndEndTimeIsNull(Long vendorId);
    List<StallSession> findByVendorIdOrderByStartTimeDesc(Long vendorId);
}