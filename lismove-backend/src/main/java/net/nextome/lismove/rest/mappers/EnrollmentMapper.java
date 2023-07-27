package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Enrollment;
import net.nextome.lismove.rest.dto.EnrollmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class EnrollmentMapper extends UtilMapper {

	@Mapping(target = "user", source = "user.username")
	@Mapping(target = "uid", source = "user.uid")
	@Mapping(target = "organization", source = "organization.id")
	@Mapping(target = "organizationTitle", source = "organization.title")
	public abstract EnrollmentDto enrollmentToDto(Enrollment enrollment);

	public abstract List<EnrollmentDto> enrollmentToDto(List<Enrollment> organizations);
}
