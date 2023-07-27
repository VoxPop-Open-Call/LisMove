package net.nextome.lismove.rest;

import net.nextome.lismove.models.enums.*;
import net.nextome.lismove.rest.dto.EnumDto;
import net.nextome.lismove.rest.mappers.EnumMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.Part;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("enums")
public class EnumController {
	@Autowired
	private EnumMapper mapper;

	@GetMapping("partial-types")
	public List<EnumDto> getPartialTypes() {
		return mapper.partialTypeDto(Arrays.asList(PartialType.values()));
	}
	@GetMapping("ranking-filter")
	public List<EnumDto> getRankingFilter() {
		return mapper.rankingFilterDto(Arrays.asList(RankingFilter.values()));
	}
	@GetMapping("ranking-value")
	public List<EnumDto> getRankingValue() {
		return mapper.rankingValueDto(Arrays.asList(RankingValue.values()));
	}
	@GetMapping("refund-status")
	public List<EnumDto> getRefundStatus() {
		return mapper.refundStatusDto(Arrays.asList(RefundStatus.values()));
	}
	@GetMapping("session-status")
	public List<EnumDto> getSessionStatus() {
		return mapper.sessionStatusDto(Arrays.asList(SessionStatus.values()));
	}
	@GetMapping("session-type")
	public List<EnumDto> getSessionType() {
		return mapper.sessionTypeDto(Arrays.asList(SessionType.values()));
	}
}
