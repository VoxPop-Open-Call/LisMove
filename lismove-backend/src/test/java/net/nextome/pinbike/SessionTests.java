package net.nextome.lismove;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.SessionController;
import net.nextome.lismove.rest.dto.SessionDto;
import net.nextome.lismove.rest.mappers.SessionMapper;
import net.nextome.lismove.services.*;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Session creating process")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class SessionTests {
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private OrganizationSettingRepository organizationSettingRepository;
	@Autowired
	private SessionController sessionController;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private SettingsService settingsService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private SessionPointService sessionPointService;
	@Autowired
	private SessionMapper sessionMapper;
	@MockBean
	private AchievementService achievementService;
	@Autowired
	private GoogleMapsService googleMapsService;
	@Autowired
	private MockMvc mockMvc;

	private static UUID uuid1, uuid2;

	@BeforeAll
	public static void setup(
			@Autowired UserRepository userRepository,
			@Autowired OrganizationRepository organizationRepository,
			@Autowired CityRepository cityRepository,
			@Autowired HomeAddressRepository homeAddressRepository,
			@Autowired SeatRepository seatRepository,
			@Autowired WorkAddressRepository workAddressRepository,
			@Autowired HomeWorkPathRepository homeWorkPathRepository,
			@Autowired OrganizationSettingRepository organizationSettingRepository
	) {
		cityRepository.save(Generator.generateCity(72006L, "Bari"));

		Organization o = new Organization();
		o.setId(1L);
		o.setType(OrganizationType.COMPANY);
		o.setTitle("Test");
		organizationRepository.save(o);
		organizationSettingRepository.save(new OrganizationSetting("homeWorkPointsTolerance", "0.05"));

		User user = Generator.generateUser("qwerty");
		userRepository.save(user);
		user = Generator.generateUser("sesstest", "cFHVWXpWQ1PPHmYtYqxrhradQKv2");
		userRepository.save(user);

		HomeAddress home = new HomeAddress();
		WorkAddress work = new WorkAddress();
		HomeWorkPath path = new HomeWorkPath();

		home.setAddress("Via G. Maselli");
		home.setNumber("57");
		home.setCity(cityRepository.findById(72006L).get());
		home.setLatitude(40.89124140676849);
		home.setLongitude(16.956067823731892);
		homeAddressRepository.save(home);

		Seat seat = new Seat();
		seat.setAddress("Via Conversano");
		seat.setNumber("84");
		seat.setCity(cityRepository.findById(72006L).get());
		seat.setLatitude(40.919768516956196);
		seat.setLongitude(17.030067879638164);
		seat.setOrganization(o);
		seatRepository.save(seat);

		work.setUser(user);
		work.setSeat(seat);
		work.setStartAssociation(LocalDateTime.now());
		workAddressRepository.save(work);

		path.setUser(user);
		path.setHomeAddress(home);
		path.setSeat(seat);
		path.setDistance(BigDecimal.valueOf(7.6));
		user.setHomeWorkPaths(new HashSet<>(Collections.singletonList(path)));
		user.setHomeAddress(home);

		homeWorkPathRepository.save(path);
		userRepository.save(user);
	}

	@Nested
	@DisplayName("Partial validation testing")
	class partialValidationTest {

		@Test
		public void validateSpeedPartialTest() {
			Partial partial = new Partial();
			partial.setStatus(SessionStatus.VALID);
			partial.setGpsDistance(BigDecimal.valueOf(1));  //1 km in 600sec
			partial.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(1615922671000L), ZoneId.systemDefault()));
			LocalDateTime oldTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1615922071000L), ZoneId.systemDefault());
			assertTrue(sessionService.validateSpeedPartial(partial, oldTime));
		}

		@Test
		public void validateDistancePartialTest() {
			Partial partial = new Partial();
			partial.setStatus(SessionStatus.VALID);
			partial.setGpsDistance(BigDecimal.valueOf(1000));
			partial.setSensorDistance(BigDecimal.valueOf(1900));
			assertTrue(sessionService.validateDistancePartial(partial));
			partial.setStatus(SessionStatus.VALID);
			partial.setSensorDistance(BigDecimal.valueOf(2100));
			assertFalse(sessionService.validateDistancePartial(partial));
		}
	}

	@Test
	@Order(1)
	@DisplayName("Session with speed error")
	public void sessionCreatingSpeedErrorTest() {
		Session session = sessionMapper.dtoToSession(
				SessionTestGenerator.generate(SessionTestGenerator.TestCase.SESSION_WITH_SPEED_ERROR));
		session.setUser(userRepository.findByUsername("qwerty").get());
		sessionService.validateAsync(session);
		uuid1 = session.getId();

		Session sessionSaved = sessionRepository.findById(uuid1).orElse(null);
		assertNotNull(sessionSaved);
		System.out.println(sessionSaved.getPolyline());
		assertAll(
				() -> assertEquals(SessionType.BIKE, session.getType()),
				() -> assertEquals(SessionStatus.SPEED_ERROR, sessionSaved.getStatus())
		);
	}

	@Test
	@Order(2)
	@DisplayName("Revalidate session with speed error")
	public void sessionUpdate1() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
