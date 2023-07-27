package net.nextome.lismove;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.AwardType;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.models.enums.RankingValue;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.services.AchievementService;
import net.nextome.lismove.services.AwardService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext
@DisplayName("RankingS/Achievements awards tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class AwardTests {

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
	private AwardService awardService;
	@Autowired
	private AchievementService achievementService;
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private SessionPointRepository sessionPointRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CityRepository cityRepository;

	private static Ranking ranking;
	private static Achievement achievement;
	private static Organization org;
	private static LocalDate startDate, endDate;

	@BeforeAll
	static void setup(
			@Autowired CityRepository cityRepository,
			@Autowired UserRepository userRepository,
			@Autowired RankingRepository rankingRepository,
			@Autowired AchievementRepository achievementRepository,
			@Autowired OrganizationRepository organizationRepository,
			@Autowired EnrollmentRepository enrollmentRepository
	) {
		cityRepository.save(Generator.generateCity(72006L, "Bari"));
		userRepository.saveAll(Arrays.asList(
				Generator.generateLismover("user1", "m", LocalDate.of(2000, 9, 7), BigDecimal.valueOf(30)),
				Generator.generateLismover("user2", "f", LocalDate.of(1990, 9, 7), BigDecimal.valueOf(300D)),
				Generator.generateLismover("user3", "m", LocalDate.of(2010, 9, 7), BigDecimal.valueOf(3D)))
		);

		startDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
		endDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getMonth().minLength());
		org = organizationRepository.save(Generator.generateOrganization("Organizzazione", OrganizationType.COMPANY));
		enrollmentRepository.save(Generator.generateEnrollment(userRepository.findByUsername("user1").get(), org, startDate, endDate));
		ranking = rankingRepository.save(Generator.generateRanking(null, startDate, endDate, RankingValue.NATIONAL_KM, null, null));
		achievement = achievementRepository.save(Generator.generateAchievement(startDate, endDate, org, RankingValue.INITIATIVE_POINTS, BigDecimal.valueOf(500), null, null));
	}

	@Test
	void awardRankingsTest() {
		awardRankingRepository.save(Generator.generateAwardRanking(AwardType.POINTS, BigDecimal.valueOf(1000), ranking, 1));
		awardRankingRepository.save(Generator.generateAwardRanking(AwardType.POINTS, BigDecimal.valueOf(100), ranking, 2));
		awardRankingRepository.save(Generator.generateAwardRanking(AwardType.POINTS, BigDecimal.valueOf(10), ranking, 3));
		awardRankingRepository.save(Generator.generateAwardRanking(AwardType.POINTS, BigDecimal.valueOf(5), ranking, "1 - 999"));

		awardService.assignAwardRankings(ranking);
		List<AwardRanking> list = awardService.findAllByRankingOrderByValue(ranking);

		assertAll(
				() -> assertEquals(4, list.size()),
				() -> assertEquals(ranking.getRankingPositions().get(0).getUsername(), list.get(0).getUser().getUsername()),
				() -> assertEquals(ranking.getRankingPositions().get(1).getUsername(), list.get(1).getUser().getUsername()),
				() -> assertEquals(ranking.getRankingPositions().get(2).getUsername(), list.get(2).getUser().getUsername()),
				() -> assertNotNull(list.get(3).getUser())
		);
	}

	@Test
	void awardAchievementsTest() {
		User user = userRepository.findByUsername("user1").get();
		awardAchievementRepository.save(Generator.generateAwardAchievement(AwardType.POINTS, BigDecimal.valueOf(1000), achievement));

		LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(), LocalDateTime.now().getSecond()).minusDays(2);
		Session session = sessionRepository.save(Generator.generateSession(user, 500D, start, new ArrayList<Organization>() {{
			add(org);
		}}, null));
		sessionPointRepository.saveAll(session.getSessionPoints());
		achievementService.updateAchievements(user);

		List<AwardAchievementUser> list = awardAchievementUserRepository.findAllByAchievementUser_User(user);
		assertAll(
				() -> assertEquals(1, list.size()),
				() -> assertEquals(user.getUsername(), list.get(0).getAchievementUser().getUser().getUsername())
		);
	}

	@Test
	void awardPositionsTest() {
		User user = userRepository.findByUsername("user1").get();
		AwardPosition award = awardService.save(Generator.generateAwardPosition(AwardType.POINTS, BigDecimal.valueOf(1000), startDate, endDate, BigDecimal.valueOf(0.1), "Via Gorizia", "13", cityRepository.findById(72006L).get(), null, null));

		LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		awardService.assignAwardPosition(award, user, timestamp.toInstant(ZoneOffset.UTC).toEpochMilli());

		Optional<AwardPositionUser> a = awardPositionUserRepository.findAllByAwardPosition(award).stream().findFirst();
		assertTrue(a.isPresent());
		assertAll(
				() -> assertNotNull(a.get().getUser()),
				() -> assertNotNull(a.get().getAwardPosition().getLatitude()),
				() -> assertNotNull(a.get().getAwardPosition().getLongitude())
		);
	}

	@Test
	void awardPositionsTestManualCoordinates() {
		User user = userRepository.findByUsername("user1").get();
		AwardPosition award = awardService.save(Generator.generateAwardPosition(AwardType.POINTS, BigDecimal.valueOf(1000), startDate, endDate, BigDecimal.valueOf(0.1), "Via Gorizia", "13", cityRepository.findById(72006L).get(), 41.11903092110878, 16.884132714247794));

		awardService.assignAwardPosition(award, user, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());

		Optional<AwardPositionUser> a = awardPositionUserRepository.findAllByAwardPosition(award).stream().findFirst();
		assertTrue(a.isPresent());
		assertAll(
				() -> assertNotNull(a.get().getUser()),
				() -> assertEquals(a.get().getAwardPosition().getLatitude(), 41.11903092110878),
				() -> assertEquals(a.get().getAwardPosition().getLongitude(), 16.884132714247794)
		);
	}
}
