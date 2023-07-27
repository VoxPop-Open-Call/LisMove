package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.models.enums.RankingValue;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.AchievementRepository;
import net.nextome.lismove.repositories.AchievementUserRepository;
import net.nextome.lismove.rest.dto.AchievementUserListDto;
import net.nextome.lismove.rest.mappers.AchievementsMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AchievementService extends UtilitiesService {
	@Autowired
	private AchievementRepository achievementRepository;
	@Autowired
	private AchievementUserRepository achievementUserRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AwardService awardService;
	@Autowired
	private AchievementsMapper achievementsMapper;

	@Autowired
	private EntityManager entityManager;

	public Achievement save(Achievement achievement) {
		return achievementRepository.save(achievement);
	}

	public AchievementUser save(AchievementUser achievement) {
		return achievementUserRepository.save(achievement);
	}

	public Achievement update(Achievement old, Achievement upd) {
		notNullBeanCopy(upd, old, "id", "organization");
		save(old);
		return old;
	}

	public void delete(Achievement achievement) {
		achievementRepository.delete(achievement);
	}

	@Transactional
	public void updateAchievements(User user) {
		findActiveByUser(user).forEach(a -> {
			BigDecimal score;
			LocalDateTime start = LocalDate.now().minusDays(a.getDuration()).atStartOfDay();
			if(start.toLocalDate().isBefore(a.getStartDate())) start = a.getStartDate().atStartOfDay();
			if(a.getOrganization() == null) {
				score = getNationalUserScore(user, a.getValue(), start);
			} else {
				score = getIniziativeUserScore(user, a.getOrganization(), a.getValue(), start, a.getFilter(), a.getFilterValue());
			}
			AchievementUser achievementUser = findByUser(user, a);
			achievementUser.setScore(Optional.ofNullable(score).orElse(BigDecimal.ZERO));
			boolean wasFullfilled = Optional.ofNullable(achievementUser.getFullfilled()).orElse(false);
			achievementUser.setFullfilled(achievementUser.getScore().compareTo(a.getTarget()) >= 0);
			if(!wasFullfilled && achievementUser.getFullfilled()) {
				awardService.assignAwardAchievements(achievementUser);
			}
			achievementUserRepository.save(achievementUser);
		});

	}

	public void updateAchievements(String userId) {
		User user = userService.findByUid(userId).orElseThrow(() -> new LismoveException("User not found"));
		updateAchievements(user);
	}

	public AchievementUserListDto getAchievementUserList(Achievement a) {
		AchievementUserListDto dto = achievementsMapper.achievementToAchievementUserListDto(a);
		dto.setUsers(achievementsMapper.userToAchievementUserPositionDto(findAchievementUser(a)));
		return dto;
	}

	public List<Achievement> findByUser(User user) {
		List<Achievement> achievements = findNationalAchievements(false);
		List<Enrollment> enrollments = userService.getActiveEnrollments(user);
		enrollments.stream().map(Enrollment::getOrganization).collect(Collectors.toList()).forEach(o -> achievements.addAll(achievementRepository.findByOrganization(o)));
		return achievements.stream().filter(a -> a != null && userService.isUserInFilter(user, a.getFilter(), a.getFilterValue(), a.getOrganization())).collect(Collectors.toList());
	}

	public List<Achievement> findActiveByUser(User user) {
		//TODO usare la data della sessione piuttosto che LocalDate.now() ?
		return findByUser(user).stream().filter(achievement -> achievement.getStartDate().minusDays(1).isBefore(LocalDate.now()) && achievement.getEndDate().plusDays(1).isAfter(LocalDate.now())).collect(Collectors.toList());
	}

	public AchievementUser findByUser(User u, Achievement a) {
		return achievementUserRepository.findByUserAndAchievement(u, a).orElse(new AchievementUser(a, u));
	}

	public List<AchievementUser> findAchievementUser(User u) {
		return achievementUserRepository.findActiveByUser(u, LocalDate.now());
	}

	public List<AchievementUser> findAchievementUser(Achievement a) {
		return achievementUserRepository.findByAchievementOrderByScoreDesc(a);
	}

	public BigDecimal getNationalUserScore(User user, RankingValue rankingValue, LocalDateTime start) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> query = builder.createQuery(BigDecimal.class);
		Root<Session> root = query.from(Session.class);

		query
				.select(builder.sum(root.get(rankingValue.getColumnName())))
				.where(builder.greaterThan(root.get("startTime"), start), builder.equal(root.get("user"), user), builder.equal(root.get("valid"), true));
		return entityManager.createQuery(query).getSingleResult();
	}

	public BigDecimal getIniziativeUserScore(User user, Organization org, RankingValue rankingValue, LocalDateTime start, RankingFilter rankingFilter, String filterValue) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<BigDecimal> query = builder.createQuery(BigDecimal.class);
		Root<SessionPoint> sessionPointRoot = query.from(SessionPoint.class);
		Join<SessionPoint, Session> sessionJoin = sessionPointRoot.join("session", JoinType.LEFT);
		Expression<BigDecimal> aggregate = rankingValue.equals(RankingValue.WORK_NUM) ?
				builder.count(sessionPointRoot.get(rankingValue.getColumnName())).as(BigDecimal.class) :
				builder.sum(sessionPointRoot.get(rankingValue.getColumnName()));

		List<Predicate> predicates = new LinkedList<>();
		predicates.add(builder.between(sessionJoin.get("startTime"), start, LocalDateTime.now()));
		predicates.add(builder.equal(sessionJoin.get("user"), user));
		predicates.add(builder.equal(sessionPointRoot.get("organization"), org));
		predicates.add(builder.equal(sessionJoin.get("valid"), true));

		if(rankingValue.equals(RankingValue.WORK_KM) || rankingValue.equals(RankingValue.WORK_NUM)) {
			predicates.add(builder.equal(sessionJoin.get("isHomeWorkPath"), true));
		}
		if(rankingFilter != null && rankingFilter.equals(RankingFilter.TYPE)) {
			predicates.add(builder.equal(sessionJoin.get("type"), SessionType.values()[Integer.parseInt(filterValue)]));
		}

		query
				.select(aggregate)
				.where(predicates.toArray(new Predicate[0]));
		return Optional.ofNullable(entityManager.createQuery(query).getSingleResult()).orElse(BigDecimal.ZERO);
	}

	public Optional<Achievement> findById(Long id) {
		return achievementRepository.findById(id);
	}

	public List<Achievement> findAll(boolean active) {
		List<Achievement> achievements;
		if(active) {
			achievements = achievementRepository.findAllByEndDateGreaterThanEqual(LocalDate.now());
		} else {
			achievements = new LinkedList<>();
			achievementRepository.findAll().forEach(achievements::add);
		}
		return achievements;
	}

	public List<Achievement> findNationalAchievements(boolean active) {
		if(active) {
			return achievementRepository.findByOrganizationIsNullAndEndDateGreaterThanEqual(LocalDate.now());
		} else {
			return achievementRepository.findByOrganizationIsNull();
		}
	}

	public List<Achievement> findByOrganization(Organization org) {
		return achievementRepository.findByOrganization(org);
	}
}
