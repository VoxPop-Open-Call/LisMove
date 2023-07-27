package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.Ranking;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface RankingRepository extends CrudRepository<Ranking, Long> {

	List<Ranking> findAllByOrganization(Organization org);

	List<Ranking> findAllByOrganizationIsNull();

//    @Query(value = "SELECT r FROM Ranking r WHERE r.organization = :org AND :date BETWEEN r.startDate AND r.endDate")
//    Set<Ranking> findAllActiveByOrganization(@Param("org") Organization org, @Param("date") LocalDateTime date);

	List<Ranking> findAllByOrganizationAndEndDateGreaterThanEqual(Organization org, LocalDate date);

	List<Ranking> findAllByOrganizationIsNullAndEndDateGreaterThanEqual(LocalDate date);

	List<Ranking> findAllByEndDateGreaterThanEqual(LocalDate date);

	List<Ranking> findAllByEndDateLessThanEqual(LocalDate date);

}
