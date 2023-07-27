package net.nextome.lismove.rest;

import io.swagger.annotations.ApiOperation;
import net.nextome.lismove.config.SecurityServiceProperties;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.integrations.autodata.CarDataService;
import net.nextome.lismove.models.*;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.rest.mappers.*;
import net.nextome.lismove.security.NextomeUserDetails;
import net.nextome.lismove.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private RankingService rankingService;
	@Autowired
	private AchievementService achievementService;
	@Autowired
	private SensorService sensorService;
	@Autowired
	private AwardService awardService;
	@Autowired
	private ShopService shopService;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private SensorMapper sensorMapper;
	@Autowired
	private SessionMapper sessionMapper;
	@Autowired
	private SeatMapper seatMapper;
	@Autowired
	private EnrollmentMapper enrollmentMapper;
	@Autowired
	private RankingMapper rankingMapper;
	@Autowired
	private AchievementsMapper achievementsMapper;
	@Autowired
	private AwardMapper awardMapper;
	@Autowired
	private AddressMapper addressMapper;
	@Autowired
	private NotificationMessageMapper notificationMessageMapper;
	@Autowired
	private SecurityServiceProperties serviceProperties;
	@Autowired
	private CarDataService carDataService;
	@Autowired
	private NotificationMessageService notificationMessageService;
	@Autowired
	private FirebaseAuthService firebaseAuthService;

	@PostMapping
	public LismoverUserDto create(@RequestBody LismoverUserDto dto, @ApiIgnore HttpServletRequest request) {
		if(dto.getUid() == null) {
			throw new LismoveException("Missing uid", HttpStatus.BAD_REQUEST);
		}
		String token = request.getHeader(serviceProperties.getJwt_header());
		firebaseAuthService.checkToken(token, dto.getUid());

		User u = userService.createLismover(dto);
		return userMapper.userToDto(u);
	}

	@ApiOperation(value = "update", notes = "Aggiorna i dati dell’utente identificato dall'uid spedificato.<br>Per aggiornare le sedi di lavoro attualmente attive, valorizzare il campo workAddresses con una lista dei soli id delle sedi selezionate.")
	@PutMapping("{uid}")
	public LismoverUserDto update(@PathVariable("uid") String uid, @RequestBody LismoverUserDto userDto) {
		User old = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		old = userService.updateLismover(old, userDto);
		return userMapper.userToDto(old);
	}

	@GetMapping("{uid}")
	public LismoverUserDto get(@PathVariable("uid") String uid) {
		return userMapper.userToDto(userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND)));
	}

	@GetMapping("{email}/profile")
	public Map<String, String> getProfile(@PathVariable("email") String email) {
		User user = userService.findByEmail(email).orElseThrow(() -> new LismoveException(""));
		Map<String, String> out = new HashMap<>();
		out.put("uid", user.getUid());
		out.put("firstName", user.getFirstName());
		out.put("lastName", user.getLastName());
		return out;
	}

	@GetMapping("{email}/exists")
	public boolean exists(@PathVariable("email") String email) {
		return userService.findByEmail(email).isPresent();
	}

	@GetMapping("{email}/reset-password")
	public boolean resetPassword(@PathVariable("email") String email) {
		return userService.findByEmail(email).isPresent() && Optional.ofNullable(userService.findByEmail(email).get().getResetPasswordRequired()).orElse(false);
	}

	@GetMapping()
	public List<UserOverviewDto> list(Authentication authentication) {
		User user = ((NextomeUserDetails) authentication.getPrincipal()).getUserData();
		return userService.getOverview(user.getUserType(), user.getOrganization());
	}

	@PostMapping("{uid}/sensor")
	public SensorDto saveSensor(@PathVariable("uid") String uid, @RequestBody SensorDto sensorDto) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		Sensor sensor = sensorMapper.dtoToSensor(sensorDto);
		sensor.setUser(user);
		sensor = sensorService.saveSensor(sensor);
		return sensorMapper.sensorToDto(sensor);
	}

	@GetMapping("{uid}/sensor")
	public List<SensorDto> getSensors(@PathVariable("uid") String uid, @RequestParam(defaultValue = "false", required = false) Boolean active) {
		userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return sensorMapper.sensorToDto(sensorService.findByUser(uid, active));
	}

	@DeleteMapping("{uid}/sensor/{uuid}")
	public void disassociateSensor(@PathVariable("uid") String uid, @PathVariable("uuid") String uuid) {
		userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		sensorService.endSensorAssociation(uuid);
	}

	@GetMapping("{uid}/sensor/{uuid}/stolen")
	public SensorDto setStolen(@PathVariable("uid") String uid, @PathVariable("uuid") String uuid) {
		userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return sensorMapper.sensorToDto(sensorService.setStolen(uuid));
	}

	@GetMapping("{uid}/sessions")
	public List<SessionDto> getUserSessions(@PathVariable("uid") String uid) {
		User u = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		List<SessionDto> out = new ArrayList<>();
		sessionService.findByUser(u.getUid()).forEach(session -> {
			out.add(sessionMapper.sessionToDtoWithoutPartials(session));
		});
		return out;
	}

	@GetMapping("{uid}/enrollments")
	public List<EnrollmentDto> getEnrollments(@PathVariable("uid") String uid) {
		userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return enrollmentMapper.enrollmentToDto(organizationService.getUserEnrollments(uid));
	}

	@GetMapping("{uid}/consume/{code}")
	public EnrollmentDto consumeCode(@PathVariable("uid") String uid, @PathVariable("code") String code) {
		User u = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return enrollmentMapper.enrollmentToDto(organizationService.consumeCode(code, u));
	}

	@GetMapping("{uid}/verify/{code}")
	public EnrollmentDto verifyCode(@PathVariable("uid") String uid, @PathVariable("code") String code) {
		User u = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return enrollmentMapper.enrollmentToDto(organizationService.verifyCode(code, u));
	}

	@ApiOperation(value = "requestSeat", notes = "Richiede l’aggiunta di una sede a una Organization che deve essere una <u>PA con validazione attiva</u>. L'Organization in questione deve essere specificata nel body.")
	@PostMapping("{uid}/seats")
	public SeatDto requestSeat(@PathVariable("uid") String uid, @RequestBody SeatDto dto) {
		User u = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return seatMapper.seatToDto(addressService.requestSeat(u, dto));
	}

	//	Rankings
	@GetMapping("{uid}/rankings")
	public List<RankingDto> getRankings(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return rankingMapper.rankingToDto(rankingService.findByUser(user));
	}

	@GetMapping("{uid}/achievements")
	public List<AchievementDto> getAchievements(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return achievementsMapper.achievementUserToDto(achievementService.findAchievementUser(user));
	}

	//Awards
	@GetMapping("{uid}/awards")
	public List<AwardDto> getAwards(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return awardService.findAllByUser(user);
	}

	@GetMapping("{uid}/award-positions")
	public List<AwardPositionDto> getAwardPositions(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return awardMapper.awardPositionToDto(awardService.findAwardPositionsByUser(user));
	}

	@ApiOperation(value = "assignAwardPosition", notes = "È sufficiente valorizzare solo il campo timestamp nel body.")
	@PostMapping("{uid}/award-positions/{aid}")
	public String assignAwardPosition(@PathVariable String uid, @PathVariable Long aid, @RequestBody AwardPositionDto dto) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		AwardPosition award = awardService.findAwardPositionById(aid).orElseThrow(() -> new LismoveException("Award not found", HttpStatus.NOT_FOUND));
		awardService.assignAwardPosition(award, user, dto.getTimestamp());
		return "Assigned";
	}

	@GetMapping("{uid}/car")
	public CarModification getUserCar(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return user.getCar();
	}

	@PostMapping("{uid}/car")
	public CarModification setUserCar(@PathVariable String uid, @RequestBody CarModificationDto carModification) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		CarModification modification = carDataService.getModification(carModification.getId()).orElseThrow(() -> new LismoveException("Car not found", HttpStatus.NOT_FOUND));
		user.setCar(modification);
		userService.save(user);
		return modification;
	}

	@DeleteMapping("{uid}/car")
	public String deleteUserCar(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		user.setCar(null);
		userService.save(user);
		return "Deleted";
	}

	@ApiOperation(value = "getActiveHomeAddress", notes = "Ritorna l'indirizzo di casa attivo nel timestamp indicato. Se non viene passato alcun timestamp, ritorna l'indirizzo attualmente attivo.")
	@GetMapping("{uid}/home-address")
	public AddressOverviewDto getActiveHomeAddress(@PathVariable String uid, @RequestParam(required = false) Long activeAt) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		if(activeAt != null) {
			return addressMapper.homeAddressToDto(addressService.getActiveHomeAddressAt(user, activeAt).orElse(null));
		}
		return addressMapper.homeAddressToDto(addressService.getActiveHomeAddress(user).orElse(null));
	}

	@ApiOperation(value = "getActiveHomeAddresses", notes = "Ritorna un array fatto di copie dell'indirizzo di casa attivo nel timestamp indicato; " +
			"l'unica cosa che cambia tra le copie è la tolleranza riconosciuta dalle varie organizzazioni." +
			"Se non viene passato alcun timestamp, ritorna un array di copie dell'indirizzo attualmente attivo.")
	@GetMapping("{uid}/home-addresses")
	public Set<AddressOverviewDto> getActiveHomeAddresses(@PathVariable String uid, @RequestParam(required = false) Long activeAt) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return addressService.getActiveHomeAddressesAt(user, activeAt);
	}

	@ApiOperation(value = "getActiveWorkAddresses", notes = "Ritorna gli indirizzi di lavoro attivi nel timestamp indicato. Se non viene passato alcun timestamp, ritorna gli indirizzi attualmente attivi.")
	@GetMapping("{uid}/work-addresses")
	public Set<AddressOverviewDto> getActiveWorkAddresses(@PathVariable String uid, @RequestParam(required = false) Long activeAt) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		if(activeAt != null) {
			return addressMapper.workAddressToDto(addressService.getActiveWorkAddressesAt(user, activeAt));
		}
		return addressMapper.workAddressToDto(addressService.getActiveWorkAddresses(user));
	}

	@GetMapping("{uid}/dashboard")
	public UserDashboard getDashboard(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return userService.getUserDashboard(uid);
	}

	@GetMapping("{uid}/smartphones")
	public List<SmartphoneDto> getSmartphones(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return userMapper.smartphoneToDto(userService.getSmartphones(user));
	}

	@GetMapping("{uid}/messages")
	public List<NotificationMessageDeliveryDto> getMessages(@PathVariable String uid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return notificationMessageMapper.notificationMessageDeliveryToDto(notificationMessageService.getByUser(user));
	}

	@GetMapping("{uid}/messages/{mid}")
	public NotificationMessageReceiverDto markMessageAsRead(@PathVariable String uid, @PathVariable Long mid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		return notificationMessageMapper.notificationMessageDeliveryToReceiverDto(notificationMessageService.markRead(mid, user));
	}

	@PostMapping("{uid}/buy-article/{aid}")
	public AwardCustomDto buyArticle(@PathVariable String uid, @PathVariable Long aid) {
		User user = userService.findByUid(uid).orElseThrow(() -> new LismoveException("User not found", HttpStatus.NOT_FOUND));
		Article article = shopService.findArticleById(aid).orElseThrow(() -> new LismoveException("Article not found", HttpStatus.NOT_FOUND));
		return awardMapper.awardCustomUserToDto(shopService.buyArticle(user, article));
	}
}
