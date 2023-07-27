package net.nextome.lismove;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.User;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.repositories.OrganizationRepository;
import net.nextome.lismove.repositories.OrganizationSettingRepository;
import net.nextome.lismove.repositories.OrganizationSettingValueRepository;
import net.nextome.lismove.repositories.UserRepository;
import net.nextome.lismove.rest.OrganizationController;
import net.nextome.lismove.rest.dto.ManagerDto;
import net.nextome.lismove.rest.dto.OrganizationDto;
import net.nextome.lismove.rest.dto.OrganizationSettingValueDto;
import net.nextome.lismove.rest.mappers.OrganizationMapper;
import net.nextome.lismove.services.FirebaseAuthService;
import net.nextome.lismove.services.OrganizationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@DisplayName("Organization create/update tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
class OrganizationTests {

	@Autowired
	private OrganizationService organizationService;
	@MockBean
	private FirebaseAuthService firebaseAuthService;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OrganizationSettingRepository organizationSettingRepository;
	@Autowired
	private OrganizationSettingValueRepository organizationSettingRepositoryValue;
	@Autowired
	private OrganizationMapper organizationMapper;
	@Autowired
	private OrganizationController organizationController;

	private static Long oid;

	@Test
	public void createOrganizationCompanyTest() {
		OrganizationDto org = Generator.generateOrganizationDto(1);
		org.setValidation(true);
		org.setValidatorEmail("email@test.test");

		Long oid = organizationService.create(org).getId();
		Optional<Organization> o = organizationRepository.findById(oid);
		assertAll(
				() -> assertTrue(o.isPresent()),
				() -> assertFalse(o.get().getValidation())
		);
	}

	@Test
	@Order(1)
	public void createOrganizationPATest() {
		OrganizationDto org = Generator.generateOrganizationDto(0);
		org.setValidation(true);
		org.setValidatorEmail("email@test.test");

		oid = organizationService.create(org).getId();
		Optional<Organization> o = organizationRepository.findById(oid);
		assertAll(
				() -> assertTrue(o.isPresent()),
				() -> assertTrue(o.get().getValidation())
		);
	}

	@Test
	@Order(2)
	public void updateOrganizationTest() {
		OrganizationDto org = new OrganizationDto();
		org.setLogo("logo");

		organizationService.update(organizationRepository.findById(oid).get(), org);
		Optional<Organization> o = organizationRepository.findById(oid);
		assertAll(
				() -> assertEquals("logo", o.get().getLogo()),
				() -> assertEquals("Organizzazione 0", o.get().getTitle())
		);
	}

	@Test
	@Order(3)
	public void deleteOrganizationTest() {
		organizationService.delete(oid);
		assertFalse(organizationRepository.findById(oid).isPresent());
	}

	@Nested
	@DisplayName("Manager create/update tests")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class managerTests {
		Long oid;
		private ManagerDto managerDto;

		@BeforeAll
		void init() throws FirebaseAuthException {
			OrganizationDto dto = Generator.generateOrganizationDto(0);
			oid = organizationRepository.save(organizationMapper.dtoToOrganization(dto)).getId();
			managerDto = Generator.generateManager("manager");

			UserRecord firebaseUser = FirebaseAuth.getInstance().getUser("EdEx0CA4w3dN9IswoYdeZR9Gmhf1");
			when(firebaseAuthService.createUser(any(User.class), eq("managerPass"))).thenReturn(firebaseUser);
			when(firebaseAuthService.updateUser(any(User.class), eq("managerPass"))).thenReturn(firebaseUser);
		}

		@Test
		@Order(1)
		public void createManagerTest() {
			organizationService.createManager(managerDto, organizationRepository.findById(oid).get());

			Optional<User> m = userRepository.findByUsername("manager");
			assertAll(
					() -> assertTrue(m.isPresent()),
					() -> assertEquals(oid, m.get().getOrganization().getId()),
					() -> assertEquals(UserType.ROLE_MANAGER, m.get().getUserType())
			);
		}

		@Test
		@Order(2)
		public void updateManagerTest() {
			managerDto.setFirstName("fname2");
			managerDto.setLastName("lname2");

			organizationService.updateManager(userRepository.findByUsername("manager").get(), managerDto);

			Optional<User> m = userRepository.findByUsername("manager");
			assertAll(
					() -> assertTrue(m.isPresent()),
					() -> assertEquals(oid, m.get().getOrganization().getId()),
					() -> assertEquals(UserType.ROLE_MANAGER, m.get().getUserType()),
					() -> assertEquals("fname2", m.get().getFirstName()),
					() -> assertEquals("lname2", m.get().getLastName())
			);
		}

		@Test
		@Order(3)
		public void getManagersTest() {
			Set<User> u = organizationService.findAllManagers(organizationRepository.findById(oid).get());
			assertAll(
					() -> assertEquals(1, u.size()),
					() -> assertTrue(u.stream().allMatch(user -> user.getUserType().equals(UserType.ROLE_MANAGER)))
			);
		}

	}

	//TODO testare organizzazioni PA con validazione true/false e validatorEmail

	@Nested
	@DisplayName("Settings create/update tests")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class settingsTests {

		private Long oid;

		@BeforeAll
		void setup(@Autowired OrganizationRepository organizationRepository) {
			oid = organizationRepository.save(Generator.generateOrganization("companyTest", OrganizationType.COMPANY)).getId();
		}

		@Test
		@Order(1)
		public void saveSettingsTest() {
			organizationController.saveSettings(oid, Arrays.asList(
					Generator.generateOrganizationSetting(null, "setting_1", "def_value_1"),
					Generator.generateOrganizationSetting(null, "setting_2", "def_value_2")
			));

			assertAll(
					() -> assertEquals(organizationSettingRepository.findById("setting_1").get().getDefaultValue(), "def_value_1"),
					() -> assertEquals(organizationSettingRepositoryValue.findByOrganizationIdAndOrganizationSettingName(oid, "setting_1").get().getValue(), "def_value_1")
			);
		}

		@Test
		@Order(2)
		public void updateSettingsTest() {
			organizationService.save(Arrays.asList(
					Generator.generateOrganizationSetting(oid, "setting_1", "new_value_1")
			));

			assertAll(
					() -> assertEquals(organizationSettingRepository.findById("setting_1").get().getDefaultValue(), "def_value_1"),
					() -> assertEquals(organizationSettingRepositoryValue.findByOrganizationIdAndOrganizationSettingName(oid, "setting_1").get().getValue(), "new_value_1")
			);
		}

		@Test
		@Order(3)
		public void getSettingsTest() {
			List<OrganizationSettingValueDto> settings = organizationController.getSettings(oid);

			OrganizationSettingValueDto setting1 = settings.stream().filter(dto -> dto.getOrganizationSetting().equals("setting_1")).findFirst().get();
			OrganizationSettingValueDto setting2 = settings.stream().filter(dto -> dto.getOrganizationSetting().equals("setting_2")).findFirst().get();
			assertAll(
					() -> assertEquals(settings.size(), 2),
					() -> assertEquals(setting1.getDefaultValue(), "def_value_1"),
					() -> assertEquals(setting1.getValue(), "new_value_1"),
					() -> assertEquals(setting2.getDefaultValue(), "def_value_2"),
					() -> assertEquals(setting2.getValue(), "def_value_2")
			);
		}
	}
}
