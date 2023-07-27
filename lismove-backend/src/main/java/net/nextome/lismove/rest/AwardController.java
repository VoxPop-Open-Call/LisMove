package net.nextome.lismove.rest;

import io.swagger.annotations.ApiOperation;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.AwardCustomIssuer;
import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.rest.dto.AwardAchievementDto;
import net.nextome.lismove.rest.dto.AwardCustomDto;
import net.nextome.lismove.rest.dto.AwardPositionDto;
import net.nextome.lismove.rest.dto.AwardRankingDto;
import net.nextome.lismove.rest.mappers.AwardMapper;
import net.nextome.lismove.security.NextomeUserDetails;
import net.nextome.lismove.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("awards")
public class AwardController {

	@Autowired
	private AwardService awardService;
	@Autowired
	private RankingService rankingService;
	@Autowired
	private AchievementService achievementService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private UserService userService;
	@Autowired
	private AwardMapper awardMapper;

	@GetMapping("rankings/{aid}")
	public AwardRankingDto getAwardRanking(@PathVariable Long aid) {
		return awardMapper.awardRankingToDto(awardService.findAwardRankingById(aid).orElseThrow(() -> new LismoveException("Award not found", HttpStatus.NOT_FOUND)));
	}

	@GetMapping("achievements/{aid}")
	public AwardAchievementDto getAwardAchievement(@PathVariable Long aid) {
		return awardMapper.awardAchievementToDto(awardService.findAwardAchievementById(aid).orElseThrow(() -> new LismoveException("Award not found", HttpStatus.NOT_FOUND)));
	}

	@GetMapping("customs")
	public List<AwardCustomDto> getAwardCustom() {
		return awardMapper.awardCustomToDto(awardService.findAwardCustom());
	}

	@GetMapping("customs/{aid}")
	public AwardCustomDto getAwardCustom(@PathVariable Long aid) {
		return awardMapper.awardCustomToDto(awardService.findAwardCustomById(aid).orElseThrow(() -> new LismoveException("Award not found", HttpStatus.NOT_FOUND)));
	}

	@PostMapping("rankings")
	public AwardRankingDto createAwardRanking(@RequestBody AwardRankingDto dto) {
		rankingService.findById(dto.getRanking()).orElseThrow(() -> new LismoveException(("Ranking not found"), HttpStatus.NOT_FOUND));
		return awardMapper.awardRankingToDto(awardService.save(awardMapper.dtoToAwardRanking(dto)));
	}

	@PostMapping("achievements")
	public AwardAchievementDto createAwardAchievement(@RequestBody AwardAchievementDto dto) {
		achievementService.findById(dto.getAchievement()).orElseThrow(() -> new LismoveException(("Achievement not found"), HttpStatus.NOT_FOUND));
		return awardMapper.awardAchievementToDto(awardService.save(awardMapper.dtoToAwardAchievement(dto)));
	}

	@PostMapping("positions")
	public AwardPositionDto createAwardPosition(@RequestBody AwardPositionDto dto) {
		if(dto.getOrganization() != null) {
			organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
			// altrimenti si tratta di un award nazionale
		}
		if(!awardService.coordinatesNotNull(dto)
				|| !awardService.fullAddressNotNull(dto) && !awardService.coordinatesNotNull(dto)) {
			throw new LismoveException("Address not valid", HttpStatus.BAD_REQUEST);
		}
		return awardMapper.awardPositionToDto(awardService.save(awardMapper.dtoToAwardPosition(dto)));
	}

