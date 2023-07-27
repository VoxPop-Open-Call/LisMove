package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.enums.*;
import net.nextome.lismove.rest.dto.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class EnumMapper {

	public abstract EnumDto partialTypeToDto(PartialType e);
	public abstract List<EnumDto> partialTypeDto(List<PartialType> e);
	public abstract EnumDto rankingFilterToDto(RankingFilter e);
	public abstract List<EnumDto> rankingFilterDto(List<RankingFilter> e);
	public abstract EnumDto rankingValueToDto(RankingValue e);
	public abstract List<EnumDto> rankingValueDto(List<RankingValue> e);
	public abstract EnumDto refundStatusToDto(RefundStatus e);
	public abstract List<EnumDto> refundStatusDto(List<RefundStatus> e);
	public abstract EnumDto sessionStatusToDto(SessionStatus e);
	public abstract List<EnumDto> sessionStatusDto(List<SessionStatus> e);
	public abstract EnumDto sessionTypeToDto(SessionType e);
	public abstract List<EnumDto> sessionTypeDto(List<SessionType> e);

	@AfterMapping
	@Mapping(target = "value", ignore = true)
	public void partialTypeToDto(PartialType e, @MappingTarget EnumDto dto) {
		dto.setId(e.ordinal());
		dto.setName(e.name());
	}
	@AfterMapping
	@Mapping(target = "value", ignore = true)
	public void rankingFilterToDto(RankingFilter e, @MappingTarget EnumDto dto) {
		dto.setId(e.ordinal());
		dto.setName(e.name());
	}
	@AfterMapping
	public void rankingValueToDto(RankingValue e, @MappingTarget EnumDto dto) {
		dto.setId(e.ordinal());
		dto.setName(e.name());
		dto.setValue(e.getColumnName());
	}
	@AfterMapping
	public void refundStatusToDto(RefundStatus e, @MappingTarget EnumDto dto) {
		dto.setId(e.ordinal());
		dto.setName(e.name());
		dto.setValue(e.getMsg());
	}
	@AfterMapping
	public void sessionStatusToDto(SessionStatus e, @MappingTarget EnumDto dto) {
		dto.setId(e.ordinal());
		dto.setName(e.name());
		dto.setValue(e.getMsg());
	}
	@AfterMapping
	public void sessionTypeToDto(SessionType e, @MappingTarget EnumDto dto) {
		dto.setId(e.ordinal());
		dto.setName(e.name());
		dto.setValue(e.getName());
	}


}