//      This session contains a leg travelled at approx 135 km/h for 5min

		settingsService.set("SPEED_THRESHOLD", "135");
		settingsService.set("TIME_PEAK_THRESHOLD", "4");
		SessionDto s = mapper.readValue(mockMvc.perform(put("/sessions/" + uuid1 + "/validate")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SessionDto.class);
		assertAll(
				() -> assertEquals(0, s.getStatus())
		);
		settingsService.set("SPEED_THRESHOLD", "60");
		settingsService.set("TIME_PEAK_THRESHOLD", "4");
	}

	@Test
	@Order(1)
	@DisplayName("Session with distance error")
	public void sessionCreatingDistanceErrorTest() {
		Session session = sessionMapper.dtoToSession(SessionTestGenerator.generate(SessionTestGenerator.TestCase.SESSION_WITH_DISTANCE_ERROR));
		session.setUser(userRepository.findByUsername("qwerty").get());
		sessionService.validateAsync(session);
		uuid2 = session.getId();

		Session sessionSaved = sessionRepository.findById(uuid2).orElse(null);
		assertNotNull(sessionSaved);
		assertAll(
				() -> assertEquals(SessionType.BIKE, session.getType()),
				() -> assertEquals(SessionStatus.DISTANCE_ERROR, sessionSaved.getStatus())
		);
	}

	@RepeatedTest(2)
	@DisplayName("Valid home-work session")
	public void createHomeWorkSessionTest(RepetitionInfo repetitionInfo) {
		organizationSettingRepository.save(new OrganizationSetting("homeWorkPathTolerancePerc", "1"));
		User user = userRepository.findByUsername("sesstest").get();
		Session session = sessionMapper.dtoToSession(SessionTestGenerator.generate(SessionTestGenerator.TestCase.VALID));

		session.getSessionPoints().get(0).setOrganization(organizationRepository.findById(1L).get());
		session.setUser(user);
//			Revert positions
		if(repetitionInfo.getCurrentRepetition() == 2) {
			Stack<Double> lats = new Stack<>();
			Stack<Double> lngs = new Stack<>();
			lats.addAll(session.getPartials().stream().map(Partial::getLatitude).collect(Collectors.toList()));
			lngs.addAll(session.getPartials().stream().map(Partial::getLongitude).collect(Collectors.toList()));
			session.getPartials().forEach(partial -> {
				partial.setLatitude(lats.pop());
				partial.setLongitude(lngs.pop());
			});
		}

		sessionService.validateAsync(session);
		UUID uuid = session.getId();
		Session s = sessionRepository.findById(uuid).orElse(null);
		assertNotNull(s);
		Optional<HomeWorkPath> p = addressService.getActiveHomeWorkPaths(user).stream().findFirst();
		assertAll(
				() -> assertEquals(SessionStatus.VALID, s.getStatus()),
				() -> assertEquals(calculateGpsDistance(session).setScale(5), s.getGpsDistance().setScale(5)),
				() -> assertEquals(calculateGmapsDistance(session).setScale(5), s.getGmapsDistance().setScale(5)),
				() -> assertTrue(s.getHomeWorkPath()),
				() -> assertEquals(p.get().getHomeAddress().formatAddress(), s.getHomeAddress().formatAddress()),
				() -> assertEquals(p.get().getSeat().formatAddress(), s.getWorkAddress().getSeat().formatAddress())
		);
		System.out.println("Polyline: " + session.getPolyline());
	}

	@Test
	@Order(2)
	@DisplayName("Revalidate session with distance error")
	public void sessionUpdate2() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
