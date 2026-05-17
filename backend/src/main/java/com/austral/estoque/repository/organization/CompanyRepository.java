package com.austral.estoque.repository.organization;

import com.austral.estoque.domain.organization.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findFirstByDeletedAtIsNullOrderByCreatedAtAsc();
}
