package net.nextome.lismove.rest;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Achievement;
import net.nextome.lismove.rest.dto.AchievementDto;
import net.nextome.lismove.rest.dto.AchievementUserListDto;
import net.nextome.lismove.rest.dto.AwardAchievementDto;
import net.nextome.lismove.rest.mappers.AchievementsMapper;
import net.nextome.lismove.rest.mappers.AwardMapper;
import net.nextome.lismove.services.AchievementService;
import net.nextome.lismove.services.AwardService;
import net.nextome.lismove.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("achievements")
public class AchievementController {

	@Autowired
	private AchievementService achievementService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private AwardService awardService;
	@Autowired
	private AchievementsMapper achievementsMapper;
	@Autowired
	private AwardMapper awardMapper;

	@GetMapping
	public List<AchievementDto> getAll(
			@RequestParam(defaultValue = "false", required = false) Boolean active,
			@RequestParam(defaultValue = "false", required = false) Boolean national
	) {
		if(national) {
			return achievementsMapper.achievementToDto(achievementService.findNationalAchievements(active));
		} else {
			return achievementsMapper.achievementToDto(achievementService.findAll(active));
		}
	}

	@PostMapping
	public AchievementDto create(@RequestBody AchievementDto dto) {
		if(dto.getOrganization() != null) {
			organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
		}
		return achievementsMapper.achievementToDto(achievementService.save(achievementsMapper.dtoToAchievement(dto)));
	}

	@PutMapping("{aid}")
	public AchievementDto update(@PathVariable Long aid, @RequestBody AchievementDto dto) {
		Achievement old = achievementService.findById(aid).orElseThrow(() -> new LismoveException("Achievement not found", HttpStatus.NOT_FOUND));
		return achievementsMapper.achievementToDto(achievementService.update(old, achievementsMapper.dtoToAchievement(dto)));
	}

	@DeleteMapping("{aid}")
	public String delete(@PathVariable Long aid) {
		Achievement achievement = achievementService.findById(aid).orElseThrow(() -> new LismoveException("Achievement not found", HttpStatus.NOT_FOUND));
		try {
			achievementService.delete(achievement);
		} catch(DataIntegrityViolationException e) {
			throw new LismoveException(e.getMessage());
		}
		return "Deleted";
	}

	@GetMapping("{aid}/awards")
	public List<AwardAchievementDto> getAwards(@PathVariable Long aid) {
		Achievement achievement = achievementService.findById(aid).orElseThrow(() -> new LismoveException("Achievement not found", HttpStatus.NOT_FOUND));
		return awardMapper.awardAchievementToDto(awardService.findAllByAchievement(achievement));
	}

	@GetMapping("{aid}/users")
	public AchievementUserListDto getAchievements(@PathVariable Long aid) {
		Achievement achievement = achievementService.findById(aid).orElseThrow(() -> new LismoveException("Achievement not found", HttpStatus.NOT_FOUND));
		return achievementService.getAchievementUserList(achievement);
	}
}