//            This session contains a leg travelled at approx 135 km/h for 5min

		settingsService.set("VALID_PARTIAL_DEVIATION", ".65");
		SessionDto s = mapper.readValue(mockMvc.perform(put("/sessions/" + uuid2 + "/validate")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SessionDto.class);
		assertAll(
				() -> assertEquals(0, s.getStatus())
		);
		settingsService.set("VALID_PARTIAL_DEVIATION", ".50");
	}

	@Test
	@DisplayName("Valid session")
	public void sessionCreatingValidTest() {
		Session session = sessionMapper.dtoToSession(SessionTestGenerator.generate(SessionTestGenerator.TestCase.VALID));
		session.setUser(userRepository.findByUsername("qwerty").get());
		session.getSessionPoints().get(0).setOrganization(organizationRepository.findById(1L).get());
		sessionService.validateAsync(session);

		Session sessionSaved = sessionRepository.findById(session.getId()).orElse(null);
		assertNotNull(sessionSaved);
		System.out.println("Polyline: " + session.getPolyline());
		assertAll(
				() -> assertEquals(SessionType.BIKE, session.getType()),
				() -> assertEquals(SessionStatus.VALID, sessionSaved.getStatus()),
				() -> assertEquals(calculateGpsDistance(session).setScale(5), sessionSaved.getGpsDistance().setScale(5)),
				() -> assertEquals(calculateGmapsDistance(session).setScale(5), sessionSaved.getGmapsDistance().setScale(5))
		);
	}

	@Test
	public void createValidSessionWithPauseTest() {
		Session session = sessionMapper.dtoToSession(SessionTestGenerator.generate(SessionTestGenerator.TestCase.VALID_WITH_PAUSE));
		session.setUser(userRepository.findByUsername("qwerty").get());
		session.getSessionPoints().get(0).setOrganization(organizationRepository.findById(1L).get());
		sessionService.validateAsync(session);

		Session sessionSaved = sessionRepository.findById(session.getId()).orElse(null);
		assertNotNull(sessionSaved);
		System.out.println("Polyline: " + session.getPolyline());
		assertAll(
				() -> assertEquals(SessionType.BIKE, session.getType()),
				() -> assertEquals(SessionStatus.VALID, sessionSaved.getStatus()),
				() -> assertEquals(calculateGpsDistance(session).setScale(5), sessionSaved.getGpsDistance().setScale(5)),
				() -> assertEquals(calculateGmapsDistance(session).setScale(5), sessionSaved.getGmapsDistance().setScale(5)),
				() -> assertEquals(2, session.getPolyline().split("€").length)
		);
		session.getPartials().forEach(
				partial -> {
					assertNotNull(partial.getType());
					assertNotNull(partial.getGpsDistance());
					assertNotNull(partial.getGmapsDistance());
				}
		);
	}

	@Test
	void createValidSessionOnFootTest() {
		Session session = sessionMapper.dtoToSession(SessionTestGenerator.generate(SessionTestGenerator.TestCase.VALID_ON_FOOT));
		session.setUser(userRepository.findByUsername("qwerty").get());
		session.getSessionPoints().get(0).setOrganization(organizationRepository.findById(1L).get());
		sessionService.validateAsync(session);

		Session sessionSaved = sessionRepository.findById(session.getId()).orElse(null);
		assertNotNull(sessionSaved);
		System.out.println("Polyline: " + session.getPolyline());
		assertAll(
				() -> assertEquals(SessionType.FOOT, session.getType()),
				() -> assertEquals(SessionStatus.VALID, sessionSaved.getStatus()),
				() -> assertEquals(calculateGpsDistance(session).setScale(5), sessionSaved.getGpsDistance().setScale(5)),
				() -> assertEquals(calculateGmapsDistance(session).setScale(5), sessionSaved.getGmapsDistance().setScale(5))
		);
	}

	@Test
	void createValidSessionAccelerationCheckTest() {
		SessionDto dto = SessionTestGenerator.generate(SessionTestGenerator.TestCase.VALID_ACCELERATION_CHECK);
		Session session = sessionMapper.dtoToSession(dto);
		session.setUser(userRepository.findByUsername("qwerty").get());
		session.getSessionPoints().get(0).setOrganization(organizationRepository.findById(1L).get());
		sessionService.validateAsync(session);

		Session sessionSaved = sessionService.findById(session.getId()).orElse(null);
		System.out.println("Polyline: " + session.getPolyline());
		assertNotNull(sessionSaved);
		assertAll(
				() -> assertEquals(SessionType.BIKE, session.getType()),
				() -> assertEquals(SessionStatus.VALID, sessionSaved.getStatus()),
				() -> assertNotEquals(calculateGpsDistance(session).setScale(5), sessionSaved.getGpsDistance().setScale(5)),
				() -> assertEquals(calculateGmapsDistance(session).setScale(5), sessionSaved.getGmapsDistance().setScale(5)),
				() -> assertNotEquals(googleMapsService.generateWaypointsListResult(session.getPartials()).getPolyline(), sessionSaved.getPolyline())
		);
	}

	@Test
	@Order(2)
	void getSessionTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		SessionDto dto1 = mapper.readValue(mockMvc.perform(get("/sessions/"+uuid1).param("partials", "false")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SessionDto.class);
		SessionDto dto2 = mapper.readValue(mockMvc.perform(get("/sessions/"+uuid1).param("partials", "true")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), SessionDto.class);
		assertAll(
				() -> assertNull(dto1.getPartials()),
				() -> assertNotNull(dto2.getPartials())
		);
	}

	private BigDecimal calculateGpsDistance(Session session) {
		BigDecimal res = BigDecimal.ZERO;
		for(Partial partial : session.getPartials()) {
			if(!sessionService.isStartingPartial(partial)) {
				res = res.add(partial.getGpsDistance());
			}
		}
		return res;
	}

	private BigDecimal calculateGmapsDistance(Session session) {
		BigDecimal res = BigDecimal.ZERO;
		for(Partial partial : session.getPartials()) {
			if(!sessionService.isStartingPartial(partial)) {
				res = res.add(partial.getGmapsDistance());
			}
		}
		return res;
	}

}
