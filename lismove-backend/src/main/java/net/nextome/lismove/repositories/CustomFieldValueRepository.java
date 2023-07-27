package net.nextome.lismove.repositories;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.RankingFilter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomFieldValueRepository extends CrudRepository<CustomFieldValue, Long> {

	List<CustomFieldValue> findByCustomFieldOrganizationAndEnrollmentUser(Organization o, User u);

	List<CustomFieldValue> findByEnrollment(Enrollment e);

	List<CustomFieldValue> findByCustomField(CustomField customField);

	Optional<CustomFieldValue> findByEnrollmentUserAndCustomFieldTypeAndCustomFieldOrganization(User u, RankingFilter type, Organization o);

	Optional<CustomFieldValue> findByEnrollmentAndCustomField(Enrollment enrollment, CustomField customField);
}
