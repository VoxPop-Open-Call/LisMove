package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Partial;
import net.nextome.lismove.rest.dto.PartialsOverviewDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface PartialRepository extends CrudRepository<Partial, Long> {

    @Query(value = "SELECT distinct lat, lng, type FROM heatmap_mat_view WHERE org = :oid AND age BETWEEN :minAge AND :maxAge AND time BETWEEN :minTime and :maxTime AND timestamp BETWEEN :minDate and :maxDate", nativeQuery = true)
    List<PartialsOverviewDto> findByOrganization(Long oid, Integer minAge, Integer maxAge, LocalDateTime minDate, LocalDateTime maxDate, LocalTime minTime, LocalTime maxTime);

    @Query(value = "SELECT distinct lat, lng, type FROM heatmap_mat_view WHERE org = :oid AND is_home_work_path = 'true' AND age BETWEEN :minAge AND :maxAge AND time BETWEEN :minTime and :maxTime AND timestamp BETWEEN :minDate and :maxDate", nativeQuery = true)
    List<PartialsOverviewDto> findByOrganizationWhereIsHomeWorkPath(Long oid, Integer minAge, Integer maxAge, LocalDateTime minDate, LocalDateTime maxDate, LocalTime minTime, LocalTime maxTime);

}
