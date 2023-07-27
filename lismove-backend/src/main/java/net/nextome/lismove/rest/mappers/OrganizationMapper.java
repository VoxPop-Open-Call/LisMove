package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.*;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.services.OrganizationService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class OrganizationMapper extends UtilMapper {

	@Autowired
	protected OrganizationService organizationService;

	public abstract Organization dtoToOrganization(OrganizationDto dto);

	public abstract OrganizationDto organizationToDto(Organization organization);

	public abstract OrganizationOverviewDto organizationToOverviewDto(Organization organization);

	public abstract List<OrganizationOverviewDto> organizationToOverviewDto(List<Organization> organizations);

	@Mapping(target = "organization", source = "organization.id")
	public abstract CustomFieldDto customFieldToDto(CustomField customField);

	public abstract List<CustomFieldDto> customFieldToDto(List<CustomField> customField);

	@Mapping(target = "organization.id", source = "organization")
	public abstract CustomField dtoToCustomField(CustomFieldDto dto);

	@Mapping(target = "customField", source = "customField.id")
	@Mapping(target = "enrollment", source = "enrollment.id")
	public abstract CustomFieldValueDto customFieldValueToDto(CustomFieldValue customFieldValue);

	public abstract List<CustomFieldValueDto> customFieldValueToDto(List<CustomFieldValue> customFieldValues);

	@Mapping(target = "customField.id", source = "customField")
	@Mapping(target = "enrollment.id", source = "enrollment")
	public abstract CustomFieldValue dtoToCustomFieldValue(CustomFieldValueDto dto);

	@Mapping(target = "defaultValue", source = "organizationSetting.defaultValue")
	@Mapping(target = "organization", source = "organization.id")
	public abstract OrganizationSettingValueDto OrganizationSettingValueToDto(OrganizationSettingValue organizationSettingValue);

	public abstract List<OrganizationSettingValueDto> OrganizationSettingValueToDto(List<OrganizationSettingValue> organizationSettingValue);

	public abstract OrganizationSettingValue dtoToOrganizationSettingValue(OrganizationSettingValueDto dto);

	public abstract List<OrganizationSettingValue> dtoToOrganizationSettingValue(List<OrganizationSettingValueDto> dtos);
}
