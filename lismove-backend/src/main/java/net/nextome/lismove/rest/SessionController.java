package net.nextome.lismove.rest;

import com.bugsnag.Bugsnag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Enrollment;
import net.nextome.lismove.models.Session;
import net.nextome.lismove.models.User;
import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.rest.dto.OfflineSessionDto;
import net.nextome.lismove.rest.dto.SessionDto;
import net.nextome.lismove.rest.dto.SessionOverviewDto;
import net.nextome.lismove.rest.mappers.SessionMapper;
import net.nextome.lismove.security.NextomeUserDetails;
import net.nextome.lismove.services.*;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static net.nextome.lismove.services.utils.UtilitiesService.notNullBeanCopy;

@RestController
@RequestMapping("sessions")
public class SessionController {

	@Autowired
	private SessionService sessionService;
	@Autowired
	private Bugsnag bugsnag;
	@Autowired
	private OfflineSessionService offlineSessionService;
	@Autowired
	private SettingsService settingsService;
	@Autowired
	private UserService userService;
	@Autowired
	private AchievementService achievementService;
	@Autowired
	private SessionMapper sessionMapper;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private BridgeService bridgeService;
	@Autowired
	private AddressService addressService;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PostMapping
	public SessionDto create(@RequestBody String json) throws JsonProcessingException {
		logger.info(json);
		ObjectMapper mapper = new ObjectMapper();
		SessionDto dto = mapper.readValue(json, SessionDto.class);
		logger.info("Got a session with {} partials", dto.getPartials().size());
		userService.findByUid(dto.getUid()).orElseThrow(() -> new LismoveException(("User not found"), HttpStatus.NOT_FOUND));
		Session s = sessionMapper.dtoToSession(dto);
		Optional<Session> old = sessionService.findExists(s.getUser().getUid(), s.getStartTime());
		addressService.refreshHomeWorkPathsByUser(s.getUser());
		if(old.isPresent()) {
			logger.info("Session started at {} for user {} already exists", s.getStartTime().format(DateTimeFormatter.ISO_DATE_TIME), s.getUser().getUid());
			s = old.get();
		} else {
			Session light = new Session();
			logger.info("Save session {}", s.getId());
			//TODO salva sessione, parziali e sessionPoints
			notNullBeanCopy(s, light, "partials", "sessionPoints");
			logger.info("Save session");
			try {
				sessionService.save(light);
				notNullBeanCopy(light, s, "partials", "sessionPoints");
				sessionService.saveCollections(s);
			} catch(ConstraintViolationException e) {
				throw new LismoveException("Sessione salvata", HttpStatus.BAD_REQUEST);
			}
			sessionService.validateAsync(s);
			achievementService.updateAchievements(s.getUser());
			//Session forwarding
			try {
				Session finalS = s;
				if(s.getCertificated() && s.getSessionPoints() != null && s.getSessionPoints().stream().anyMatch(sessionPoint -> {
					Optional<Enrollment> e = organizationService.findActiveByUserAndOrganization(finalS.getUser(), sessionPoint.getOrganization());
					return e.isPresent() && Optional.ofNullable(e.get().getSessionForwarding()).orElse(false);
				})) {
					logger.info("Forwarding session {}", s.getId());
					if(bridgeService.forwardSession(s)) {
						s.setForwardedAt(LocalDateTime.now());
						sessionService.save(s);
					}
				}
			} catch(RuntimeException e) {
				bugsnag.notify(e);
			}
		}
		logger.info("Send response with session {}", s.getId());
		return sessionMapper.sessionToDtoWithoutPartials(s);
	}

	@PostMapping("offline")
	public void createOffline(@RequestBody List<OfflineSessionDto> dto) {
		logger.info("Got a offline session with {} values", dto.size());
		ObjectMapper mapper = new ObjectMapper();
		for(OfflineSessionDto el : dto) {
			try {
				logger.info("Received {}", mapper.writeValueAsString(el));
			} catch(JsonProcessingException e) {
				logger.error("Jackson error: {}", e.getMessage());
			}
		}
		offlineSessionService.receive(sessionMapper.dtoToOfflineSession(dto));
	}

