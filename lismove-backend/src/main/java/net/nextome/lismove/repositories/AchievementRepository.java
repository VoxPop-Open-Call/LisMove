package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Achievement;
import net.nextome.lismove.models.Organization;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface AchievementRepository extends CrudRepository<Achievement, Long> {

	List<Achievement> findByOrganization(Organization org);

    List<Achievement> findByOrganizationIsNull();

    List<Achievement> findAllByEndDateGreaterThanEqual(LocalDate date);

    List<Achievement> findByOrganizationIsNullAndEndDateGreaterThanEqual(LocalDate date);
}
