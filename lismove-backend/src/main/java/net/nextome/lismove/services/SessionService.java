package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.DirectionsLeg;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.PartialType;
import net.nextome.lismove.models.enums.RefundStatus;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.PartialRepository;
import net.nextome.lismove.repositories.SessionRepository;
import net.nextome.lismove.rest.dto.SessionOverviewDto;
import net.nextome.lismove.services.utils.UtilitiesService;
import net.nextome.lismove.services.utils.WaypointsList;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static net.nextome.lismove.services.SessionValidatorService.DEFAULT_GMAPS_POLYLINE_DEVIATION;

@Service
@Transactional
public class SessionService extends UtilitiesService {
	public static final double DEFAULT_VALID_PARTIAL_QTY = 0.80;
	public static final double DEFAULT_VALID_PARTIAL_DEVIATION = 0.50; //%
	public static final double DEFAULT_SPEED_THRESHOLD = 60.0; //km/h
	public static final double DEFAULT_ON_FOOT_THRESHOLD = 17.0; //km/h
	public static final double DEFAULT_DISTANCE_THRESHOLD = 1; //m
	public static final long DEFAULT_TIME_PEAK_THRESHOLD = 4; //minutes
	public static final long DEFAULT_ACCELERATION_PEAK_THRESHOLD = 50; //ms2
	public final double CO2 = 163; //g

	private final MathContext mc = new MathContext(5, RoundingMode.HALF_UP);
	@Autowired
	private SessionRepository sessionRepository;
	@Autowired
	private PartialRepository partialRepository;
	@Autowired
	private SessionPointService sessionPointService;
	@Autowired
	private SettingsService settingsService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private GoogleMapsService googleMapsService;
	@Autowired
	private UserService userService;
	@Autowired
	private SensorService sensorService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private Bugsnag bugsnag;
	@Autowired
	private LogWallService logWallService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private SessionValidatorService sessionValidatorService;
	@Autowired
	private BridgeService bridgeService;
	@Autowired
	private EntityManager entityManager;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public Session save(Session s) {
		return sessionRepository.save(s);
	}

	public void saveCollections(Session session) {
		logger.info("Save {} partials", session.getPartials().size());
		session.setPartials(session.getPartials().stream().sorted(Comparator.comparing(Partial::getTimestamp)).collect(Collectors.toList()));
		session.getPartials().forEach(p -> {
			p.setSession(session);
			partialRepository.save(p);
		});
		logger.info("Save {} sessionPoints", session.getSessionPoints() != null ? session.getSessionPoints().size() : 0);
		if(session.getSessionPoints() != null) {
			session.getSessionPoints().forEach(sp -> {
				sp.setSession(session);
				sessionPointService.save(sp);
			});
		}
	}

	public void validateAsync(Session session) {
		logger.info("Start validating session {}", session.getId());
		sessionValidatorService.performFirstValidation(session);
		logger.info("Session " + session.getId().toString() + " validated");
		save(session);
	}

	public Session validate(Session session) {
		return validate(session,
				settingsService.get("VALID_PARTIAL_QTY", DEFAULT_VALID_PARTIAL_QTY),
				settingsService.get("VALID_PARTIAL_DEVIATION", DEFAULT_VALID_PARTIAL_DEVIATION),
				settingsService.get("SPEED_THRESHOLD", DEFAULT_SPEED_THRESHOLD),
				settingsService.get("ON_FOOT_THRESHOLD", DEFAULT_ON_FOOT_THRESHOLD),
				settingsService.get("DISTANCE_THRESHOLD", DEFAULT_DISTANCE_THRESHOLD),
				settingsService.get("TIME_PEAK_THRESHOLD", DEFAULT_TIME_PEAK_THRESHOLD));
	}

