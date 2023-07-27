package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.models.enums.RankingRepeat;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.models.query.UserRankingPosition;
import net.nextome.lismove.repositories.EnrollmentRepository;
import net.nextome.lismove.repositories.RankingRepository;
import net.nextome.lismove.rest.dto.RankingDto;
import net.nextome.lismove.rest.dto.RankingPositionDto;
import net.nextome.lismove.rest.mappers.RankingMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RankingService extends UtilitiesService {

	@Autowired
	private UserService userService;
	@Autowired
	private CustomFieldService customFieldService;
	@Autowired
	private RankingRepository rankingRepository;
	@Autowired
	private RankingMapper rankingMapper;
	@Autowired
	private EnrollmentRepository enrollmentRepository;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private Bugsnag bugsnag;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public Ranking getGlobal() {
		Ranking global = new Ranking();
		global.setTitle("Globale");
		global.setRankingPositions(new ArrayList<>());
		int i = 1;
		for (UserRankingPosition user : userService.findAllOrderByEarnedNationalPointsDesc()) {
			global.getRankingPositions().add(new RankingPositionDto(user.getUsername(), Optional.ofNullable(user.getPoints()).orElse(BigDecimal.ZERO), user.getAvatar(), i++));
		}
		return global;
	}

	public List<Ranking> getNationals(Boolean active, boolean withPositions) {
		if(withPositions) {
			return addRankingPositions(findNationals(active));
		} else {
			return findNationals(active);
		}
	}

	public List<Ranking> addRankingPositions(List<Ranking> set) {
		for(Ranking ranking : set) {
			addRankingPositions(ranking);
		}
		return set;
	}

	public void addRankingPositions(Ranking ranking) {
		ranking.setRankingPositions(getPositions(ranking));
		List<User> emptyUsers = getUsersFromFilters(ranking.getFilter(), ranking.getFilterValue(), ranking.getOrganization());
		int size = emptyUsers.size();
		emptyUsers.removeIf(user -> ranking.getRankingPositions().stream().map(RankingPositionDto::getUsername).collect(Collectors.toList()).contains(user.getUsername()));
		Iterator<User> iterator = emptyUsers.iterator();
		for(int i = 0; i < size; i++) {
			if(i >= ranking.getRankingPositions().size() && iterator.hasNext()) {
				ranking.getRankingPositions().add(new RankingPositionDto(iterator.next(), BigDecimal.ZERO));
			}
			if (ranking.getRankingPositions().get(i) != null) {
				ranking.getRankingPositions().get(i).setPosition(i + 1);
			}
		}
	}

	public List<RankingPositionDto> getPositions(Ranking ranking) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<RankingPositionDto> query = builder.createQuery(RankingPositionDto.class);
		Expression<User> user;
		Expression<Long> aggregate = null;
		ArrayList<Predicate> predicates;

		if(ranking.getOrganization() == null) { //nationals
			Root<Session> sessionRoot = query.from(Session.class);
			user = sessionRoot.get("user");
			aggregate = builder.sum(sessionRoot.get(ranking.getValue().getColumnName()));
			predicates = generatePredicates(ranking, builder, sessionRoot);
		} else {
			Root<SessionPoint> sessionPointRoot = query.from(SessionPoint.class);
			Join<SessionPoint, Session> sessionJoin = sessionPointRoot.join("session", JoinType.LEFT);
			user = sessionJoin.get("user");
			predicates = generatePredicates(ranking, builder, sessionJoin);
			predicates.add(builder.equal(sessionPointRoot.get("organization"), ranking.getOrganization()));
			predicates.add(builder.equal(sessionJoin.get("valid"), true));
			switch(ranking.getValue()) {
				case URBAN_KM:
				case INITIATIVE_POINTS:
					aggregate = builder.sum(sessionPointRoot.get(ranking.getValue().getColumnName()));
					break;
				case WORK_KM:
					predicates.add(builder.equal(sessionJoin.get("isHomeWorkPath"), true));
					aggregate = builder.sum(sessionJoin.get(ranking.getValue().getColumnName()));
					break;
				case WORK_NUM:
					predicates.add(builder.equal(sessionJoin.get("isHomeWorkPath"), true));
					aggregate = builder.countDistinct(sessionPointRoot.get(ranking.getValue().getColumnName()));
					break;
			}
		}

		query
				.multiselect(user, aggregate)
				.where(predicates.toArray(new Predicate[0]))
				.groupBy(user)
				.having(builder.isNotNull(aggregate))
				.orderBy(builder.desc(aggregate));
		return entityManager.createQuery(query).getResultList();
	}

	private <X, Y> ArrayList<Predicate> generatePredicates(Ranking ranking, CriteriaBuilder builder, From<X, Y> from) {
		ArrayList<Predicate> predicates = new ArrayList<>();
		if(ranking.getStartDate() != null && ranking.getEndDate() != null) {
			predicates.add(builder.between(from.get("startTime"), ranking.getStartDate().atStartOfDay(), ranking.getEndDate().plusDays(1).atStartOfDay()));
		}

		if(ranking.getFilter() != null) {
			switch(ranking.getFilter()) {
				case AGE:
				case GENDER:
				case JOLLY_A:
				case JOLLY_B:
				case JOLLY_C:
					predicates.add(builder.in(from.get("user")).value(getUsersFromFilters(ranking.getFilter(), ranking.getFilterValue(), ranking.getOrganization())));
					break;
				case TYPE:
					predicates.add(builder.equal(from.get("type"), SessionType.values()[Integer.parseInt(ranking.getFilterValue())]));
					break;
			}
		}
		return predicates;
	}

	@Deprecated
	private <X, Y> ArrayList<Predicate> generatePredicatesOLD(Ranking ranking, CriteriaBuilder builder, From<X, Y> from) {
		ArrayList<Predicate> predicates = new ArrayList<>();
		if(ranking.getStartDate() != null && ranking.getEndDate() != null) {
			predicates.add(builder.between(from.get("startTime"), ranking.getStartDate().atStartOfDay(), ranking.getEndDate().plusDays(1).atStartOfDay()));
		}

		if(ranking.getFilter() != null) {
			switch(ranking.getFilter()) {
				case AGE:
					if(ranking.getFilterValue() != null) {
						Integer[] interval = getInterval(ranking.getFilterValue());
						if(interval[0] != null) {
							predicates.add(builder.greaterThanOrEqualTo(from.join("user").get("age"), interval[0]));
						}
						if(interval[1] != null) {
							predicates.add(builder.lessThanOrEqualTo(from.join("user").get("age"), interval[1]));
						}
					}
					break;
				case GENDER:
					predicates.add(builder.equal(from.join("user").get("gender"), ranking.getFilterValue()));
					break;
				case TYPE:
					predicates.add(builder.equal(from.get("type"), SessionType.values()[Integer.parseInt(ranking.getFilterValue())]));
					break;
				case JOLLY_A:
				case JOLLY_B:
				case JOLLY_C:
					predicates.add(builder.in(from.get("user")).value(getUsersFromFilters(ranking.getFilter(), ranking.getFilterValue(), ranking.getOrganization())));
					break;
			}
		}

		return predicates;
	}

	public List<User> getUsersFromFilters(RankingFilter filter, String filterValue, Organization organization) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<User> query = builder.createQuery(User.class);

		Root<User> userRoot = query.from(User.class);
		ArrayList<Predicate> predicates = new ArrayList<>();

		predicates.add(builder.isNotNull(userRoot.get("username")));

		if(organization != null) {
			Subquery<String> subquery = query.subquery(String.class);
			Root<Enrollment> enrollmentRoot = subquery.from(Enrollment.class);
			subquery.select(enrollmentRoot.get("user"))
					.where(builder.equal(enrollmentRoot.get("organization"), organization));
			predicates.add(builder.in(userRoot.get("uid")).value(subquery));
		}

		if(filter != null) {
			switch(filter) {
				case AGE:
					if(filterValue != null) {
						Integer[] interval = getInterval(filterValue);
						if(interval[0] != null) {
							predicates.add(builder.greaterThanOrEqualTo(userRoot.get("age"), interval[0]));
						}
						if(interval[1] != null) {
							predicates.add(builder.lessThanOrEqualTo(userRoot.get("age"), interval[1]));
						}
					}
					break;
				case GENDER:
					predicates.add(builder.equal(userRoot.get("gender"), filterValue));
					break;
				case JOLLY_A:
				case JOLLY_B:
				case JOLLY_C:
					Subquery<String> subquery = query.subquery(String.class);
					Root<CustomFieldValue> customFieldValueRoot = subquery.from(CustomFieldValue.class);

					subquery.select(customFieldValueRoot.join("enrollment").get("user"))
							.where(builder.and(
									builder.equal(customFieldValueRoot.get("customField"), customFieldService.findByType(organization, filter)),
									builder.equal(customFieldValueRoot.get("value"), true)
							));
					predicates.add(builder.in(userRoot.get("uid")).value(subquery));
					break;
			}
		}

		return entityManager.createQuery(query
				.select(userRoot)
				.where(predicates.toArray(new Predicate[0]))
				.orderBy(builder.asc(userRoot.get("username")))).getResultList();
	}

	public Ranking create(RankingDto dto) {
		Ranking ranking = rankingMapper.dtoToRanking(dto);
		if (ranking.getRepeatType() != null) {
			switch (ranking.getRepeatType()) {
				case NONE:
					ranking.setRepeatNum(0);
					break;
				case MONTH:
					ranking.setEndDate(ranking.getStartDate().plusMonths(1).minusDays(1));
					break;
			}
		}
		return rankingRepository.save(ranking);
	}

	public Ranking update(Ranking old, RankingDto dto) {
		Ranking upd = rankingMapper.dtoToRanking(dto);
		notNullBeanCopy(upd, old, "id");
		return rankingRepository.save(old);
	}

	public void delete(Ranking ranking) {
		rankingRepository.delete(ranking);
	}

	public Boolean validateUser(User user, Organization org, RankingFilter filter, String filterValue) {
		boolean result = true;
		switch(filter) {
			case AGE:
				Integer[] interval = getInterval(filterValue);
				if(interval[0] != null) {
					result = user.getAge() >= interval[0];
				}
				if(interval[1] != null && result) {
					result = user.getAge() <= interval[1];
				}
				break;
			case GENDER:
				result = user.getGender().equalsIgnoreCase(filterValue);
				break;
			case JOLLY_A:
			case JOLLY_B:
			case JOLLY_C:
				result = getUsersFromFilters(filter, filterValue, org).contains(user);
				break;
		}
		return result;
	}

