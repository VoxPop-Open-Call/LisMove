package net.nextome.lismove.repositories;

import net.nextome.lismove.models.AwardPosition;
import net.nextome.lismove.models.Organization;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface AwardPositionRepository extends CrudRepository<AwardPosition, Long> {

    List<AwardPosition> findAllByOrganization(Organization organization);

    List<AwardPosition> findAllByOrganizationAndEndDateGreaterThanEqual(Organization organization, LocalDate endDate);

}