	public Session validate(Session session, Double partialQty, Double partialDeviation, Double speedThreshold, Double onFootThreshold, Double distanceThreshold, Long timeThreshold) {
		initSession(session);
		List<Partial> partials = session.getPartials();
		LocalDateTime oldTime;
		int validPartials = 0;
		int certifiedPartials = 0;
		long timeCounter = 0L;
		boolean onFoot = true;
		oldTime = partials.get(0).getTimestamp();

		if(session.getGyroDistance() == null || session.getGyroDistance().equals(BigDecimal.ZERO)) {
			session.setGyroDistance(BigDecimal.valueOf(session.getPartials().stream().mapToDouble(p -> Optional.ofNullable(p.getSensorDistance()).orElse(BigDecimal.ZERO).doubleValue()).sum()));
		}

		//Calcola GpsDistance, GmapsDistance e la polyline
		getGmapsPath(session, distanceThreshold);

//       *** Partials and session validation ***
		for(Partial partial : partials) {
			if(!isStartingPartial(partial)) {
//            Checking distance measurements accuracy
				if(!isStartingPartial(partial) && partial.getSensorDistance() != null) {
					if(!validateDistancePartial(partial, partialDeviation)) {
						partial.setValid(false);
						partial.setStatus(SessionStatus.DISTANCE_ERROR);
					}
				}

//            Checking partial speed peak
				if(!partial.getTimestamp().isEqual(oldTime)) {
					if(validateSpeedPartial(partial, oldTime, speedThreshold)) {
						timeCounter = 0;    //if speed is OK, counter is raised to 0
					} else {
						timeCounter = oldTime.until(partial.getTimestamp(), MILLIS);    //otherwise, the counter is incremented
					}
					if(onFoot) {
						onFoot = partial.getDeltaRevs() != null || validateSpeedPartial(partial, oldTime, onFootThreshold);
					}
				}
				oldTime = partial.getTimestamp();
				if(TimeUnit.MILLISECONDS.toMinutes(timeCounter) >= timeThreshold) {    //if peak lasts >= 4 minutes
					session.setValid(false);
					session.setStatus(SessionStatus.SPEED_ERROR);
				}
			}

//            Updating valid partials counter
			if(partial.getValid()) {
				validPartials++;
				if(partial.getDeltaRevs() != null && partial.getSensorDistance() != null) {
					certifiedPartials++;
				}
			}
		}

//        Checking total valid partials
		boolean isValid = (double) validPartials / partials.size() > partialQty;
		if(!isValid && session.getValid()) {
			session.setStatus(SessionStatus.DISTANCE_ERROR);
			session.setValid(false);
		}
		session.setCertificated((double) certifiedPartials / partials.size() > partialQty);

//        Path validation
		session.setHomeAddress(addressService.getActiveHomeAddress(session.getUser()).orElse(null));
		if(session.getValid()) {
			User user = session.getUser();
			session.setCo2(session.getGpsDistance().doubleValue() * CO2);
			HomeWorkPath path = findHomeWorkPath(addressService.getActiveHomeWorkPaths(user), session);
			session.setHomeWorkPath(path != null);
			if(path != null) {
				session.setHomeAddress(path.getHomeAddress());
				session.setWorkAddress(addressService.getActiveWorkAddresses(user).stream().filter(address -> address.getSeat().equals(path.getSeat())).findFirst().get());
			}
		}

		session.setDuration(calculateDuration(session));
		logger.info("Certificated: {}", session.getCertificated());
		logger.info("Gyro distance: {}", session.getGyroDistance());
		logger.info("GPS Only distance: {}", session.getGpsOnlyDistance());
		logger.info("GPS distance: {}", session.getGpsDistance());
		session.setNationalKm(session.getCertificated() ? session.getGyroDistance().add(Optional.ofNullable(session.getGpsOnlyDistance()).orElse(BigDecimal.ZERO)) : session.getGpsDistance());
		session.setNationalPoints(session.getNationalKm().movePointRight(1).setScale(0, RoundingMode.FLOOR));

//		Points assignment and refund calculation
		try {
			if(session.getValidatedDate() == null) {
				assignPoints(session);
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			bugsnag.notify(e);
		}

		if(session.getSensor() == null) {
			sensorService.getActiveSensor(session.getUser()).ifPresent(value -> {
				assignSensor(session, value);
			});
		} else if(session.getFirmware() != null) {
			sensorService.getActiveSensor(session.getUser()).ifPresent(value -> {
				value.setFirmware(session.getFirmware());
				sensorService.save(value);
			});
		}

		if(session.getAppVersion() == null) {
			userService.getActivePhone(session.getUser()).ifPresent(
					smartphone -> {
						session.setAppVersion(smartphone.getAppVersion());
						session.setPlatform(smartphone.getPlatform());
						session.setPhoneModel(smartphone.getModel());
					}
			);
		}

		if(session.getValidatedDate() == null && session.getValid()) {
			try {
				logWallService.writeLog(session);
			} catch(RuntimeException e) {
				logger.error(e.getMessage(), e);
				bugsnag.notify(e);
			}
		}

//		  Setting session type
		session.setType((onFoot && session.getSensor() == null) ? SessionType.FOOT : session.getType());
		session.setValidatedDate(LocalDateTime.now());
		return session;
	}

	public void assignPoints(Session session) {
		List<SessionPoint> points = Optional.ofNullable(session.getSessionPoints()).orElse(new LinkedList<>());
		if(session.getHomeWorkPath() != null && session.getHomeWorkPath()) {
			//TODO c'è un modo per evitare getId()? O è sempre preferibile fare così?
			Optional<SessionPoint> hwsp = points.stream().filter(sp -> session.getWorkAddress().getSeat().getOrganization().getId().equals(sp.getOrganization().getId())).findFirst();
			if(hwsp.isPresent()) {
				hwsp.get().setHomeWorkDistance(session.getNationalKm().setScale(5, RoundingMode.HALF_UP));
			} else {
				points.add(sessionPointService.createHomeWorkSessionPoints(session));
			}
		}
		session.setSessionPoints(points);
		points.forEach(sp -> {
			sp.setSession(session);
			sp.setRefundStatus(RefundStatus.NO_REFUND);
		});
		sessionPointService.assignSessionPoints(session.getSessionPoints());
	}

	private void getGmapsPath(Session session, Double distanceThreshold) {
		List<Partial> partials = session.getPartials();
		List<List<Partial>> subsessions = new LinkedList<>();
		int currentWaypointsList = -1;

		for(int i = 0; i < partials.size(); i++) {
			Partial partial = partials.get(i);
			Partial prevPartial = i > 0 ? partials.get(i - 1) : null;
			if(prevPartial == null || isStartingPartial(partial)) {
				//Add starting point for each subsession
				++currentWaypointsList;
				subsessions.add(new LinkedList<>());
				subsessions.get(currentWaypointsList).add(partial);
				partial.setValid(true);
				partial.setStatus(SessionStatus.VALID);
				partial.setGpsDistance(BigDecimal.ZERO);
				partial.setGmapsDistance(BigDecimal.ZERO);
			} else {
				double distance;
				try {
					distance = UtilitiesService.distance(prevPartial.getLatitude(), prevPartial.getLongitude(), partial.getLatitude(), partial.getLongitude());
				} catch(NullPointerException e) {
					distance = 0.0;
				}
				if(prevPartial.getGpsDistance() != null) {
					double accel = UtilitiesService.acceleration(prevPartial.getGpsDistance().doubleValue() * 1000, distance, partial.getTimestamp().until(prevPartial.getTimestamp(), SECONDS));
					partial.setGpsDistance(new BigDecimal(distance, mc).movePointLeft(3));
					if(distance > distanceThreshold && Math.abs(accel) < DEFAULT_ACCELERATION_PEAK_THRESHOLD) {
						subsessions.get(currentWaypointsList).add(partial);
						session.setGpsDistance(session.getGpsDistance().add(partial.getGpsDistance())); //updating session gps distance only if the value is valid
					} else {
						partial.setGmapsDistance(BigDecimal.ZERO);
					}
				} else {
					partial.setGpsDistance(new BigDecimal(distance, mc).movePointLeft(3));
					partial.setGmapsDistance(BigDecimal.ZERO);
				}

			}
		}
		for(List<Partial> subsession : subsessions) {
			processSubsession(session, subsession);
		}
	}

	public void processSubsession(Session session, List<Partial> subsession) {
		try {
			WaypointsList.WaypointsListResult result = googleMapsService.generateWaypointsListResult(subsession);
			Iterator<DirectionsLeg> legs = result.getDirectionsLegs().iterator();
			for(Partial partial : subsession) {
				if(!isStartingPartial(partial)) {
					partial.setGmapsDistance(new BigDecimal(legs.next().distance.inMeters, mc).movePointLeft(3));
					session.setGmapsDistance(session.getGmapsDistance().add(partial.getGmapsDistance()));
				}
			}
			String polyline = Optional.ofNullable(session.getPolyline()).orElse("");
			session.setPolyline(polyline + (polyline.isEmpty() ? "" : "€") + result.getPolyline());
		} catch(Exception e) {
			logger.error(e.getMessage());
			bugsnag.notify(e);
			subsession.forEach(p -> p.setGmapsDistance(null));
		}
	}

	public int calculateDuration(Session session) {
		int duration = 0;
		Partial partial, prevPartial;
		List<Partial> partials = session.getPartials();
		for(int i = 1; i < partials.size(); i++) {
			partial = partials.get(i);
			prevPartial = partials.get(i - 1);
			if(!isStartingPartial(partial)) {
				duration += (int) Duration.between(prevPartial.getTimestamp(), partial.getTimestamp()).getSeconds();
			}
		}
		return duration;
	}

	private void initSession(Session session) {
		if(session.getType() == null) {
			session.setType(SessionType.BIKE);
		}
		session.setValid(true);
		session.setCertificated(false);
		session.setHomeWorkPath(false);
		session.setStatus(SessionStatus.VALID);
		session.setGpsDistance(BigDecimal.ZERO);
		session.setGmapsDistance(BigDecimal.ZERO);
		session.setPolyline("");
		boolean pause = false;
		Partial partial;
		for(int i = 0; i < session.getPartials().size(); i++) {
			partial = session.getPartials().get(i);
			initPartial(partial);
			partial.setSession(session);

			if((pause && !isStartingPartial(partial)) || isDebugPartial(partial)) {
				session.getPartials().remove(i--);
			}
			pause = partial.getType().equals(PartialType.PAUSE) || pause && !isStartingPartial(partial);
		}
		session.getPartials().get(0).setGmapsDistance(BigDecimal.ZERO);
		Optional.ofNullable(session.getSessionPoints()).ifPresent(sps -> sps.forEach(sp -> sp.setSession(session)));
	}

	private void initPartial(Partial partial) {
		if(partial.getType() == null) {
			partial.setType(PartialType.UNKNOWN);
		}
		partial.setValid(true);
		partial.setStatus(SessionStatus.VALID);
		partial.setGpsDistance(null);
		partial.setGmapsDistance(null);
	}

	public boolean isStartingPartial(Partial partial) {
		return partial.getType().equals(PartialType.START)
				|| partial.getType().equals(PartialType.RESUME);
	}

	public boolean isDebugPartial(Partial partial) {
		return partial.getType().ordinal() >= PartialType.SESSION.ordinal();
	}

	public void assignSensor(Session session, Sensor sensor) {
		session.setBikeType(sensor.getBikeType());
		session.setWheelDiameter(sensor.getWheelDiameter());
		session.setSensor(sensor.getUuid());
		session.setSensorName(sensor.getName());
		if(sensor.getBikeType() == null || sensor.getBikeType().equalsIgnoreCase("normale")) {
			session.setType(SessionType.BIKE);
		}
	}

	/**
	 * Checks whether the distance is accurate according to VALID_PARTIAL_DEVIATION
	 *
	 * @param partial partial to check
	 * @return true if valid, false otherwise
	 */
	public boolean validateDistancePartial(Partial partial, double partialDistanceDeviation) {
		if(partial.getSensorDistance().compareTo(BigDecimal.ZERO) > 0) {
			return isInPercent(partial.getGpsDistance(), partial.getSensorDistance(), partialDistanceDeviation);
		} else {
			return partial.getGpsDistance().compareTo(new BigDecimal("0.01")) < 0;
		}
	}

	public boolean validateDistancePartial(Partial partial) {
		return validateDistancePartial(partial, DEFAULT_VALID_PARTIAL_DEVIATION);
	}

	/**
	 * Checks whether the speed is valid according to SPEED_THRESHOLD
	 *
	 * @param partial       partial to check
	 * @param timeReference time instant from which time difference is calculated
	 * @return true if valid, false otherwise
	 */
	public boolean validateSpeedPartial(Partial partial, LocalDateTime timeReference, double speedThreshold) {
		long interval = timeReference.until(partial.getTimestamp(), SECONDS);
		if(interval == 0L) {
			return true;
		}
		return speed(partial.getGpsDistance().multiply(BigDecimal.valueOf(1000)), interval)
				.compareTo(BigDecimal.valueOf(speedThreshold)) < 0;
	}

	public boolean validateSpeedPartial(Partial partial, LocalDateTime timeReference) {
		return validateSpeedPartial(partial, timeReference, DEFAULT_SPEED_THRESHOLD);
	}

	/**
	 * Checks whether the session is a home to work journey or the opposite, compared to each path of the list given
	 *
	 * @param paths   paths list where find path from
	 * @param session session to check
	 * @return path found
	 */
	public HomeWorkPath findHomeWorkPath(Set<HomeWorkPath> paths, Session session) {
		if(paths == null || paths.size() == 0) {
			return null;
		}
		Partial start = session.getPartials().get(0);
		Partial end = session.getPartials().get(session.getPartials().size() - 1);
		for(HomeWorkPath path : paths) {
			BigDecimal homeTolerance = BigDecimal.valueOf(organizationSettingsService.get(path.getSeat().getOrganization(), "homeWorkPointsTolerance", Double.class));
			BigDecimal workTolerance = Optional.ofNullable(path.getSeat().getDestinationTolerance()).orElse(homeTolerance);
			Double homeWorkPathTolerance = organizationSettingsService.get(path.getSeat().getOrganization(), "homeWorkPathTolerancePerc", Double.class);
			if(validateHomeWorkPath(start, end, session.getGpsDistance(), path, homeTolerance, workTolerance, homeWorkPathTolerance)
					|| validateHomeWorkPath(end, start, session.getGpsDistance(), path, homeTolerance, workTolerance, homeWorkPathTolerance)) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Checks whether starting and ending point are nearby home and seat address and if distance is compatible with the path data given
	 *
	 * @param start    start partial
	 * @param end      end partial
	 * @param distance session distance
	 * @param path     path to check
	 * @return true if start and end partials given are compatible with path
	 */
	public boolean validateHomeWorkPath(Partial start, Partial end, BigDecimal distance, HomeWorkPath path, BigDecimal homeTolerance, BigDecimal workTolerance, Double homeWorkPathTolerance) {
		boolean s = BigDecimal.valueOf(distanceKm(start.getLatitude(), start.getLongitude(), path.getHomeAddress().getLatitude(), path.getHomeAddress().getLongitude())).compareTo(homeTolerance) <= 0;
		boolean e = BigDecimal.valueOf(distanceKm(end.getLatitude(), end.getLongitude(), path.getSeat().getLatitude(), path.getSeat().getLongitude())).compareTo(workTolerance) <= 0;
		boolean p = distance.compareTo(path.getDistance().add(path.getDistance().multiply(BigDecimal.valueOf(homeWorkPathTolerance)))) <= 0;
		return s && e && p;
	}

	public Session update(Session old, SessionOverviewDto upd) {
		if(upd.getValid() != null) {
			if(old.getValid().equals(Boolean.FALSE) && upd.getValid().equals(Boolean.TRUE)) {
				old = validateManually(old);
			} else if(old.getValid().equals(Boolean.TRUE) && upd.getValid().equals(Boolean.FALSE)) {
				old = invalidateManually(old);
			}
		} else if(upd.getCertificated() != null) {
			if(old.getCertificated().equals(Boolean.FALSE) && upd.getCertificated().equals(Boolean.TRUE)) {
				old = certifyManually(old);
			} else if(old.getValid().equals(Boolean.TRUE) && upd.getValid().equals(Boolean.FALSE)) {
				old = uncertifyManually(old);
			}
		} else {
			notNullBeanCopy(upd, old, "uuid");
		}

		return this.save(old);
	}

	public Session certifyManually(Session s) {
		if(!Optional.ofNullable(s.getValid()).orElse(false)) {
			s = validateManually(s);
		}
		s.setSessionPoints(sessionValidatorService.calculateUrbanPoints(s));
		assignPoints(s);
		return s;
	}

	public Session uncertifyManually(Session s) {
		s.getSessionPoints().forEach(sp -> {
			Optional<Enrollment> enrollment = organizationService.findActiveByUserAndOrganization(s.getUser(), sp.getOrganization());
			if(enrollment.isPresent()) {
				enrollment.get().setPoints(Optional.ofNullable(enrollment.get().getPoints()).orElse(BigDecimal.ZERO).subtract(sp.getPoints()));
				organizationService.save(enrollment.get());
			}
			sp.setDistance(BigDecimal.ZERO);
			sp.setEuro(BigDecimal.ZERO);
			sp.setPoints(BigDecimal.ZERO);
			sessionPointService.save(sp);
		});
		return s;
	}

	public Session validateManually(Session s) {
		s.setCo2(sessionValidatorService.calculateCo2(s.getGpsDistance().doubleValue()));
//			Check if Home-Work
		SessionValidatorService.HomeWorkCheck check = sessionValidatorService.checkIsHomeWork(s.getUser(), sessionValidatorService.filterPartials(s));
		s.setHomeWorkPath(check.isHomeWork());
		s.setHomeAddress(check.getHomeAddress());
		s.setWorkAddress(check.getWorkAddress());
		s.setNationalPoints(sessionValidatorService.addNationalPoints(s.getUser(), s.getNationalKm()));
		try {
			logWallService.writeLog(s);
		} catch(RuntimeException e) {
			logger.error(e.getMessage(), e);
			bugsnag.notify(e);
		}
		return s;
	}

	public Session invalidateManually(Session s) {
		s.setCo2(0D);
		s.setHomeWorkPath(false);
		s.setNationalPoints(BigDecimal.ZERO);
		Optional.ofNullable(s.getSessionPoints()).orElse(new LinkedList<>()).forEach(sp -> {
			sp.setPoints(BigDecimal.ZERO);
			sp.setEuro(BigDecimal.ZERO);
			sessionPointService.save(sp);
		});
		return s;
	}

	public Optional<Session> findById(UUID id) {
		return sessionRepository.findById(id);
	}

	public List<Session> findAll(Integer limit) {
		if(limit == null) {
			return sessionRepository.findAllByOrderByStartTimeDesc();
		} else {
			return sessionRepository.findAllByOrderByStartTimeDesc(limit);
		}
	}

	public List<Session> findByUser(String uid) {
		return sessionRepository.findByUserUid(uid);
	}

	public List<Session> findValidByUser(User user, LocalDateTime startTime) {
		return sessionRepository.findByValidIsTrueAndUserAndStartTimeAfter(user, startTime).orElse(new LinkedList<>());
	}

	public List<Session> findByOrganization(Organization o, Integer limit) {
		if(limit == null) return sessionRepository.findByOrganization(o.getId());
		else return sessionRepository.findByOrganization(o.getId(), limit);
	}

	public Optional<Session> findExists(String uid, LocalDateTime start) {
		return sessionRepository.findByUserUidAndStartTime(uid, start);
	}

	public List<Session> findNotCertificatedByUserAndBetween(User user, LocalDateTime start, LocalDateTime end) {
		return sessionRepository.findByUserAndStartTimeIsGreaterThanEqualAndEndTimeIsLessThanEqual(user, start, end);
	}

	public void tester() {
		List<Session> sessions = findValidByUser(userService.findByUid("kV2neXAAWVR7QD9py2aSpRSQGni1").get(), LocalDate.of(2021, 11, 15).atStartOfDay());

		for(Session session : sessions) {
			System.out.println(session.getId());
			List<SessionPoint> points = sessionValidatorService.calculateUrbanPoints(session);
			for(SessionPoint p : points) {
				System.out.println("- " + p.getOrganization().getTitle() + ": " + p.getPoints());
			}
		}
	}

	public void resetUserNationalPoints() {
		List<User> users = userService.findAll();
		for(User user : users) {
			List<Session> sessions = findByUser(user.getUid());
			BigDecimal points = BigDecimal.ZERO;
			for(Session s : sessions) {
				if(s.getValid()) {
					s.setNationalPoints(Optional.ofNullable(s.getGyroDistance()).orElse(BigDecimal.ZERO).movePointRight(1).setScale(0, RoundingMode.FLOOR));
					sessionRepository.save(s);
				}
				points = points.add(Optional.ofNullable(s.getNationalPoints()).orElse(BigDecimal.ZERO));
			}
			user.setEarnedNationalPoints(points);
			userService.save(user);
		}
	}

	public BigDecimal distanceInPolygon(String geoJsonArea, List<Partial> partials) throws JsonProcessingException {
		GeometryFactory gf = new GeometryFactory();
		ObjectMapper mapper = new ObjectMapper();
		Coords[][] areas = mapper.readValue(geoJsonArea, Coords[][].class);
		List<Polygon> polygons = new LinkedList<>();

		for(Coords[] area : areas) {
			List<Coordinate> c = Arrays.stream(area).map(a -> new Coordinate(a.lat, a.lng)).collect(Collectors.toList());
			c.add(new Coordinate(area[0].lat, area[0].lng));
			polygons.add(gf.createPolygon(c.toArray(new Coordinate[0])));
		}

		BigDecimal urbanDistance = BigDecimal.ZERO;
		for(Partial point : partials) {
			boolean isInArea = false;
			for(Polygon p : polygons) {
				if(p.contains(gf.createPoint(new Coordinate(point.getLatitude(), point.getLongitude())))) {
					isInArea = true;
				}
			}
			if(isInArea) {
				urbanDistance = urbanDistance.add(Optional.ofNullable(point.getSensorDistance()).orElse(BigDecimal.ZERO));
			}
		}
		return urbanDistance;
	}

	static class Coords {
		public Double lat;
		public Double lng;
	}

	public Session deepCopy(Session session) {
		Session clone = new Session();
		notNullBeanCopy(session, clone, "partials", "sessionPoints");
		clone.setSessionPoints(new LinkedList<>());
		clone.setPartials(new LinkedList<>());
		for(SessionPoint sp : session.getSessionPoints()) {
			SessionPoint cloneSp = new SessionPoint();
			notNullBeanCopy(sp, cloneSp);
			clone.getSessionPoints().add(cloneSp);
		}
		for(Partial p : session.getPartials()) {
			Partial cloneP = new Partial();
			notNullBeanCopy(p, cloneP);
			clone.getPartials().add(cloneP);
		}
		return clone;
	}

	@Transactional(propagation = Propagation.NEVER)
	public void sendToGrifo(LocalDateTime start) {
		List<Session> sessions = sessionRepository.findByValidIsTrueAndStartTimeAfter(start).orElse(new LinkedList<>());
		AtomicInteger i = new AtomicInteger(1);
		sessions.forEach(s -> {
			if(s.getForwardedAt() != null) return;
			Optional<Enrollment> enrollmentBA = organizationService.findActiveByUserAndOrganization(s.getUser(), organizationService.findById(1L).get());
			Optional<Enrollment> enrollmentBG = organizationService.findActiveByUserAndOrganization(s.getUser(), organizationService.findById(12L).get());

			if(s.getUser().getOldUserId() != null && (enrollmentBA.isPresent() && Optional.ofNullable(enrollmentBA.get().getSessionForwarding()).orElse(false)) || (enrollmentBG.isPresent() && Optional.ofNullable(enrollmentBG.get().getSessionForwarding()).orElse(false))) {
				logger.info("{} - {} {} {}", i.getAndIncrement(), s.getStartTime().toString(), s.getUser().getEmail(), s.getId());
				if(bridgeService.forwardSession(s)) {
					s.setForwardedAt(LocalDateTime.now());
					save(s);
				}
			}
		});

	}

	@Transactional(propagation = Propagation.NEVER)
	public void sessionFixer() {
		AtomicInteger count = new AtomicInteger();
		AtomicInteger clones = new AtomicInteger();
		List<Session> brokenSessions = sessionRepository.findBrokenSessions();
//		List<Session> brokenSessions = sessionRepository.findBrokenSessionsByUser("LoTGO4mcsBUs77KJhSN6zSz8q0s1");
		logger.info("{} broken session(s) found", brokenSessions.size());
		brokenSessions.forEach(session -> {
			logger.info("Session {} found...", session.getId().toString());
			List<Partial> partials = new LinkedList<Partial>() {{
				addAll(session.getPartials());
			}};
			//Aggiunta type a tutti i parziali
			partials.forEach(p -> p.setType(PartialType.IN_PROGRESS));
			partials.get(0).setType(PartialType.START);
			partials.get(partials.size() - 1).setType(PartialType.END);

			//Rimozione parziali clonati
			logger.info("Scanning {} partial(s)", session.getPartials().size());
			count.incrementAndGet();
			Partial partial;
			int size = partials.size();
			for(int i = 0; i < size; i++) {
				partial = partials.get(i);
				if(i > 0 && i < size - 1) {
					if(partial.getDeltaRevs().equals((double) 0)
							&& (Duration.between(partial.getTimestamp(), partials.get(i - 1).getTimestamp()).getSeconds() == 0
							|| Duration.between(partial.getTimestamp(), partials.get(i + 1).getTimestamp()).getSeconds() == 0)) {
						clones.incrementAndGet();
						//Cancellazione cloni
						entityManager.detach(session.getPartials().get(i));
						session.getPartials().remove(i);
						--size;
					}
				}
			}
			logger.info("{} clone(s) found", clones);
			logger.info("{} partial(s) remaning", session.getPartials().size());
			clones.set(0);
			boolean tempValid = session.getValid() != null && session.getValid(),
					tempCert = session.getCertificated() != null && session.getCertificated(),
					tempHWP = session.getHomeWorkPath() != null && session.getHomeWorkPath();
			// Sanificazione
			try {
				//TODO realizza funzione
				SessionValidatorService.Distances distances = sessionValidatorService.calculateDistances(partials, SessionValidatorService.DEFAULT_DISTANCE_THRESHOLD);
				session.setGmapsDistance(distances.getGmapsDistance());
				session.setGmapsPolyline(distances.getGmapsPolyline());
				session.setRawPolyline(distances.getRawPolyline());
				session.setPolyline(distances.getGmapsPolyline() != null && !distances.getGmapsPolyline().isEmpty() && isInPercent(session.getGmapsDistance(), session.getGpsDistance(), DEFAULT_GMAPS_POLYLINE_DEVIATION) ? session.getGmapsPolyline() : session.getRawPolyline());
				// -----
				session.setValid(tempValid);
				session.setCertificated(tempCert);
				session.setHomeWorkPath(tempHWP);
				save(session);
				logger.info("Session {} fixed", session.getId().toString());
			} catch(Exception e) {
				logger.error(e.getMessage());
				logger.info("Session {} failed", session.getId().toString());
			}
		});
		logger.info("{} session(s) fixed", count);
	}
}
