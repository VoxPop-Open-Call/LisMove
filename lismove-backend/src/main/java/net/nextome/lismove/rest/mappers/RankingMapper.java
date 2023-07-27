package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.Ranking;
import net.nextome.lismove.rest.dto.RankingDto;
import net.nextome.lismove.services.OrganizationService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class RankingMapper extends UtilMapper {

	@Autowired
	protected UserMapper userMapper;
	@Autowired
	protected OrganizationService organizationService;

	@Mapping(target = "organization", source = "organization.id")
	public abstract RankingDto rankingToDto(Ranking ranking);

	public abstract List<RankingDto> rankingToDto(List<Ranking> ranking);

	public abstract Ranking dtoToRanking(RankingDto dto);

	Organization mapOrganization(Long value) {
		if(value == null) {
			return null;
		}
		return organizationService.findById(value).orElseThrow(() -> new LismoveException("L'organizzazione non esiste"));
	}
}
