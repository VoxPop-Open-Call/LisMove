package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Enrollment;
import net.nextome.lismove.models.Organization;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends CrudRepository<Enrollment, Long> {

	Optional<Enrollment> findByCode(String code);

	List<Enrollment> findByUserUid(String uid);

	@Query(value = "SELECT e FROM Enrollment e WHERE e.user.uid = :userUId AND :date BETWEEN e.startDate AND e.endDate")
	Optional<List<Enrollment>> findAllEnabledByUser(@Param("userUId") String userUId, @Param("date") LocalDate date);

	@Query(value = "SELECT e FROM Enrollment e WHERE e.organization.id = :org AND :date BETWEEN e.startDate AND e.endDate")
	Optional<List<Enrollment>> findAllEnabledByOrganization(@Param("org") Long org, @Param("date") LocalDate date);

	@Query(value = "SELECT e FROM Enrollment e WHERE e.user.uid = :userUId AND e.organization.id = :oid AND current_date BETWEEN e.startDate AND e.endDate")
	Optional<Enrollment> findActiveByUserAndOrganization(@Param("userUId") String userUId, @Param("oid") Long oid);

	@Query(value = "SELECT e FROM Enrollment e WHERE e.user.uid = :userUId AND e.organization.id = :oid AND :date BETWEEN e.startDate AND e.endDate")
	Optional<Enrollment> findActiveByUserAndOrganization(@Param("userUId") String userUId, @Param("oid") Long oid, @Param("date") LocalDate date);

	List<Enrollment> findByOrganization(Organization org);
}
