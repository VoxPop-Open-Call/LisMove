package net.nextome.lismove.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.PartialType;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.EnrollmentRepository;
import net.nextome.lismove.repositories.LogWallRepository;
import net.nextome.lismove.repositories.PartialRepository;
import net.nextome.lismove.repositories.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.nextome.lismove.services.SessionValidatorService.*;

@Service
@Transactional
public class BatchService {

	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private PartialRepository partialRepository;
	@Autowired
	private EnrollmentRepository enrollmentRepository;
	@Autowired
	private SessionPointService sessionPointService;
	@Autowired
	private UserService userService;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private SessionValidatorService sessionValidatorService;
	@Autowired
	private LogWallRepository logWallRepository;
	@Autowired
	private LogWallService logWallService;
	@Autowired
	private BridgeService bridgeService;
	@Autowired
	private AchievementService achievementService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void recalculatePoints(Long org) {
		enrollmentRepository.findAllEnabledByOrganization(org, LocalDate.of(2021, 11, 19)).get().forEach(e -> {
			if(e.getUser() != null) {
				User u = e.getUser();
				u.setEarnedNationalPoints(BigDecimal.ZERO);
				u.setPoints(BigDecimal.ZERO);
				e.setPoints(BigDecimal.ZERO);
				List<Session> sessions = sessionRepository.findByUserUidAndStartTimeAfter(
						e.getUser().getUid(),
						LocalDateTime.of(2021, 11, 17, 0, 0));
				logger.info("{} - {}", e.getUser().getUsername(), sessions.size());
				sessions.forEach(s -> {
					if(s.getValid() != null && s.getValid()) {

						SessionPoint sp = s.getSessionPoints().stream().filter(p -> p.getOrganization().getId().equals(org)).findFirst().orElse(null);
						if(sp != null) {
							logger.info("{} - {}: {}", s.getId(), s.getStartTime().toString(), sp.getPoints());
							e.setPoints(Optional.ofNullable(e.getPoints()).orElse(BigDecimal.ZERO).add(sp.getPoints()));
							u.setPoints(Optional.ofNullable(u.getPoints()).orElse(BigDecimal.ZERO).add(sp.getPoints()));
							u.setEarnedNationalPoints(Optional.ofNullable(u.getEarnedNationalPoints()).orElse(BigDecimal.ZERO).add(sp.getPoints()));
							enrollmentRepository.save(e);
						}
					}
				});
				achievementService.updateAchievements(u);
				userService.save(u);
			}
		});
		logger.info("END");
	}

	@Scheduled(cron = "0 0 2 * * ?")
	public void refreshHeatmapView() {
		logger.info("Start Heatmap Refresh");
		sessionRepository.refreshHeatmapView();
	}

	public void revalidateSessionByUser(String uid) {
		List<Session> sessions = sessionRepository.findByUserUidAndStartTimeAfter(uid, LocalDate.of(2021, 11, 17).atStartOfDay());
		for(Session s : sessions) {
			revalidateSession(s);
		}
	}

	public void revalidateSessionByDate(LocalDate date) {
		List<Session> sessions = sessionRepository.findByOldSessionIdIsNullAndStartTimeAfter(date.atStartOfDay());
		for(Session s : sessions) {
			if(s.getOldSessionId() == null)
				revalidateSession(s);
		}
	}

	public void revalidateSession(UUID uuid) {
		revalidateSession(sessionService.findById(uuid).get());
	}

