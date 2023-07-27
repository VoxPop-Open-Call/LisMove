package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.OrganizationSettingValue;
import net.nextome.lismove.rest.dto.OrganizationSettingValueDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationSettingValueRepository extends CrudRepository<OrganizationSettingValue, Long> {

    @Query(value = "SELECT new net.nextome.lismove.rest.dto.OrganizationSettingValueDto(v.id, coalesce(v.value, s.defaultValue), s.defaultValue, s.name, v.organization.id) FROM OrganizationSetting s LEFT OUTER JOIN OrganizationSettingValue v ON s = v.organizationSetting AND v.organization=:organization")
    List<OrganizationSettingValueDto> findByOrganization(Organization organization);

    Optional<OrganizationSettingValue> findByOrganizationIdAndOrganizationSettingName(Long organization_id, String organizationSetting_name);

    Optional<OrganizationSettingValue> findByOrganizationAndOrganizationSettingName(Organization organization, String organizationSetting_name);

}
