package org.example.stalleco_backend.repository;

import org.example.stalleco_backend.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUsername(String username);
    List<Vendor> findByStallNameContaining(String stallName);
    List<Vendor> findByIsActiveTrue(); 
}