	public void revalidateSession(Session session) {
		logger.info("User: {} - Session: {}", session.getUser(), session.getId());
		List<Partial> partials = sessionValidatorService.filterPartials(session);
		if(partials != null && !partials.isEmpty()) {
			Partial last = partials.get(partials.size() - 1);
			last.setType(PartialType.END);
			partialRepository.save(last);
		}
		boolean wasValid = Optional.ofNullable(session.getValid()).orElse(false);//, wasCertificated = session.getCertificated(), wasHomeWork = session.getHomeWorkPath(), sendToGrifo = false;
		long[] orgs = {1L, 12L, 19L};
//		Validation
//		if(!wasValid || !wasCertificated) {
		Distances distances = sessionValidatorService.calculateDistances(partials, DEFAULT_DISTANCE_THRESHOLD);
		session.setGyroDistance(distances.getGyroDistance());
		session.setGpsDistance(distances.getGpsDistance());
		session.setGmapsDistance(distances.getGmapsDistance());
		session.setGmapsPolyline(distances.getGmapsPolyline());
		session.setRawPolyline(distances.getRawPolyline());
		session.setPolyline(distances.getGmapsPolyline().equals("") || sessionValidatorService.isInPercent(session.getGmapsDistance(), session.getGpsDistance(), DEFAULT_GMAPS_POLYLINE_DEVIATION) ? session.getGmapsPolyline() : session.getRawPolyline());

		SessionValidatorService.Validation validation = sessionValidatorService.checkValid(partials, DEFAULT_VALID_PARTIAL_DEVIATION, DEFAULT_SPEED_THRESHOLD, DEFAULT_ON_FOOT_THRESHOLD,
				DEFAULT_TIME_PEAK_THRESHOLD, DEFAULT_VALID_PARTIAL_QTY);
		boolean isFullDistance = (session.getGpsDistance() != null && session.getGyroDistance() != null && session.getGyroDistance().compareTo(BigDecimal.ZERO) > 0 && sessionValidatorService.isInPercent(session.getGpsDistance(), session.getGyroDistance(), DEFAULT_VALID_SESSION_DEVIATION));
		session.setValid(isFullDistance || validation.getValid());
		session.setCertificated((session.getValid() && isFullDistance) || validation.getCertificated());
		session.setStatus(isFullDistance ? SessionStatus.VALID : validation.getStatus());
		session.setType(isFullDistance ? SessionType.BIKE : validation.getType());
		session.setNationalKm(Optional.ofNullable(session.getGyroDistance()).orElse(BigDecimal.ZERO).add(Optional.ofNullable(session.getGpsOnlyDistance()).orElse(BigDecimal.ZERO)).max(session.getGpsDistance()));
//		}

		if(session.getValid()) {
			HomeWorkCheck check = sessionValidatorService.checkIsHomeWork(session.getUser(), partials);
			session.setHomeWorkPath(check.isHomeWork());
			session.setHomeAddress(check.getHomeAddress());
			session.setWorkAddress(check.getWorkAddress());

//			SessionPoints

			if(session.getValid())
				Arrays.stream(orgs).forEach(l -> {
					Organization org = organizationService.findById(l).get();
					organizationService.findActiveByUserAndOrganization(session.getUser(), org).ifPresent(e -> {
						logger.info("{} attiva", org.getTitle());
						if(session.getValid()) {
							Optional<SessionPoint> points = session.getSessionPoints().stream().filter(sp -> sp.getOrganization().getId().equals(org.getId())).findFirst();
							SessionPoint sp = null;
							BigDecimal distance = null;
							if(points.isPresent() && session.getHomeWorkPath()) {
								sp = points.get();
								sp.setHomeWorkDistance(session.getNationalKm());
								distance = sp.getDistance().min(session.getNationalKm());
								sp.setDistance(distance);
								sp.setPoints(distance.movePointRight(1).setScale(0, RoundingMode.FLOOR));
							} else if(points.isPresent() && !session.getHomeWorkPath()) {
								sp = points.get();
								sp.setHomeWorkDistance(BigDecimal.ZERO);
								distance = sp.getDistance().min(session.getNationalKm());
//								sessionService.distanceInPolygon(org.getGeojson(), partials.stream().filter(p -> p.getLongitude() != null && p.getLatitude() != null).collect(Collectors.toList()));
//								sp.setDistance(distance);
							} else if(session.getCertificated()) {
								sp = new SessionPoint(session, org, BigDecimal.ZERO, BigDecimal.ZERO, 1D);
								if(session.getHomeWorkPath()) {
									sp.setHomeWorkDistance(session.getNationalKm());
								}
								distance = BigDecimal.ZERO;

								if(org.getId().equals(19L))
									distance = session.getNationalKm();
								else {
									try {
										distance = sessionService.distanceInPolygon(org.getGeojson(), partials.stream().filter(p -> p.getLongitude() != null && p.getLatitude() != null).collect(Collectors.toList()));
									} catch(JsonProcessingException ex) {
										ex.printStackTrace();
									}
								}
							}
							if(sp != null) {
								sp.setDistance(distance);
								sp.setPoints(distance.movePointRight(1).setScale(0, RoundingMode.FLOOR));
								sessionPointService.save(sp);

								if(Optional.ofNullable(e.getSessionForwarding()).orElse(false)) {
									if(bridgeService.forwardSession(session)) {
										session.setForwardedAt(LocalDateTime.now());
									}
								}
							}
						}
					});
				});

//			LogWall
			if(!wasValid) {
				sessionValidatorService.addNationalPoints(session.getUser(), session.getNationalKm());

				try {
					logWallService.writeLog(session);
				} catch(RuntimeException e) {
					logger.error(e.getMessage(), e);
				}
			}
			sessionService.save(session);
		}

	}
}