//	@Scheduled(cron = "0/10 * * * * ?") //test
	@Scheduled(cron = "0 0 2 * * ?")
	public void cloneRankingsScheduled() {
		logger.info("Checking expired rankings for repetitions");
		int created = 0, closed = 0;
		for(Ranking ranking : findAllExpired()) {
			if (ranking.getClosed() == null) {
				try {
					// clonazione classifiche scadute
					if (ranking.getRepeatType() != null && ranking.getRepeatNum() > 0 && ranking.getRepeatType() != RankingRepeat.NONE) {
						cloneRanking(ranking);
						created++;
					}
					// chiusura classifiche scadute
					setClosed(ranking);
					closed++;
				} catch (Exception e) {
					logger.error(e.getMessage());
					bugsnag.notify(e, report -> {
							report.addToTab("Ranking info", "id", ranking.getId());
					});
				}
			}
		}
		logger.info("{} new ranking(s) created", created);
		logger.info("{} ranking(s) closed", closed);
	}

	private void cloneRanking(Ranking ranking) {
		RankingDto newRanking;
		newRanking = new RankingDto();
		notNullBeanCopy(rankingMapper.rankingToDto(ranking), newRanking, "id", "startDate", "endDate");
		newRanking.setRepeatNum(ranking.getRepeatNum() - 1);
		switch(ranking.getRepeatType()) {
			case MONTH:
				newRanking.setStartDate(ranking.getStartDate().plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
				newRanking.setEndDate(ranking.getStartDate().plusMonths(2).minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
				break;
			case CUSTOM:
				int diff = ranking.getStartDate().until(ranking.getEndDate()).getDays();
				newRanking.setStartDate(ranking.getEndDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
				newRanking.setEndDate(ranking.getEndDate().plusDays(diff + 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
				break;
		}
		create(newRanking);
	}

	public List<Ranking> findAllByOrganization(Organization org) {
		return rankingRepository.findAllByOrganization(org);
	}

	public List<Ranking> findActiveByOrganization(Organization org) {
		return rankingRepository.findAllByOrganizationAndEndDateGreaterThanEqual(org, LocalDate.now());
	}

	public List<Ranking> findNationals(boolean active) {
		if(active) {
			return rankingRepository.findAllByOrganizationIsNullAndEndDateGreaterThanEqual(LocalDate.now());
		} else {
			return rankingRepository.findAllByOrganizationIsNull();
		}
	}

	public Optional<Ranking> findById(Long rid) {
		return rankingRepository.findById(rid);
	}

	public List<Ranking> findAll(boolean active, boolean withPositions) {
		List<Ranking> rankings;
		if(active) {
			rankings = rankingRepository.findAllByEndDateGreaterThanEqual(LocalDate.now());
		} else {
			rankings = new LinkedList<>();
			rankingRepository.findAll().forEach(rankings::add);
			return rankings;
		}
		if(withPositions) {
			return addRankingPositions(rankings);
		} else {
			return rankings;
		}
	}

	public List<Ranking> findByUser(User user) {
		List<Ranking> rankings = new LinkedList<>();
		Optional<List<Enrollment>> enrollments = enrollmentRepository.findAllEnabledByUser(user.getUid(), LocalDate.now());
		if(enrollments.isPresent() && !enrollments.get().isEmpty()) {
			enrollments.get().stream().map(Enrollment::getOrganization).collect(Collectors.toList()).forEach(o -> rankings.addAll(findAllByOrganization(o)));
		}
		return rankings;
	}

	public List<Ranking> findAllExpired() {
		return rankingRepository.findAllByEndDateLessThanEqual(LocalDate.now());
	}

	public List<Ranking> findAll() {
		List<Ranking> rankings = new LinkedList<>();
		Iterable<Ranking> var10000 = rankingRepository.findAll();
		Objects.requireNonNull(rankings);
		for(Ranking u : var10000) {
			rankings.add(u);
		}
		return rankings;
	}

	public void setAwardAssigned(Ranking ranking) {
		ranking.setAwardsAssigned(LocalDateTime.now());
		rankingRepository.save(ranking);
	}

	public void setClosed(Ranking ranking) {
		ranking.setClosed(LocalDateTime.now());
		rankingRepository.save(ranking);
	}
}