	@GetMapping("offline")
	public List<OfflineSessionDto> getOffline() {
		return sessionMapper.offlineSessionToDto(offlineSessionService.list());
	}

	@PutMapping("{uuid}")
	public SessionOverviewDto update(@PathVariable("uuid") UUID uuid, @RequestBody SessionOverviewDto sessionOverviewDto) {
		Session old = sessionService.findById(uuid).orElseThrow(() -> new LismoveException("Session not found", HttpStatus.NOT_FOUND));
		old = sessionService.update(old, sessionOverviewDto);
		return sessionMapper.sessionToOverviewDto(old);
	}

	@PutMapping("{uuid}/validate")
	public SessionDto validate(@PathVariable("uuid") UUID uuid,
	                           @RequestParam(required = false) Double partialQty,
	                           @RequestParam(required = false) Double partialDeviation,
	                           @RequestParam(required = false) Double speedThreshold,
	                           @RequestParam(required = false) Double onFootThreshold,
	                           @RequestParam(required = false) Double distanceThreshold,
	                           @RequestParam(required = false) Long timeThreshold) {
		if(partialQty == null) {
			partialQty = settingsService.get("VALID_PARTIAL_QTY", SessionService.DEFAULT_VALID_PARTIAL_QTY);
		}
		if(partialDeviation == null) {
			partialDeviation = settingsService.get("VALID_PARTIAL_DEVIATION", SessionService.DEFAULT_VALID_PARTIAL_DEVIATION);
		}
		if(speedThreshold == null) {
			speedThreshold = settingsService.get("SPEED_THRESHOLD", SessionService.DEFAULT_SPEED_THRESHOLD);
		}
		if(onFootThreshold == null) {
			onFootThreshold = settingsService.get("ON_FOOT_THRESHOLD", SessionService.DEFAULT_ON_FOOT_THRESHOLD);
		}
		if(distanceThreshold == null) {
			distanceThreshold = settingsService.get("DISTANCE_THRESHOLD", SessionService.DEFAULT_DISTANCE_THRESHOLD);
		}
		if(timeThreshold == null) {
			timeThreshold = settingsService.get("TIME_PEAK_THRESHOLD", SessionService.DEFAULT_TIME_PEAK_THRESHOLD);
		}
		Session session = sessionService.findById(uuid).orElseThrow(() -> new LismoveException("Session not found"));
		Session clone = sessionService.deepCopy(session);
		entityManager.detach(session);
		sessionService.validate(clone, partialQty, partialDeviation, speedThreshold, onFootThreshold, distanceThreshold, timeThreshold);
		return sessionMapper.sessionToDto(clone);
	}

	@GetMapping("parameters")
	public List<Double> getParameters() {
		List<Double> list = new ArrayList<Double>();
		list.add(settingsService.get("VALID_PARTIAL_QTY", SessionService.DEFAULT_VALID_PARTIAL_QTY));
		list.add(settingsService.get("VALID_PARTIAL_DEVIATION", SessionService.DEFAULT_VALID_PARTIAL_DEVIATION));
		list.add(settingsService.get("SPEED_THRESHOLD", SessionService.DEFAULT_SPEED_THRESHOLD));
		return list;
	}

	@GetMapping("{uuid}")
	public SessionDto get(@PathVariable UUID uuid, @RequestParam(defaultValue = "false", required = false) Boolean partials) {
		Session session = sessionService.findById(uuid).orElseThrow(() -> new LismoveException("Session not found", HttpStatus.NOT_FOUND));
		if(partials) {
			return sessionMapper.sessionToDto(session);
		} else {
			return sessionMapper.sessionToDtoWithoutPartials(session);
		}
	}

	@GetMapping
	public List<SessionOverviewDto> getAllSessions(@RequestParam(required = false) Integer limit, Authentication authentication) {
		User user = ((NextomeUserDetails) authentication.getPrincipal()).getUserData();
		List<Session> sessions;
		if(user.getUserType().equals(UserType.ROLE_ADMIN))
			sessions = sessionService.findAll(limit);
		else if(user.getUserType().equals(UserType.ROLE_MANAGER))
			sessions = sessionService.findByOrganization(user.getOrganization(), limit);
		else sessions = new LinkedList<>();
		return sessionMapper.sessionToOverviewDto(sessions);
	}
}
