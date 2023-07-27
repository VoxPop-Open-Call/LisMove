package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import com.google.maps.model.LatLng;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.AwardCustomIssuer;
import net.nextome.lismove.models.enums.AwardType;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.rest.mappers.AwardMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class AwardService extends UtilitiesService {
	@Autowired
	private RankingService rankingService;
	@Autowired
	private UserService userService;
	@Autowired
	private GoogleMapsService googleMapsService;
	@Autowired
	private CouponService couponService;
	@Autowired
	private LogWallService logWallService;
	@Autowired
	private AwardRankingRepository awardRankingRepository;
	@Autowired
	private AwardAchievementRepository awardAchievementRepository;
	@Autowired
	private AwardAchievementUserRepository awardAchievementUserRepository;
	@Autowired
	private AwardPositionRepository awardPositionRepository;
	@Autowired
	private AwardPositionUserRepository awardPositionUserRepository;
	@Autowired
	private AwardCustomRepository awardCustomRepository;
	@Autowired
	private AwardCustomUserRepository awardCustomUserRepository;
	@Autowired
	private RankingRepository rankingRepository; //test
	@Autowired
	private AwardMapper awardMapper;

	@Autowired
	private Bugsnag bugsnag;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public AwardRanking save(AwardRanking award) {
		return awardRankingRepository.save(award);
	}

	public AwardAchievement save(AwardAchievement award) {
		return awardAchievementRepository.save(award);
	}

	public AwardPosition save(AwardPosition award) {
		if(!coordinatesNotNull(award)) {
			try {
				LatLng latLng = googleMapsService.generateLatLng(formatAddress(award));
				award.setLatitude(latLng.lat);
				award.setLongitude(latLng.lng);
			} catch(Exception e) {
				logger.error(e.getMessage());
				bugsnag.notify(e);
			}
		}
		return awardPositionRepository.save(award);
	}

	public AwardCustom save(AwardCustom award) {
		return awardCustomRepository.save(award);
	}

	public AwardCustomUser save(AwardCustomUser awardCustomUser) {
		return awardCustomUserRepository.save(awardCustomUser);
	}

	public AwardRanking update(AwardRankingDto dto, AwardRanking old) {
		notNullBeanCopy(awardMapper.dtoToAwardRanking(dto), old, "id");
		return awardRankingRepository.save(old);
	}

	public AwardAchievement update(AwardAchievementDto dto, AwardAchievement old) {
		notNullBeanCopy(awardMapper.dtoToAwardAchievement(dto), old, "id");
		return awardAchievementRepository.save(old);
	}

	public AwardPosition update(AwardPositionDto dto, AwardPosition old) {
		notNullBeanCopy(awardMapper.dtoToAwardPosition(dto), old, "id");
		if(!coordinatesNotNull(old)) {
			try {
				LatLng latLng = googleMapsService.generateLatLng(formatAddress(old));
				old.setLatitude(latLng.lat);
				old.setLongitude(latLng.lng);
			} catch(Exception e) {
				logger.error(e.getMessage());
				bugsnag.notify(e);
			}
		}
		return awardPositionRepository.save(old);
	}

	public AwardCustom update(AwardCustomDto dto, AwardCustom old) {
		notNullBeanCopy(awardMapper.dtoToAwardCustom(dto), old, "id");
		return awardCustomRepository.save(old);
	}

	public void delete(AwardRanking award) {
		if(award.getUser() != null) {
			throw new LismoveException("Award has already been assigned");
		}
		awardRankingRepository.delete(award);
	}

	public void delete(AwardAchievement award) {
		awardAchievementUserRepository.findByAwardAchievement(award).ifPresent(awardAchievementUser -> {
			throw new LismoveException("Award has already been assigned");
		});
		awardAchievementRepository.delete(award);
	}

	public void delete(AwardPosition award) {
		awardPositionUserRepository.findByAwardPosition(award).ifPresent(awardAchievementUser -> {
			throw new LismoveException("Award has already been assigned");
		});
		awardPositionRepository.delete(award);
	}

	public void delete(AwardCustom award) {
		if(awardCustomUserRepository.findAllByAwardCustom(award).size() > 0) {
			throw new LismoveException("Award has already been assigned");
		}
		awardCustomRepository.delete(award);
	}

	public AwardCustom generate(AwardType type, BigDecimal value, AwardCustomIssuer issuer, Organization org, Integer winningsAllowed, String name, String description, String imageUrl) {
		AwardCustom awardCustom = new AwardCustom();
		awardCustom.setType(type);
		awardCustom.setValue(value);
		awardCustom.setIssuer(issuer);
		awardCustom.setOrganization(org);
		awardCustom.setWinningsAllowed(winningsAllowed);
		awardCustom.setName(name);
		awardCustom.setDescription(description);
		awardCustom.setImageUrl(imageUrl);
		return awardCustomRepository.save(awardCustom);
	}

	public AwardCustomUser generateAndAssign(AwardType type, BigDecimal value, AwardCustomIssuer issuer, Organization org, Integer winningsAllowed, String name, String description, String imageUrl, User user) {
		return assignAwardCustom(user, generate(type, value, issuer, org, winningsAllowed, name, description, imageUrl), null);
	}

	public AwardCustomUser generateAndAssign(AwardType type, BigDecimal value, AwardCustomIssuer issuer, Organization org, Integer winningsAllowed, String name, String description, String imageUrl, User user, Article article) {
		return assignAwardCustom(user, generate(type, value, issuer, org, winningsAllowed, name, description, imageUrl), article);
	}

	public int assignAwardRankings(Ranking ranking) {
		int count = 0;
		if(ranking.getRankingPositions() == null) {
			rankingService.addRankingPositions(ranking);
		}
		for(AwardRanking award : awardRankingRepository.findAllByRanking(ranking)) {
			if(award.getUser() == null && ranking.getRankingPositions().size() > 0) {
				if(award.getPosition() != null && ranking.getRankingPositions().get(award.getPosition() - 1) != null) {
					userService.findByUsername(ranking.getRankingPositions().get(award.getPosition() - 1).getUsername()).ifPresent(award::setUser);
					count++;
				} else if(award.getRange() != null) {
					Integer[] range = getInterval(award.getRange());
					if(range[1] > ranking.getRankingPositions().size()) {
						range[1] = ranking.getRankingPositions().size() - 1;
					}
					userService.findByUsername(ranking.getRankingPositions().get(randomNumber(range[0], range[1])).getUsername()).ifPresent(award::setUser);
					count++;
				}
				// Generazione coupon
				if(award.getUser() != null && award.getType() != null && award.getType().equals(AwardType.MONEY)) {
					award.setCoupon(couponService.generate(award));
				}
			}
			awardRankingRepository.save(award);
		}
		rankingService.setAwardAssigned(ranking);
		return count;
	}

	public void assignAwardAchievements(AchievementUser achievementUser) {
		awardAchievementRepository.findByAchievement(achievementUser.getAchievement()).forEach(
				awardAchievement -> {
					try {
						checkNumberOfWinnings(achievementUser.getUser(), awardAchievement);
						AwardAchievementUser awardAchievementUser = new AwardAchievementUser(awardAchievement, achievementUser, LocalDateTime.now());
						if(awardAchievement.getType() != null && awardAchievement.getType().equals(AwardType.MONEY)) {
							awardAchievementUser.setCoupon(couponService.generate(awardAchievementUser));
						}
						awardAchievementUserRepository.save(awardAchievementUser);
						logWallService.writeLog(awardAchievementUser);
					} catch(LismoveException ignored) {
						//TODO gestisci eccezione
					}
				}
		);
	}

	public void assignAwardPosition(AwardPosition award, User user, Long timestamp) {
		checkNumberOfWinnings(user, award);
		AwardPositionUser awardPositionUser = new AwardPositionUser(award, user, LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
		if(awardPositionUser.getAwardPosition().getType() != null && awardPositionUser.getAwardPosition().getType().equals(AwardType.MONEY)) {
			awardPositionUser.setCoupon(couponService.generate(awardPositionUser));
		}
		awardPositionUserRepository.save(awardPositionUser);
	}

//	@Scheduled(cron = "0/10 * * * * ?") //test
	@Scheduled(cron = "0 0 2 * * ?")
	public void assignAwardsScheduled() {
		logger.info("Checking expired rankings for awarding");
		int count = 0;
		for(Ranking ranking : rankingService.findAllExpired()) {
			try {
				if (ranking.getAwardsAssigned() == null && ranking.getEndDate().until(LocalDate.now()).getDays() >= 5) {
					count += assignAwardRankings(ranking);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
				bugsnag.notify(e, report -> {
					report.addToTab("Ranking info", "id", ranking.getId());
				});
			}
		}
		logger.info("{} award(s) assigned", count);
	}

	public AwardCustomUser assignAwardCustom(User user, AwardCustom award) {
		return assignAwardCustom(user, award, null);
	}

	public AwardCustomUser assignAwardCustom(User user, AwardCustom award, Article article) {
		checkNumberOfWinnings(user, award);
		AwardCustomUser awardCustomUser = new AwardCustomUser(award, user);
		if(award.getType() != null && !award.getType().equals(AwardType.POINTS)) {
			awardCustomUser.setCoupon(couponService.generate(awardCustomUser, article));
		}
		return awardCustomUserRepository.save(awardCustomUser);
	}

	public void checkNumberOfWinnings(User user, AwardAchievement award) {
		if(award.getWinningsAllowed() != null && awardAchievementUserRepository.findByAwardAchievementAndAchievementUser_User(award, user).size() >= award.getWinningsAllowed()) {
			throw new LismoveException("Award already won by user", HttpStatus.BAD_REQUEST);
		}
	}

	public void checkNumberOfWinnings(User user, AwardCustom award) {
		if(award.getWinningsAllowed() != null && awardCustomUserRepository.findAllByAwardCustomAndUser(award, user).size() >= award.getWinningsAllowed()) {
			throw new LismoveException("Award already won by user", HttpStatus.BAD_REQUEST);
		}
	}

	public void checkNumberOfWinnings(User user, AwardPosition award) {
		if(award.getWinningsAllowed() != null && awardPositionUserRepository.findByAwardPositionAndUser(award, user).size() >= award.getWinningsAllowed()) {
			throw new LismoveException("Award already won by user", HttpStatus.BAD_REQUEST);
		}
	}

	public List<AwardDto> findAllByUser(User user) {
		List<AwardDto> awards = new LinkedList<>();
		awards.addAll(awardMapper.awardAchievementToAwardDto(findAllAwardAchievementsByUser(user)));
		awards.addAll(awardMapper.awardRankingToAwardDto(findAllAwardRankingsByUser(user)));
		awards.addAll(awardMapper.awardPositionToAwardDto(findAllAwardPositionUsersByUser(user)));
		awards.addAll(awardMapper.awardCustomToAwardDto(findAllAwardCustomUsersByUser(user)));
		return awards;
	}

	public List<AwardCustomUser> findAllAwardCustomUsersByUser(User user) {
		return awardCustomUserRepository.findAllByUser(user);
	}

	public List<AwardRanking> findAllAwardRankingsByUser(User user) {
		return awardRankingRepository.findAllByUser(user);
	}

	public List<AwardAchievementUser> findAllAwardAchievementsByUser(User user) {
		return awardAchievementUserRepository.findAllByAchievementUser_User(user);
	}

	public List<AwardPositionUser> findAllAwardPositionUsersByUser(User user) {
		return awardPositionUserRepository.findAllByUser(user);
	}

	public List<AwardPosition> findAwardPositionsByUser(User user) {
		List<AwardPosition> awards = findAllAwardPositionsByOrganization(null, true);
		userService.getActiveEnrollments(user).forEach(e -> awards.addAll(findAllAwardPositionsByOrganization(e.getOrganization(), true)));
		return awards;
	}

	public Optional<AwardRanking> findAwardRankingById(Long aid) {
		return awardRankingRepository.findById(aid);
	}

	public Optional<AwardAchievement> findAwardAchievementById(Long aid) {
		return awardAchievementRepository.findById(aid);
	}

	public Optional<AwardPosition> findAwardPositionById(Long aid) {
		return awardPositionRepository.findById(aid);
	}

	public List<AwardCustom> findAwardCustom() {
		List<AwardCustom> list = new LinkedList<>();
		awardCustomRepository.findAll().forEach(list::add);
		return list;
	}

	public Optional<AwardCustom> findAwardCustomById(Long aid) {
		return awardCustomRepository.findById(aid);
	}

	public List<AwardRanking> findAllByRankingOrderByValue(Ranking ranking) {
		return awardRankingRepository.findAllByRanking(ranking);
	}

	public List<AwardRanking> findAllByRanking(Ranking ranking) {
		return awardRankingRepository.findAllByRanking(ranking);
	}

	public List<AwardAchievement> findAllByAchievement(Achievement achievement) {
		return awardAchievementRepository.findByAchievement(achievement);
	}

	public List<AwardPosition> findAllAwardPositionsByOrganization(Organization organization, Boolean active) {
		if(active) {
			return awardPositionRepository.findAllByOrganizationAndEndDateGreaterThanEqual(organization, LocalDate.now());
		} else {
			return awardPositionRepository.findAllByOrganization(organization);
		}
	}

	public boolean fullAddressNotNull(AwardPosition award) {
		return award.getAddress() != null && award.getNumber() != null && award.getCity() != null;
	}

	public boolean fullAddressNotNull(AwardPositionDto award) {
		return award.getAddress() != null && award.getNumber() != null && award.getCity() != null;
	}

	public boolean coordinatesNotNull(AwardPosition award) {
		return award.getLatitude() != null && award.getLongitude() != null;
	}

	public boolean coordinatesNotNull(AwardPositionDto award) {
		return award.getLatitude() != null && award.getLongitude() != null;
	}

	public String formatAddress(AwardPosition award) {
		return award.getAddress() + ", " +
				award.getNumber() + ", " +
				award.getCity().getCap() + " " +
				award.getCity().getCity() + " " +
				award.getCity().getProvince();
	}

	public List<AwardCustomUser> findAwardCustomUsersByUser(User user) {
		return awardCustomUserRepository.findAllByUser(user);
	}
}
