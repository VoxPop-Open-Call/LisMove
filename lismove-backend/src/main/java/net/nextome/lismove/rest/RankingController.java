package net.nextome.lismove.rest;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Ranking;
import net.nextome.lismove.models.enums.RankingValue;
import net.nextome.lismove.rest.dto.AwardRankingDto;
import net.nextome.lismove.rest.dto.RankingDto;
import net.nextome.lismove.rest.mappers.AwardMapper;
import net.nextome.lismove.rest.mappers.RankingMapper;
import net.nextome.lismove.services.AwardService;
import net.nextome.lismove.services.OrganizationService;
import net.nextome.lismove.services.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("rankings")
public class RankingController {

	@Autowired
	private RankingService rankingService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private AwardService awardService;
	@Autowired
	private RankingMapper rankingMapper;
	@Autowired
	private AwardMapper awardMapper;

	@GetMapping("global")
	public RankingDto getGlobal() {
		return rankingMapper.rankingToDto(rankingService.getGlobal());
	}

	@GetMapping("{rid}")
	public RankingDto get(@PathVariable Long rid, @RequestParam(defaultValue = "false", required = false) Boolean withUsers) {
		Ranking ranking = rankingService.findById(rid).orElseThrow(() -> new LismoveException("Ranking not found", HttpStatus.NOT_FOUND));
		if(withUsers) {
			rankingService.addRankingPositions(ranking);
		}
		return rankingMapper.rankingToDto(ranking);
	}

	@GetMapping
	public List<RankingDto> getAll(
			@RequestParam(defaultValue = "false", required = false) Boolean active,
			@RequestParam(defaultValue = "false", required = false) Boolean national,
			@RequestParam(defaultValue = "false", required = false) Boolean withPositions
	) {
		if(national) {
			return rankingMapper.rankingToDto(rankingService.getNationals(active, withPositions));
		} else {
			return rankingMapper.rankingToDto(rankingService.findAll(active, withPositions));
		}
	}

	@PostMapping
	public RankingDto create(@RequestBody RankingDto dto) {
		if(dto.getOrganization() != null) {
			organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
		}
		if (dto.getOrganization() == null && dto.getValue() < 4) {
			throw new LismoveException("National rankings don't support value: " + RankingValue.values()[dto.getValue()].getColumnName(), HttpStatus.BAD_REQUEST);
		}
		return rankingMapper.rankingToDto(rankingService.create(dto));
	}

	@PutMapping("{rid}")
	public RankingDto update(@PathVariable Long rid, @RequestBody RankingDto dto) {
		Ranking old = rankingService.findById(rid).orElseThrow(() -> new LismoveException("Ranking not found", HttpStatus.NOT_FOUND));
		if(dto.getOrganization() != null) {
			organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
		}
		return rankingMapper.rankingToDto(rankingService.update(old, dto));
	}

	@DeleteMapping("{rid}")
	public String delete(@PathVariable Long rid) {
		Ranking ranking = rankingService.findById(rid).orElseThrow(() -> new LismoveException("Ranking not found", HttpStatus.NOT_FOUND));
		rankingService.delete(ranking);
		return "Deleted";
	}

	@GetMapping("{rid}/awards")
	public List<AwardRankingDto> getAwards(@PathVariable Long rid) {
		Ranking ranking = rankingService.findById(rid).orElseThrow(() -> new LismoveException(("Ranking not found"), HttpStatus.NOT_FOUND));
		return awardMapper.awardRankingToDto(awardService.findAllByRanking(ranking));
	}
}