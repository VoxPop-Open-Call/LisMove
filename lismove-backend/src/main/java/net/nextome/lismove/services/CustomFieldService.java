package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.repositories.CustomFieldRepository;
import net.nextome.lismove.repositories.CustomFieldValueRepository;
import net.nextome.lismove.rest.dto.CustomFieldDto;
import net.nextome.lismove.rest.mappers.OrganizationMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomFieldService extends UtilitiesService {
	@Autowired
	private CustomFieldRepository customFieldRepository;
	@Autowired
	private CustomFieldValueRepository customFieldValueRepository;
	@Autowired
	private OrganizationMapper organizationMapper;

	public CustomField save(CustomField customField) {
		return customFieldRepository.save(customField);
	}

	public CustomField update(CustomField old, CustomFieldDto dto) {
		CustomField upd = organizationMapper.dtoToCustomField(dto);
		notNullBeanCopy(upd, old, "id", "organization");
		return customFieldRepository.save(old);
	}

	public void delete(CustomField customField) {
		if(!findByCustomField(customField).isEmpty()) {
			throw new LismoveException("Custom Field referenced by Custom Field Values");
		}
		customFieldRepository.delete(customField);
	}

	public CustomFieldValue save(CustomFieldValue customFieldValue) {
		return customFieldValueRepository.save(customFieldValue);
	}

	public CustomFieldValue update(CustomFieldValue old, Boolean value) {
		old.setValue(value);
		return customFieldValueRepository.save(old);
	}

	public void delete(CustomFieldValue customFieldValue) {
		customFieldValueRepository.delete(customFieldValue);
	}

	public List<CustomField> findByOrganization(Organization o) {
		return customFieldRepository.findByOrganization(o);
	}

	public List<CustomFieldValue> findByUser(User u, Organization o) {
		return customFieldValueRepository.findByCustomFieldOrganizationAndEnrollmentUser(o, u);
	}

	public CustomField findByType(Organization o, RankingFilter type) {
		return customFieldRepository.findByOrganizationAndType(o, type);
	}

	public Optional<CustomFieldValue> findByType(Organization o, RankingFilter type, User u) {
		return customFieldValueRepository.findByEnrollmentUserAndCustomFieldTypeAndCustomFieldOrganization(u, type, o);
	}

	public List<CustomFieldValue> findByEnrollment(Enrollment e) {
		return customFieldValueRepository.findByEnrollment(e);
	}

	public List<CustomFieldValue> findByCustomField(CustomField customField) {
		return customFieldValueRepository.findByCustomField(customField);
	}

	public Optional<CustomField> findById(Long id) {
		return customFieldRepository.findById(id);
	}

	public Optional<CustomFieldValue> findValueById(Long id) {
		return customFieldValueRepository.findById(id);
	}

	public Optional<CustomFieldValue> findValueByEnrollmentAndCustomField(Enrollment enrollment, CustomField customField) {
		return customFieldValueRepository.findByEnrollmentAndCustomField(enrollment, customField);
	}
}
