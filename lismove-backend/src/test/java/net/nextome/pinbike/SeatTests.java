package net.nextome.lismove;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.LismoverUserDto;
import net.nextome.lismove.rest.dto.SeatDto;
import net.nextome.lismove.services.AddressService;
import net.nextome.lismove.services.EmailService;
import net.nextome.lismove.services.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.context.Context;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@DisplayName("Seat tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class SeatTests {

	@Autowired
	private UserService userService;
	@MockBean
	private EmailService emailService;
	@Spy
	@Autowired
	private AddressService addressService;
	@Autowired
	private SeatRepository seatRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private HomeWorkPathRepository homeWorkPathRepository;
	@Autowired
	private HomeAddressRepository homeAddressRepository;
	@Autowired
	private WorkAddressRepository workAddressRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private CityRepository cityRepository;

	private static final Long oid = 1L;
	private static Long sid, pid, wid;

	@BeforeAll
	public static void setup(
			@Autowired CityRepository cityRepository,
			@Autowired AddressService addressService,
			@Autowired HomeAddressRepository homeAddressRepository,
			@Autowired UserRepository userRepository,
			@Autowired OrganizationRepository organizationRepository
	) {
		cityRepository.save(Generator.generateCity(72006L, "Bari"));

		HomeAddress home = new HomeAddress();
		home.setAddress("Via Dante Alighieri");
		home.setNumber("5");
		home.setCity(cityRepository.findById(72006L).get());
		addressService.calculateLatLng(home);
		homeAddressRepository.save(home);

		User user = Generator.generateUser("user");
		user.setHomeAddress(home);
		userRepository.save(user);

		Organization org = new Organization();
		org.setId(oid);
		org.setType(OrganizationType.PA);
		org.setValidation(true);
		org.setTitle("Title");
		organizationRepository.save(org);
	}

	@Test
	@Order(1)
	public void createSeatTest() {
		SeatDto dto = new SeatDto();
		dto.setName("Sede 1");
		dto.setAddress("Via Gorizia");
		dto.setNumber("12");
		dto.setCity(72006L);
		sid = addressService.createSeat(organizationRepository.findById(1L).get(), dto).getId();

		User user = userRepository.findByUsername("user").get();
		Seat seat = seatRepository.findById(sid).orElse(null);
		HomeAddress h = Generator.generateHomeAddress("Via Niccolò Pizzoli", "55", cityRepository.findById(72006L).get(), 41.127732052482656, 16.86077523674286);
		h.setUser(userRepository.findByUsername("user").get());
		homeAddressRepository.save(h);

		HomeWorkPath path = new HomeWorkPath();
		path.setUser(user);
		path.setHomeAddress(h);
		path.setSeat(seat);

		pid = homeWorkPathRepository.save(path).getId();

		Optional<Seat> s = seatRepository.findById(sid);
		assertAll(
				() -> assertTrue(s.isPresent()),
				() -> assertFalse(s.get().getDeleted()),
				() -> assertNotNull(s.get().getLatitude()),
				() -> assertNotNull(s.get().getLongitude()),
				() -> assertEquals(oid, s.get().getOrganization().getId())
		);
	}

	@Test
	@Order(1)
	public void createSeatManualCoordinatesTest() {
		SeatDto dto = new SeatDto();
		dto.setName("Sede 2");
		dto.setAddress("Via Gorizia");
		dto.setNumber("12");
		dto.setCity(72006L);
		dto.setLatitude(41.11897);
		dto.setLongitude(16.88430);
		Long sid = addressService.createSeat(organizationRepository.findById(1L).get(), dto).getId();

		Optional<Seat> s = seatRepository.findById(sid);
		assertAll(
				() -> assertTrue(s.isPresent()),
				() -> assertFalse(s.get().getDeleted()),
				() -> assertEquals(41.11897, s.get().getLatitude()),
				() -> assertEquals(16.88430, s.get().getLongitude()),
				() -> assertEquals(oid, s.get().getOrganization().getId())
		);
	}

	@Test
	@Order(2)
	public void updateSeatTest() {
		Seat old = seatRepository.findById(sid).get();
		SeatDto upd = new SeatDto();
		upd.setAddress("Via Gorizia");
		upd.setNumber("13");
		upd.setCity(72006L);

		Seat s = addressService.updateSeat(old, upd);
		sid = s.getId();
		HomeWorkPath p = homeWorkPathRepository.findById(pid).get();
		assertAll(
				() -> assertNotNull(s.getLatitude()),
				() -> assertNotNull(s.getLongitude()),
				() -> assertEquals(s.getId(), p.getSeat().getId()),
				() -> assertTrue(old.getDeleted())
		);
		workAddressRepository.findBySeat(old).forEach(workAddress -> {
			assertNotNull(workAddress.getEndAssociation());
		});
		workAddressRepository.findBySeat(s).forEach(workAddress -> {
			assertNull(workAddress.getEndAssociation());
		});
	}

	@Test
	@Order(3)
	public void updateSeatAddressAndCoordinatesTest() {
		Seat old = seatRepository.findById(sid).orElse(null);
		SeatDto upd = new SeatDto();
		upd.setAddress("Via Gorizia");
		upd.setNumber("13");
		upd.setCity(72006L);
		upd.setLatitude(41.11988);
		upd.setLongitude(16.88450);

		Seat s = addressService.updateSeat(old, upd);
		sid = s.getId();
		HomeWorkPath p = homeWorkPathRepository.findById(pid).get();
		assertAll(
				() -> assertNotNull(s.getLatitude()),
				() -> assertNotNull(s.getLongitude()),
				() -> assertEquals(p.getSeat().getId(), s.getId()),
				() -> assertEquals(41.11988, s.getLatitude()),
				() -> assertEquals(16.88450, s.getLongitude()),
				() -> assertTrue(old.getDeleted())
		);
	}

	@Test
	@Order(4)
	public void updateSeatCoordinatesTest() {
		Seat old = seatRepository.findById(sid).orElse(null);
		SeatDto upd = new SeatDto();
		upd.setLatitude(41.11960);
		upd.setLongitude(16.88470);

		Seat s = addressService.updateSeat(old, upd);
		sid = s.getId();
		HomeWorkPath p = homeWorkPathRepository.findById(pid).get();
		assertAll(
				() -> assertNotNull(s.getLatitude()),
				() -> assertNotNull(s.getLongitude()),
				() -> assertEquals(p.getSeat().getId(), s.getId()),
				() -> assertEquals("Via Gorizia", s.getAddress()),
				() -> assertEquals("13", s.getNumber()),
				() -> assertEquals(72006L, s.getCity().getIstatId()),
				() -> assertEquals(41.11960, s.getLatitude()),
				() -> assertEquals(16.88470, s.getLongitude()),
				() -> assertTrue(old.getDeleted())
		);
	}

	@Test
	@Order(5)
	public void deleteSeatTest() {
		WorkAddress w = new WorkAddress();
		w.setUser(userRepository.findByUsername("user").get());
		w.setSeat(seatRepository.findById(sid).get());
		wid = workAddressRepository.save(w).getId();

		addressService.deleteSeat(seatRepository.findById(sid).get());

		Optional<Seat> s = seatRepository.findById(sid);
		assertAll(
				() -> assertTrue(s.isPresent()),
				() -> assertTrue(s.get().getDeleted()),
				() -> assertNotNull(workAddressRepository.findById(wid).get().getEndAssociation()),
				() -> assertNull(homeWorkPathRepository.findById(pid).orElse(null))
		);
	}

	@Test
	void requestSeatTest() {
		Long sid;
		SeatDto dto = new SeatDto();
		User user = userRepository.findByUsername("user").get();
		LismoverUserDto userDto = new LismoverUserDto();
		dto.setAddress("Via Re David");
		dto.setNumber("100");
		dto.setOrganization(oid);
		dto.setCity(72006L);

//            Request
		doNothing().when(emailService).send(anyString(), anyString(), anyString(), any(Context.class));
		sid = addressService.requestSeat(user, dto).getId();
		dto.setId(sid);
		userDto.setWorkAddresses(new HashSet<SeatDto>() {{
			add(dto);
		}});
		userService.updateLismover(user, userDto);

		Optional<Seat> s1 = seatRepository.findById(sid);
		assertAll(
				() -> assertTrue(s1.isPresent()),
				() -> assertNull(s1.get().getValidated()),
				() -> assertFalse(homeWorkPathRepository.findByUser(user).stream().anyMatch(homeWorkPath -> homeWorkPath.getSeat().equals(s1.get())))
		);

//            Validation
		addressService.approveSeat(s1.get());
		userDto.setWorkAddresses(new HashSet<SeatDto>() {{
			add(dto);
		}});
		userService.updateLismover(user, userDto);

		Optional<Seat> s2 = seatRepository.findById(sid);
		assertTrue(s2.isPresent());
		assertTrue(s2.get().getValidated());
		Set<HomeWorkPath> byUser = homeWorkPathRepository.findByUser(user);
		assertTrue(byUser.stream().anyMatch(homeWorkPath -> homeWorkPath.getSeat().equals(s2.get())));
	}
}
