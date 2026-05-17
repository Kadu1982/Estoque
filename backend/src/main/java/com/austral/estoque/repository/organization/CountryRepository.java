package com.austral.estoque.repository.organization;

import com.austral.estoque.domain.organization.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {
    Optional<Country> findFirstByDeletedAtIsNullOrderByCreatedAtAsc();
}
