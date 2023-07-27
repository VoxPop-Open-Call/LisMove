package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Smartphone;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SmartphoneRepository extends CrudRepository<Smartphone, Long> {

	List<Smartphone> findByUser(User u);

	List<Smartphone> findByEndAssociationIsNull();

	@Query(nativeQuery = true, value = "select * from smartphones where end_association is null and user_uid in (select enrollments.user_uid from enrollments where start_date<now() and end_date>now() and organization_id=:oid)")
	List<Smartphone> findActiveByOrganizationId(Long oid);
}
