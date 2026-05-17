package com.austral.estoque.service;

import com.austral.estoque.domain.organization.*;
import com.austral.estoque.repository.organization.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationSeedService implements ApplicationRunner {

    private final CompanyRepository companyRepository;
    private final CountryRepository countryRepository;
    private final OperationalUnitRepository operationalUnitRepository;
    private final SectorRepository sectorRepository;
    private final CostCenterRepository costCenterRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Company company = companyRepository.findFirstByDeletedAtIsNullOrderByCreatedAtAsc()
            .orElseGet(() -> companyRepository.save(
                Company.builder().name("Austral").document("N/A").active(true).build()
            ));

        Country country = countryRepository.findFirstByDeletedAtIsNullOrderByCreatedAtAsc()
            .orElseGet(() -> countryRepository.save(
                Country.builder().company(company).name("Angola").code("AO").active(true).build()
            ));

        OperationalUnit unit = operationalUnitRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseGet(() -> operationalUnitRepository.save(
                OperationalUnit.builder()
                    .country(country)
                    .name("Unidade Principal")
                    .code("UP01")
                    .type(OperationalUnit.UnitType.ADMINISTRATIVO)
                    .active(true)
                    .build()
            ));

        Sector sector = sectorRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseGet(() -> sectorRepository.save(
                Sector.builder().unit(unit).name("Suprimentos").code("SUP").active(true).build()
            ));

        costCenterRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseGet(() -> costCenterRepository.save(
                CostCenter.builder().sector(sector).name("CC Principal").code("CC-001").active(true).build()
            ));

        warehouseRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseGet(() -> warehouseRepository.save(
                Warehouse.builder().unit(unit).name("Armazem Principal").code("ARM-001").active(true).build()
            ));

        log.info("Estrutura organizacional minima validada para operacao.");
    }
}
