package net.nextome.lismove;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.models.enums.RankingValue;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.RankingPositionDto;
import net.nextome.lismove.services.RankingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DirtiesContext
@DisplayName("Ranking generation tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class RankingTests {

    @Autowired
    private RankingService rankingService;
    @Autowired
    private UserRepository userRepository;

	static Organization org1, org2;
	static User user1, user2, user3;

	@BeforeAll
	static void setup(
			@Autowired CityRepository cityRepository,
			@Autowired UserRepository userRepository,
			@Autowired OrganizationRepository organizationRepository,
			@Autowired SessionRepository sessionRepository,
			@Autowired SessionPointRepository sessionPointRepository,
			@Autowired EnrollmentRepository enrollmentRepository,
			@Autowired CustomFieldRepository customFieldRepository,
			@Autowired CustomFieldValueRepository customFieldValueRepository
	) {
		cityRepository.save(Generator.generateCity(72006L, "Bari"));
		user1 = userRepository.save(Generator.generateLismover("rt_user1", "m", LocalDate.of(2000, 9, 7), BigDecimal.valueOf(30)));
		user2 = userRepository.save(Generator.generateLismover("rt_user2", "f", LocalDate.of(1990, 9, 7), BigDecimal.valueOf(300D)));
		user3 = userRepository.save(Generator.generateLismover("rt_user3", "m", LocalDate.of(2010, 9, 7), BigDecimal.valueOf(3D)));
		org1 = organizationRepository.save(Generator.generateOrganization("Orgc", OrganizationType.COMPANY));
		org2 = organizationRepository.save(Generator.generateOrganization("Orgp", OrganizationType.PA));

		LocalDate startEnrollment = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		LocalDate endEnrollment = LocalDate.of(LocalDate.now().getYear(), 12, 31);
		enrollmentRepository.saveAll(Arrays.asList(
				Generator.generateEnrollment(user1, org1, startEnrollment, endEnrollment),
				Generator.generateEnrollment(user1, org2, startEnrollment, endEnrollment),
				Generator.generateEnrollment(user2, org1, startEnrollment, endEnrollment),
				Generator.generateEnrollment(user2, org2, startEnrollment, endEnrollment),
				Generator.generateEnrollment(user3, org1, startEnrollment, endEnrollment),
				Generator.generateEnrollment(user3, org2, startEnrollment, endEnrollment)
				)
		);
		List<Session> sessions = Arrays.asList(
				Generator.generateSession(user1, LocalDateTime.of(LocalDate.of(2021, 6, 15), LocalTime.now()), new ArrayList<Organization>() {{
					add(org2);
				}}, null, false, BigDecimal.valueOf(5), BigDecimal.valueOf(10), SessionType.ELECTRIC_BIKE),
				Generator.generateSession(user1, LocalDateTime.of(LocalDate.of(2021, 5, 15), LocalTime.now()), new ArrayList<Organization>() {{
					add(org1);
					add(org2);
				}}, null, false, BigDecimal.valueOf(10), BigDecimal.valueOf(20), SessionType.BIKE),
				Generator.generateSession(user2, LocalDateTime.of(LocalDate.of(2021, 6, 15), LocalTime.now()), new ArrayList<Organization>() {{
					add(org1);
				}}, null, true, BigDecimal.valueOf(50), BigDecimal.valueOf(100), SessionType.BIKE),
				Generator.generateSession(user2, LocalDateTime.of(LocalDate.of(2021, 6, 20), LocalTime.now()), new ArrayList<Organization>() {{
					add(org1);
				}}, null, true, BigDecimal.valueOf(50), BigDecimal.valueOf(150), SessionType.BIKE),
				Generator.generateSession(user2, LocalDateTime.of(LocalDate.of(2021, 5, 15), LocalTime.now()), new ArrayList<Organization>() {{
					add(org2);
					add(org1);
				}}, null, false, BigDecimal.valueOf(100), BigDecimal.valueOf(200), SessionType.FOOT),
				Generator.generateSession(user3, LocalDateTime.of(LocalDate.of(2021, 6, 15), LocalTime.now()), new ArrayList<Organization>() {{
					add(org1);
				}}, null, true, BigDecimal.valueOf(0.5), BigDecimal.valueOf(1), SessionType.ELECTRIC_BIKE),
				Generator.generateSession(user3, LocalDateTime.of(LocalDate.of(2021, 5, 15), LocalTime.now()), new ArrayList<Organization>() {{
					add(org2);
				}}, null, false, BigDecimal.valueOf(1), BigDecimal.valueOf(2), SessionType.FOOT)
		);
		sessionRepository.saveAll(sessions);
		sessions.forEach(session -> sessionPointRepository.saveAll(session.getSessionPoints()));

		CustomField cf = new CustomField();
		cf.setOrganization(org1);
		cf.setType(RankingFilter.JOLLY_A);
		customFieldRepository.save(cf);
		customFieldValueRepository.saveAll(Arrays.asList(
				Generator.generateCustomFieldValue(cf, enrollmentRepository.findActiveByUserAndOrganization(user1.getUid(), org1.getId()).get(), true),
				Generator.generateCustomFieldValue(cf, enrollmentRepository.findActiveByUserAndOrganization(user2.getUid(), org1.getId()).get(), false),
				Generator.generateCustomFieldValue(cf, enrollmentRepository.findActiveByUserAndOrganization(user3.getUid(), org1.getId()).get(), true)
		));
	}

	@Test
	void globalRankingTest() {
		Ranking global = rankingService.getGlobal();
		printRankingPositions(global);
		assertAll(
				() -> assertEquals(3, global.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), global.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(300, global.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user1.getUsername(), global.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(30, global.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user3.getUsername(), global.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(3, global.getRankingPositions().get(2).getPoints())
		);
	}

	@Test
	void nationalKmRankingTest() {
		Ranking national = new Ranking();
		national.setStartDate(LocalDate.of(2021, 6, 1));
		national.setEndDate(LocalDate.of(2021, 6, 30));
		national.setOrganization(null);
		national.setValue(RankingValue.NATIONAL_KM);
		rankingService.addRankingPositions(national);

		printRankingPositions(national);
		assertAll(
				() -> assertEquals(3, national.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), national.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(100, national.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user1.getUsername(), national.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(5, national.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user3.getUsername(), national.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(0, national.getRankingPositions().get(2).getPoints())
		);
	}

	@Test
	void nationalPointsRankingTest() {
		Ranking national = new Ranking();
		national.setStartDate(LocalDate.of(2021, 6, 1));
		national.setEndDate(LocalDate.of(2021, 6, 30));
		national.setOrganization(null);
		national.setValue(RankingValue.NATIONAL_POINTS);
		rankingService.addRankingPositions(national);

		printRankingPositions(national);
		assertAll(
				() -> assertEquals(3, national.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), national.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(250, national.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user1.getUsername(), national.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(10, national.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user3.getUsername(), national.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(1, national.getRankingPositions().get(2).getPoints())
		);
	}

//    TESTS ON VALUES

	@Test
	void rankingInitiativePointsTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 6, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.INITIATIVE_POINTS);
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(3, ranking.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(250, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(1, ranking.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(0, ranking.getRankingPositions().get(2).getPoints())
		);
	}

	@Test
	void rankingUrbanKmTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 5, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.URBAN_KM);
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(3, ranking.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(100, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(5, ranking.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(0, ranking.getRankingPositions().get(2).getPoints())
		);
	}

	@Test
	void rankingWorkKmTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 5, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.WORK_KM);
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(3, ranking.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(100, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(0, ranking.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(0, ranking.getRankingPositions().get(2).getPoints())
		);
	}

	@Test
	void rankingWorkNumTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 6, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.WORK_NUM);
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(3, ranking.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(2, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(1, ranking.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(0, ranking.getRankingPositions().get(2).getPoints())
		);
	}

//    TESTS ON FILTERS

    @Test
    void rankingAgeTest() {
        User user = userRepository.findByUid(user1.getUid()).orElse(null); //eseguo la query perché l'età è calcolata dinamicamente
        Ranking ranking = new Ranking();
        ranking.setStartDate(LocalDate.of(2021,5,1));
        ranking.setEndDate(LocalDate.of(2021,6,30));
        ranking.setOrganization(org1);
        ranking.setValue(RankingValue.INITIATIVE_POINTS);
        ranking.setFilter(RankingFilter.AGE);
        ranking.setFilterValue( (user.getAge() - 2) + " - " + (user.getAge() + 2));
        rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(1, ranking.getRankingPositions().size()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(20, ranking.getRankingPositions().get(0).getPoints())
		);
	}

	@Test
	void rankingGenderTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 5, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.INITIATIVE_POINTS);
		ranking.setFilter(RankingFilter.GENDER);
		ranking.setFilterValue("m");
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(2, ranking.getRankingPositions().size()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(20, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(1, ranking.getRankingPositions().get(1).getPoints())
		);
	}

	@Test
	void rankingTypeTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 5, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.INITIATIVE_POINTS);
		ranking.setFilter(RankingFilter.TYPE);
		ranking.setFilterValue(String.valueOf(SessionType.BIKE.ordinal()));
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(3, ranking.getRankingPositions().size()),
				() -> assertEquals(user2.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(250, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(20, ranking.getRankingPositions().get(1).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(2).getUsername()),
				() -> assertEquals(0, ranking.getRankingPositions().get(2).getPoints())
		);
	}

	@Test
	void rankingCustomValueTest() {
		Ranking ranking = new Ranking();
		ranking.setStartDate(LocalDate.of(2021, 5, 1));
		ranking.setEndDate(LocalDate.of(2021, 6, 30));
		ranking.setOrganization(org1);
		ranking.setValue(RankingValue.INITIATIVE_POINTS);
		ranking.setFilter(RankingFilter.JOLLY_A);
		rankingService.addRankingPositions(ranking);

		printRankingPositions(ranking);
		assertAll(
				() -> assertEquals(2, ranking.getRankingPositions().size()),
				() -> assertEquals(user1.getUsername(), ranking.getRankingPositions().get(0).getUsername()),
				() -> assertEquals(20, ranking.getRankingPositions().get(0).getPoints()),
				() -> assertEquals(user3.getUsername(), ranking.getRankingPositions().get(1).getUsername()),
				() -> assertEquals(1, ranking.getRankingPositions().get(1).getPoints())
		);
	}

	@Test
	void rankingRepetitionTest() {
		//TODO
	}

	private static void printRankingPositions(Ranking ranking) {
		System.out.println("\tUser\tPoints");
		for(RankingPositionDto pos : ranking.getRankingPositions()) {
			System.out.println(pos.getPosition() + ".\t" + pos.getUsername() + "\t" + pos.getPoints());
		}
	}
}
