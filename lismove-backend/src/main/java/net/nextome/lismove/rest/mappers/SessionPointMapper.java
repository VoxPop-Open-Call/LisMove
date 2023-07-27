package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.SessionPoint;
import net.nextome.lismove.rest.dto.SessionPointDto;
import net.nextome.lismove.services.OrganizationService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class SessionPointMapper extends UtilMapper {
	@Autowired
	private OrganizationService organizationService;

	@Mapping(target = "organization", source = "organizationId")
	public abstract SessionPoint dtoToSessionPoint(SessionPointDto dto);

	@Mapping(target = "organizationId", source = "organization.id")
	@Mapping(target = "organizationTitle", source = "organization.title")
	@Mapping(target = "sessionId", source = "session.id")
	public abstract SessionPointDto sessionPointToDto(SessionPoint sessionPoint);

	public abstract List<SessionPoint> dtoToSessionPoint(List<SessionPointDto> dtoList);

	public abstract List<SessionPointDto> sessionPointToDto(List<SessionPoint> sessionPointList);

}
