package net.nextome.lismove.repositories;

import net.nextome.lismove.models.CustomField;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.enums.RankingFilter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomFieldRepository extends CrudRepository<CustomField, Long> {

	List<CustomField> findByOrganization(Organization o);

	CustomField findByOrganizationAndType(Organization o, RankingFilter type);
}
