package net.nextome.lismove;

import net.nextome.lismove.models.*;
import net.nextome.lismove.repositories.SessionPointRepository;
import net.nextome.lismove.services.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class SessionPointTests {

	@Autowired
	private SessionService sessionService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private AwardService awardService;
	@Autowired
	private SessionValidatorService sessionValidatorService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private SessionPointRepository sessionPointRepository;
	@Autowired
	private SessionPointService sessionPointService;

	@BeforeAll
	static void initDatabase(@Autowired DataSource dataSource) {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource("sql/session_point-test-data.sql"));
		populator.execute(dataSource);
	}

	@Test
	@Order(1)
	public void nationalPointsTest() {
		Session session = sessionService.findById(UUID.fromString("123e4567-7c32-4e4d-a105-1536a6120000")).get();
		sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());
		sessionService.save(session);

		User u = userService.findByUid("U53R0007WDXuDp5WAH0f97mUwxH2").get();
		assertAll(
				() -> assertEquals(23D, Optional.ofNullable(u.getPoints()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(23D, Optional.ofNullable(u.getEarnedNationalPoints()).orElse(BigDecimal.ZERO).doubleValue())
		);
	}

	@Test
	@Order(1)
	public void urbanPointsTest() { //TODO o initiative?
		Organization pa = organizationService.findById(1L).get();
		Organization comp = organizationService.findById(2L).get();
		organizationSettingsService.set("isActiveUrbanPoints", "true", pa);
		organizationSettingsService.set("isActiveUrbanPoints", "true", comp);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "false", pa);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "false", comp);
		organizationSettingsService.set("isActiveHomeWorkRefunds", "false", pa);

		Session session = sessionService.findById(UUID.fromString("123e4567-7c32-4e4d-a105-1536a6120001")).get();
		session.setSessionPoints(sessionPointRepository.findBySession(session)); // prevent hibernate lazy loading exception
		sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());
		sessionService.assignPoints(session);
		sessionService.save(session);

		User u = userService.findByUid("U53R0017WDXuDp5WAH0f97mUwxH2").get();
		Enrollment e1 = organizationService.findActiveByUserAndOrganization(u, pa).get();
		Enrollment e2 = organizationService.findActiveByUserAndOrganization(u, comp).get();
		assertAll(
				() -> assertEquals(23D, Optional.ofNullable(u.getPoints()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(23D, Optional.ofNullable(u.getEarnedNationalPoints()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(23D, e1.getPoints().doubleValue()),
				() -> assertEquals(10D, e2.getPoints().doubleValue())

		);
	}

	@Test
	@Order(2)
	public void urbanPointsWithEuroRefundTest() {
		Organization pa = organizationService.findById(1L).get();
		Organization comp = organizationService.findById(2L).get();
		organizationSettingsService.set("isActiveUrbanPoints", "true", pa);
		organizationSettingsService.set("isActiveUrbanPoints", "true", comp);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "true", pa);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "true", comp);
		organizationSettingsService.set("isActiveHomeWorkRefunds", "false", pa);
		organizationSettingsService.set("euroValueKmUrbanPathBike", "0.02", pa);
		organizationSettingsService.set("euroValueKmUrbanPathBike", "0.04", comp);

		Session session = sessionService.findById(UUID.fromString("123e4567-7c32-4e4d-a105-1536a6120002")).get();
		session.setSessionPoints(sessionPointRepository.findBySession(session)); // prevent hibernate lazy loading exception
		sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());
		sessionService.assignPoints(session);
		sessionService.save(session);

		User u = userService.findByUid("U53R0017WDXuDp5WAH0f97mUwxH2").get();
		Enrollment e1 = organizationService.findActiveByUserAndOrganization(u, pa).get();
		Enrollment e2 = organizationService.findActiveByUserAndOrganization(u, comp).get();
		List<SessionPoint> sp = sessionPointService.findBySession(session);
		assertAll(
				// controllo punti (test 1 + test 2)
				() -> assertEquals(0.04D, sp.stream().filter(p -> p.getOrganization().getId().equals(pa.getId())).findFirst().get().getEuro().doubleValue()),
				() -> assertEquals(0.02D, sp.stream().filter(p -> p.getOrganization().getId().equals(comp.getId())).findFirst().get().getEuro().doubleValue()),
				() -> assertEquals(23D + 23D, e1.getPoints().doubleValue()),
				() -> assertEquals(10D + 5D, e2.getPoints().doubleValue()),
				() -> assertEquals(23D + 23D, Optional.ofNullable(u.getPoints()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(23D + 23D, Optional.ofNullable(u.getEarnedNationalPoints()).orElse(BigDecimal.ZERO).doubleValue()),
//				controllo euro
				() -> assertEquals(0.04D, e1.getEuro().doubleValue()),
				() -> assertEquals(0.02D, e2.getEuro().doubleValue()),
				() -> assertEquals(0.06D, Optional.ofNullable(u.getEuro()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D, Optional.ofNullable(u.getTotalMoneyEarned()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D, Optional.ofNullable(u.getTotalMoneyRefundNotHomeWork()).orElse(BigDecimal.ZERO).doubleValue())
		);
	}

	@Test
	@Order(3)
	public void homeWorkPointsWithUrbanPointsRefundTest() {
		Organization pa = organizationService.findById(1L).get();
		Organization comp = organizationService.findById(2L).get();
		organizationSettingsService.set("isActiveUrbanPoints", "true", pa);
		organizationSettingsService.set("isActiveUrbanPoints", "true", comp);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "false", pa);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "false", comp);
		organizationSettingsService.set("isActiveHomeWorkRefunds", "true", pa);
		organizationSettingsService.set("valueKmHomeWorkBike", "0.30", pa);
		organizationSettingsService.set("homeWorkRefundType", "urbanPoints", pa);
		organizationSettingsService.set("homeWorkPathTolerancePerc", "10", pa);

		Session session = sessionService.findById(UUID.fromString("123e4567-7c32-4e4d-a105-1536a6120003")).get();
		session.setSessionPoints(sessionPointRepository.findBySession(session)); // prevent hibernate lazy loading exception
		sessionService.save(session);
		sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());
		sessionService.assignPoints(session);

		User u = userService.findByUid("U53R0017WDXuDp5WAH0f97mUwxH2").get();
		Enrollment e1 = organizationService.findActiveByUserAndOrganization(u, pa).get();
		Enrollment e2 = organizationService.findActiveByUserAndOrganization(u, comp).get();
		List<AwardCustomUser> ac = awardService.findAwardCustomUsersByUser(u);
		assertAll(
				() -> assertEquals(2, session.getSessionPoints().size()),
				// controllo che ci sia un parziale di tipo casa-lavoro
				() -> assertEquals(1, session.getSessionPoints().stream().filter(sp -> sessionPointService.isHomeWork(sp) && sp.getOrganization().getId().equals(pa.getId())).count()),
				// controllo punti (test (1+2) + test 3)
				() -> assertEquals(46D + 46D + 1D, e1.getPoints().doubleValue()),
				() -> assertEquals(15D + 46D, e2.getPoints().doubleValue()),
				() -> assertEquals(1, ac.size()),
				() -> assertEquals(pa.getId(), ac.get(0).getAwardCustom().getOrganization().getId()),
				() -> assertEquals(1, ac.get(0).getAwardCustom().getValue().doubleValue()),
				() -> assertEquals(46D + 46D, u.getPoints().doubleValue()),
				// controllo euro (test 2 + test 3)
				() -> assertEquals(0.04D + 0D, e1.getEuro().doubleValue()),
				() -> assertEquals(0.02D + 0D, e2.getEuro().doubleValue()),
				() -> assertEquals(0.06D + 0D, Optional.ofNullable(u.getEuro()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D + 0D, Optional.ofNullable(u.getTotalMoneyEarned()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.00D + 0D, Optional.ofNullable(u.getTotalMoneyRefundHomeWork()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D + 0D, Optional.ofNullable(u.getTotalMoneyRefundNotHomeWork()).orElse(BigDecimal.ZERO).doubleValue())
		);
	}

	@Test
	@Order(4)
	public void homeWorkPointsWithEuroRefundTest() {
		Organization pa = organizationService.findById(1L).get();
		Organization comp = organizationService.findById(2L).get();
		organizationSettingsService.set("isActiveUrbanPoints", "true", pa);
		organizationSettingsService.set("isActiveUrbanPoints", "true", comp);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "false", pa);
		organizationSettingsService.set("isActiveUrbanPathRefunds", "false", comp);
		organizationSettingsService.set("isActiveHomeWorkRefunds", "true", pa);
		organizationSettingsService.set("valueKmHomeWorkBike", "0.30", pa);
		organizationSettingsService.set("homeWorkRefundType", "euro", pa);
		organizationSettingsService.set("homeWorkPathTolerancePerc", "10", pa);

		Session session = sessionService.findById(UUID.fromString("123e4567-7c32-4e4d-a105-1536a6120004")).get();
		session.setSessionPoints(sessionPointRepository.findBySession(session)); // prevent hibernate lazy loading exception
		sessionService.save(session);
		sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());
		sessionService.assignPoints(session);

		User u = userService.findByUid("U53R0017WDXuDp5WAH0f97mUwxH2").get();
		Enrollment e1 = organizationService.findActiveByUserAndOrganization(u, pa).get();
		Enrollment e2 = organizationService.findActiveByUserAndOrganization(u, comp).get();
		assertAll(
				() -> assertEquals(2, session.getSessionPoints().size()),
				// controllo che ci sia un parziale di tipo casa-lavoro
				() -> assertEquals(1, session.getSessionPoints().stream().filter(sp -> sessionPointService.isHomeWork(sp) && sp.getOrganization().getId().equals(pa.getId())).count()),
				// controllo punti (test (1+2+3) + test 4)
				() -> assertEquals(93D + 46D, e1.getPoints().doubleValue()),
				() -> assertEquals(61D + 46D, e2.getPoints().doubleValue()),
				() -> assertEquals(92D + 46D, u.getPoints().doubleValue()),
				// controllo euro (test (2+3) + test 4)
				() -> assertEquals(0.04D + 1.39D, e1.getEuro().doubleValue()),
				() -> assertEquals(0.02D + 0D, e2.getEuro().doubleValue()),
				() -> assertEquals(0.06D + 1.39D, Optional.ofNullable(u.getEuro()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D + 1.39D, Optional.ofNullable(u.getTotalMoneyEarned()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.00D + 1.39D, Optional.ofNullable(u.getTotalMoneyRefundHomeWork()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D + 0D, Optional.ofNullable(u.getTotalMoneyRefundNotHomeWork()).orElse(BigDecimal.ZERO).doubleValue())
		);
	}

	@Test
	@Order(5)
	public void homeWorkPointsWithEuroRefundAndDayLimitsCheckTest() {
		Organization pa = organizationService.findById(1L).get();
		Organization comp = organizationService.findById(2L).get();
		organizationSettingsService.set("euroMaxRefundInADay", "2", pa);
		organizationSettingsService.set("euroMaxRefundInAMonth", null, pa);
		organizationSettingsService.set("euroMaxRefundInATime", null, pa);

		Session session = sessionService.findById(UUID.fromString("123e4567-7c32-4e4d-a105-1536a6120004")).get();
		session.setSessionPoints(sessionPointRepository.findBySession(session)); // prevent hibernate lazy loading exception
		sessionService.save(session);
		sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());
		sessionService.assignPoints(session);

		User u = userService.findByUid("U53R0017WDXuDp5WAH0f97mUwxH2").get();
		Enrollment e1 = organizationService.findActiveByUserAndOrganization(u, pa).get();
		Enrollment e2 = organizationService.findActiveByUserAndOrganization(u, comp).get();
		assertAll(
				() -> assertEquals(2, session.getSessionPoints().size()),
				// controllo che ci sia un parziale di tipo casa-lavoro
				() -> assertEquals(1, session.getSessionPoints().stream().filter(sp -> sessionPointService.isHomeWork(sp) && sp.getOrganization().getId().equals(pa.getId())).count()),
				// controllo punti (test (1+2+3+4) + test 5)
				() -> assertEquals(139D + 46D, e1.getPoints().doubleValue()),
				() -> assertEquals(107D + 46D, e2.getPoints().doubleValue()),
				() -> assertEquals(138D + 46D, u.getPoints().doubleValue()),
				// controllo euro (test (2+3+4) + test 5)
				() -> assertEquals(1.43D + (2D - 1.43D), e1.getEuro().doubleValue()),
				() -> assertEquals(0.02D + 0D, e2.getEuro().doubleValue()),
				() -> assertEquals(1.45D + (2D - 1.43D), Optional.ofNullable(u.getEuro()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(1.45D + (2D - 1.43D), Optional.ofNullable(u.getTotalMoneyEarned()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(1.39D + (2D - 1.43D), Optional.ofNullable(u.getTotalMoneyRefundHomeWork()).orElse(BigDecimal.ZERO).doubleValue()),
				() -> assertEquals(0.06D + 0D, Optional.ofNullable(u.getTotalMoneyRefundNotHomeWork()).orElse(BigDecimal.ZERO).doubleValue())
		);
	}
}