	@PostMapping("customs")
	@ApiOperation(value = "createAndAssignAwardCustom", notes = "Crea un premio di tipo AwardCustom e lo assegna all'utente che ha come uid: <tt>assignTo</tt>")
	public AwardCustomDto createAndAssignAwardCustom(@RequestBody AwardCustomDto dto, @RequestParam(required = false) String assignTo, @AuthenticationPrincipal @ApiIgnore NextomeUserDetails userDetails) {
		if(dto.getOrganization() != null) {
			organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));
			// altrimenti si tratta di un award nazionale
		}
		AwardCustom award = awardMapper.dtoToAwardCustom(dto);
		User issuer = userService.findByUid(userDetails.getUserData().getUid()).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		award.setIssuer(issuer.getUserType().equals(UserType.ROLE_MANAGER) ? AwardCustomIssuer.CREATED_BY_ORGANIZATION : AwardCustomIssuer.CREATED_BY_ADMIN);
		if (assignTo == null) {
			return awardMapper.awardCustomToDto(awardService.save(award));
		} else {
			award.setWinningsAllowed(1); // può essere vinto dal solo utente destinatario
			awardService.save(award);
			User user = userService.findByUid(assignTo).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
			AwardCustomUser awardCustomUser = awardService.assignAwardCustom(user, award);
			if(awardCustomUser == null) throw new LismoveException("Not assigned", HttpStatus.NOT_FOUND);
			return awardMapper.awardCustomUserToDto(awardCustomUser);
		}
	}

	@PostMapping("custom-users")
	@ApiOperation(value = "createAwardCustomUser", notes = "Associa l'utente trovato tramite <tt>uid</tt> all'award trovato tramite <tt>aid</tt>")
	public AwardCustomDto createAwardCustomUser(@RequestParam Long aid, @RequestParam String uid) {
		AwardCustom awardCustom = awardService.findAwardCustomById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		AwardCustomUser awardCustomUser = awardService.assignAwardCustom(user, awardCustom);
		if(awardCustomUser == null) throw new LismoveException("Not assigned", HttpStatus.NOT_FOUND);
		return awardMapper.awardCustomUserToDto(awardCustomUser);
	}

	@PutMapping("rankings/{aid}")
	public AwardRankingDto updateAwardRanking(@PathVariable Long aid, @RequestBody AwardRankingDto dto) {
		AwardRanking old = awardService.findAwardRankingById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		return awardMapper.awardRankingToDto(awardService.update(dto, old));
	}

	@PutMapping("achievements/{aid}")
	public AwardAchievementDto updateAwardAchievement(@PathVariable Long aid, @RequestBody AwardAchievementDto dto) {
		AwardAchievement old = awardService.findAwardAchievementById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		return awardMapper.awardAchievementToDto(awardService.update(dto, old));
	}

	@PutMapping("positions/{aid}")
	public AwardPositionDto updateAwardPosition(@PathVariable Long aid, @RequestBody AwardPositionDto dto) {
		AwardPosition old = awardService.findAwardPositionById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		if(!old.getOrganization().getId().equals(dto.getOrganization())) {
			throw new LismoveException("Award owned by another organization", HttpStatus.FORBIDDEN);
		}
		//TODO controlla
		if(!awardService.coordinatesNotNull(dto)) {
			throw new LismoveException("Address not valid", HttpStatus.BAD_REQUEST);
		}
		//TODO controlla
		if(!awardService.fullAddressNotNull(dto) && !awardService.coordinatesNotNull(dto)) {
			throw new LismoveException("Address not valid", HttpStatus.BAD_REQUEST);
		}
		return awardMapper.awardPositionToDto(awardService.update(dto, old));
	}

	@PutMapping("customs/{aid}")
	public AwardCustomDto updateAwardCustom(@PathVariable Long aid, @RequestBody AwardCustomDto dto) {
		AwardCustom old = awardService.findAwardCustomById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		if(dto.getOrganization() != null && old.getOrganization() != null && !old.getOrganization().getId().equals(dto.getOrganization())) {
			throw new LismoveException("Award owned by another organization", HttpStatus.FORBIDDEN);
		}
		if(dto.getWinningsAllowed() != null && dto.getWinningsAllowed() < old.getWinningsAllowed()) {
			throw new LismoveException("Cannot decrement 'winnings allowed' field", HttpStatus.BAD_REQUEST);
		}
		return awardMapper.awardCustomToDto(awardService.update(dto, old));
	}

	@DeleteMapping("rankings/{aid}")
	public String deleteAwardRanking(@PathVariable Long aid) {
		AwardRanking award = awardService.findAwardRankingById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		try {
			awardService.delete(award);
		} catch(DataIntegrityViolationException e) {
			throw new LismoveException(e.getMessage());
		}
		return "Deleted";
	}

	@DeleteMapping("achievements/{aid}")
	public String deleteAwardAchievement(@PathVariable Long aid) {
		AwardAchievement award = awardService.findAwardAchievementById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		try {
			awardService.delete(award);
		} catch(DataIntegrityViolationException e) {
			throw new LismoveException(e.getMessage());
		}
		return "Deleted";
	}

	@DeleteMapping("positions/{aid}")
	public String deleteAwardPosition(@PathVariable Long aid) {
		AwardPosition award = awardService.findAwardPositionById(aid).orElseThrow(() -> new LismoveException(("Award not found"), HttpStatus.NOT_FOUND));
		try {
			awardService.delete(award);
		} catch(DataIntegrityViolationException e) {
			throw new LismoveException(e.getMessage());
		}
		return "Deleted";
	}
}
