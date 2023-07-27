package net.nextome.lismove;

import com.google.maps.errors.ApiException;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.Partial;
import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.repositories.OrganizationRepository;
import net.nextome.lismove.repositories.SessionRepository;
import net.nextome.lismove.repositories.UserRepository;
import net.nextome.lismove.rest.mappers.SessionMapper;
import net.nextome.lismove.services.GoogleMapsService;
import net.nextome.lismove.services.SessionService;
import net.nextome.lismove.services.utils.WaypointsList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class SessionWithoutGmapsTests {

	@Autowired
	private SessionMapper sessionMapper;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private UserRepository userRepository;
	@MockBean
	private GoogleMapsService googleMapsService;

	@BeforeAll
	public static void setup(
			@Autowired GoogleMapsService googleMapsService,
			@Autowired UserRepository userRepository) throws IOException, InterruptedException, ApiException {
		doThrow(new IOException()).when(googleMapsService).generateWaypointsListResult(any(WaypointsList.class));
		userRepository.save(Generator.generateUser("sestest"));
	}

	@AfterAll
	public static void setdown(@Autowired GoogleMapsService googleMapsService) {
		reset(googleMapsService);
	}

	@Test
	@DisplayName("Valid session without GMaps")
	public void createSessionWithoutGmapsTest() {
		Session session = sessionMapper.dtoToSession(SessionTestGenerator.generate(SessionTestGenerator.TestCase.VALID));
		Organization o = new Organization();
		o.setType(OrganizationType.COMPANY);
		o.setTitle("Test");
		organizationRepository.save(o);

		session.setUser(userRepository.findByUsername("sestest").get());
		session.getSessionPoints().get(0).setOrganization(o);
		sessionService.validateAsync(session);

		BigDecimal sumGps = BigDecimal.ZERO;
		for(Partial partial : session.getPartials()) {
			sumGps = sumGps.add(partial.getGpsDistance());
		}

		BigDecimal finalGpsSum = sumGps;
		Optional<Session> s = sessionRepository.findById(session.getId());
		assertAll(
				() -> assertTrue(s.isPresent()),
				() -> assertEquals(SessionStatus.NOT_CERTIFICATED, s.get().getStatus()),
				() -> assertEquals(finalGpsSum.setScale(5), s.get().getGpsDistance().setScale(5)),
				() -> assertEquals(0D, s.get().getGmapsDistance().doubleValue()),
				() -> assertEquals("", s.get().getPolyline())
		);
		System.out.println("Polyline: " + session.getPolyline());
	}
}
