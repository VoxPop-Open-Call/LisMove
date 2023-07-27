package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.maps.model.DirectionsLeg;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.PartialType;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.models.enums.SessionType;
import net.nextome.lismove.repositories.PartialRepository;
import net.nextome.lismove.services.utils.UtilitiesService;
import net.nextome.lismove.services.utils.WaypointsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

@Service
@Transactional
public class SessionValidatorService extends UtilitiesService {

	public static final double DEFAULT_VALID_PARTIAL_QTY = 0.80;
	public static final double DEFAULT_VALID_PARTIAL_DEVIATION = 0.50;
	public static final double DEFAULT_VALID_SESSION_DEVIATION = 0.70; // %
	public static final double DEFAULT_GMAPS_POLYLINE_DEVIATION = 0.90; // %
	public static final double DEFAULT_SPEED_THRESHOLD = 60.0; // km/h
	public static final double DEFAULT_ON_FOOT_THRESHOLD = 17.0; // km/h
	public static final double DEFAULT_DISTANCE_THRESHOLD = 1; // m
	public static final long DEFAULT_TIME_PEAK_THRESHOLD = 4; // minutes
	public static final long DEFAULT_ACCELERATION_PEAK_THRESHOLD = 50; // ms2
	public final double CO2 = 163; // g

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final MathContext mc = new MathContext(5, RoundingMode.HALF_UP);
	@Value("${spring.profiles.active}")
	private String env;

	@Autowired
	private Bugsnag bugsnag;
	@Autowired
	private GoogleMapsService googleMapsService;
	@Autowired
	private LogWallService logWallService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private SensorService sensorService;
	@Autowired
	private UserService userService;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private AchievementService achievementService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private SessionPointService sessionPointService;
	@Autowired
	private PartialRepository partialRepository;

	public Session performFirstValidation(Session session) {
		// Setting default values
		if(session.getType() == null) {
			session.setType(SessionType.BIKE);
		}
		if(session.getValid() == null) {
			session.setValid(false);
		}
		if(session.getStatus() == null) {
			session.setStatus(SessionStatus.VALID);
		}
		if(session.getCertificated() == null) {
			session.setCertificated(false);
		}
		if(session.getHomeWorkPath() == null) {
			session.setHomeWorkPath(false);
		}
		if(session.getSessionPoints() == null) {
			session.setSessionPoints(new LinkedList<>());
		}
		// Distances
		List<Partial> partials = filterPartials(session);
		Distances distances = calculateDistances(partials, DEFAULT_DISTANCE_THRESHOLD);
		session.setGyroDistance(distances.getGyroDistance());
		session.setGpsDistance(distances.getGpsDistance());
		session.setGmapsDistance(distances.getGmapsDistance());
		session.setGmapsPolyline(distances.getGmapsPolyline());
		session.setRawPolyline(distances.getRawPolyline());
		session.setPolyline(distances.getGmapsPolyline() != null && !distances.getGmapsPolyline().isEmpty() && isInPercent(session.getGmapsDistance(), session.getGpsDistance(), DEFAULT_GMAPS_POLYLINE_DEVIATION) ? session.getGmapsPolyline() : session.getRawPolyline());
		// Validation
		Validation validation = checkValid(partials, DEFAULT_VALID_PARTIAL_DEVIATION, DEFAULT_SPEED_THRESHOLD, DEFAULT_ON_FOOT_THRESHOLD,
				DEFAULT_TIME_PEAK_THRESHOLD, DEFAULT_VALID_PARTIAL_QTY);

		boolean isFullDistance = (session.getGpsDistance() != null && session.getGyroDistance() != null && session.getGyroDistance().compareTo(BigDecimal.ZERO) > 0 && isInPercent(session.getGpsDistance(), session.getGyroDistance(), DEFAULT_VALID_SESSION_DEVIATION));
		session.setValid(isFullDistance || validation.getValid());
		session.setCertificated((session.getValid() && isFullDistance) || validation.getCertificated());
		session.setStatus(isFullDistance ? SessionStatus.VALID : validation.getStatus());
		session.setType(isFullDistance ? SessionType.BIKE : validation.getType());

//		session.setDuration(calculateDuration(session));
		session.setNationalKm(Optional.ofNullable(session.getGyroDistance()).orElse(BigDecimal.ZERO).add(Optional.ofNullable(session.getGpsOnlyDistance()).orElse(BigDecimal.ZERO)).max(session.getGpsDistance()));

		// Sensor
		if(session.getSensor() == null) {
			sensorService.getActiveSensor(session.getUser()).ifPresent(sensor -> {
				sessionService.assignSensor(session, sensor);
			});
		} else if(session.getFirmware() != null) {
			sensorService.getActiveSensor(session.getUser()).ifPresent(value -> {
				value.setFirmware(session.getFirmware());
				sensorService.save(value);
			});
		}
		// App Version
		if(session.getAppVersion() == null) {
			userService.getActivePhone(session.getUser()).ifPresent(
					smartphone -> {
						session.setAppVersion(smartphone.getAppVersion());
						session.setPlatform(smartphone.getPlatform());
						session.setPhoneModel(smartphone.getModel());
					}
			);
		}
		if(session.getValid()) {
			// Co2
			session.setCo2(calculateCo2(session.getGpsDistance().doubleValue()));
			// Check if Home-Work
			HomeWorkCheck check = checkIsHomeWork(session.getUser(), partials);
			session.setHomeWorkPath(check.isHomeWork);
			session.setHomeAddress(check.getHomeAddress());
			session.setWorkAddress(check.getWorkAddress());
			session.setNationalPoints(addNationalPoints(session.getUser(), session.getNationalKm()));
			// SessionPoints
			if(session.getCertificated()) {
				//TODO da rimuovere
				long mockOrg = env.equalsIgnoreCase("prod") ? 2L : 19L;
				organizationService.findActiveByUserAndOrganization(session.getUser(), organizationService.findById(mockOrg).get()).ifPresent(enrollment -> {
					if(session.getSessionPoints().stream().noneMatch(sp -> sp.getOrganization().getId().equals(enrollment.getOrganization().getId()))) {
						session.getSessionPoints().add(new SessionPoint(session, enrollment.getOrganization(), session.getNationalPoints(), session.getNationalKm(), 1D));
					}
				});
				sessionService.assignPoints(session);
				session.setEuro(session.getSessionPoints().stream().map(sp -> Optional.ofNullable(sp.getEuro()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add));
				achievementService.updateAchievements(session.getUser());
			}
			// LogWall
			if(session.getValidatedDate() == null) {
				try {
					logWallService.writeLog(session);
				} catch(RuntimeException e) {
					logger.error(e.getMessage(), e);
					bugsnag.notify(e);
				}
			}
		} else {
			session.setCo2(0D);
			session.setHomeWorkPath(false);
			session.setNationalPoints(BigDecimal.ZERO);
			Optional.ofNullable(session.getSessionPoints()).orElse(new LinkedList<>()).forEach(sp -> {
				sp.setPoints(BigDecimal.ZERO);
				sp.setEuro(BigDecimal.ZERO);
				sessionPointService.save(sp);
			});
		}
		session.setValidatedDate(LocalDateTime.now());
		return session;
	}

	public List<SessionPoint> calculateUrbanPoints(Session session) {
		List<SessionPoint> points = new LinkedList<>();
		userService.getActiveEnrollmentsAt(session.getUser(), session.getStartTime().toLocalDate()).forEach(e -> {
			if(e.getOrganization().getGeojson() != null) {
				try {
					BigDecimal urbanKm = sessionService.distanceInPolygon(e.getOrganization().getGeojson(), filterPartials(session));
					points.add(new SessionPoint(session, e.getOrganization(), urbanKm.movePointRight(1).setScale(0, RoundingMode.FLOOR), urbanKm, Optional.ofNullable(session.getMultiplier()).orElse(1D)));
				} catch(JsonProcessingException ex) {
					bugsnag.notify(ex);
				}
			}
		});
		return points;
	}

	public Double calculateCo2(double distance) {
		//TODO per ora il calcolo è statico
		return distance * CO2;
	}

	public HomeWorkCheck checkIsHomeWork(User user, List<Partial> partials) {
		HomeWorkCheck check = new HomeWorkCheck();
		HomeWorkPath path = findHomeWorkPath(addressService.getActiveHomeWorkPaths(user), partials);
		check.setHomeWork(path != null);
		if(path != null) {
			check.setHomeAddress(path.getHomeAddress());
			check.setWorkAddress(addressService.getActiveWorkAddresses(user).stream().filter(address -> address.getSeat().equals(path.getSeat())).findFirst().get());
		}
		return check;
	}

	// Incremento punti nazionali
	public BigDecimal addNationalPoints(User user, BigDecimal nationalKm) {
		BigDecimal points = nationalKm.movePointRight(1).setScale(0, RoundingMode.FLOOR);
		user.setPoints(Optional.ofNullable(user.getPoints()).orElse(BigDecimal.ZERO).add(points));
		user.setEarnedNationalPoints(Optional.ofNullable(user.getEarnedNationalPoints()).orElse(BigDecimal.ZERO).add(points));
		userService.save(user);
		return points;
	}

	public List<Partial> filterPartials(Session session) {
		List<Partial> filtered = new LinkedList<>();
		boolean pause = false;
		for(Partial partial : session.getPartials()) {
			partial.setSession(session);
			if(partial.getType() == null) {
				partial.setType(PartialType.UNKNOWN);
			}
//			partial.setGpsDistance(null);
//			partial.setGmapsDistance(null);
			if(pause && partial.getType().equals(PartialType.IN_PROGRESS)) {
				partial.setType(PartialType.SKIPPED);
				partial.setStatus(SessionStatus.ERROR);
				partial.setValid(null);
				partialRepository.save(partial);
			} else if(!sessionService.isDebugPartial(partial)) {
				partial.setValid(true);
				partial.setStatus(SessionStatus.VALID);
				filtered.add(partial);
			}
			pause = partial.getType().equals(PartialType.PAUSE) || (pause && !sessionService.isStartingPartial(partial));
		}
		return filtered;
	}

	public Distances calculateDistances(List<Partial> partials, Double distanceThreshold) {
		List<List<Partial>> subsessions = new LinkedList<>();
		int currentWaypointsList = -1;
		Distances distances = new Distances();

		for(int i = 0; i < partials.size(); i++) {
			Partial partial = partials.get(i);
			Partial prevPartial = i > 0 ? partials.get(i - 1) : null;
			if(prevPartial == null || sessionService.isStartingPartial(partial)) {
				//Add starting point for each subsession
				++currentWaypointsList;
				subsessions.add(new LinkedList<>());
				subsessions.get(currentWaypointsList).add(partial);
				partial.setValid(true);
				partial.setGpsDistance(BigDecimal.ZERO);
				partial.setGmapsDistance(BigDecimal.ZERO);
			} else {
				double distance;
				try {
					distance = UtilitiesService.distance(prevPartial.getLatitude(), prevPartial.getLongitude(), partial.getLatitude(), partial.getLongitude());
				} catch(NullPointerException e) {
					logger.error("{}: {}", partial.getId(), e.getMessage());
					distance = 0.0;
				}
				if(prevPartial.getGpsDistance() != null) {
					double accel = UtilitiesService.acceleration(prevPartial.getGpsDistance().doubleValue() * 1000, distance, partial.getTimestamp().until(prevPartial.getTimestamp(), SECONDS));
					partial.setGpsDistance(new BigDecimal(distance, mc).movePointLeft(3));
					if(partial.getType().equals(PartialType.IN_PROGRESS) && Math.abs(accel) > DEFAULT_ACCELERATION_PEAK_THRESHOLD) {
						partial.setGmapsDistance(null);
						partial.setType(PartialType.SKIPPED);
						partial.setStatus(SessionStatus.ACCELERATION_PEAK);
						partialRepository.save(partial);
					} else if(partial.getType().equals(PartialType.IN_PROGRESS) && distance < distanceThreshold) {
						partial.setGmapsDistance(null);
						partial.setType(PartialType.SKIPPED);
						partial.setStatus(SessionStatus.DISTANCE_ERROR);
						partialRepository.save(partial);
					} else if(distance > 0) {
						subsessions.get(currentWaypointsList).add(partial);
						distances.addGpsDistance(partial.getGpsDistance()); //updating session gps distance only if the value is valid
					}
				} else {
					partial.setGpsDistance(new BigDecimal(distance, mc).movePointLeft(3));
					partial.setGmapsDistance(BigDecimal.ZERO);
				}
				if(partial.getSensorDistance() != null) {
					distances.addGyroDistance(partial.getSensorDistance());
				}
			}
		}
		for(List<Partial> subsession : subsessions) {
			String rawPolyline = Optional.ofNullable(distances.getRawPolyline()).orElse("");
			distances.setRawPolyline(rawPolyline + (rawPolyline.isEmpty() ? "" : "€") + googleMapsService.getEncodedPolyline(subsession));
			try {
				WaypointsList.WaypointsListResult result = googleMapsService.generateWaypointsListResult(subsession);
				Iterator<DirectionsLeg> legs = result.getDirectionsLegs().iterator();
				for(Partial partial : subsession) {
					if(!sessionService.isStartingPartial(partial)) {
						partial.setGmapsDistance(new BigDecimal(legs.next().distance.inMeters, mc).movePointLeft(3));
						distances.addGmapsDistance(partial.getGmapsDistance());
					}
				}
				String gmapsPolyline = Optional.ofNullable(distances.getGmapsPolyline()).orElse("");
				distances.setGmapsPolyline(gmapsPolyline + (gmapsPolyline.isEmpty() ? "" : "€") + result.getPolyline());
			} catch(Exception e) {
				logger.error(e.getMessage());
				bugsnag.notify(e);
				subsession.forEach(p -> p.setGmapsDistance(null));
			}
		}
		return distances;
	}

	public Validation checkValid(List<Partial> partials, Double partialDeviation,
	                             Double speedThreshold, Double onFootThreshold,
	                             Long timeThreshold, Double partialQty) {
		if(partials == null || partials.isEmpty()) {
			return new Validation(false, false, SessionStatus.ERROR, SessionType.BIKE);
		}

		LocalDateTime oldTime = partials.get(0).getTimestamp();
		long timeCounter = 0L;
		boolean onFoot = true;
		int validPartials = 0;
		int certifiedPartials = 0;
		Validation validation = new Validation(true, true, SessionStatus.VALID, SessionType.BIKE);
		for(Partial partial : partials) {
			// Setting default values
			if(partial.getValid() == null) {
				partial.setValid(true);
			}
			if(partial.getStatus() == null) {
				partial.setStatus(SessionStatus.VALID);
			}
			if(!sessionService.isStartingPartial(partial) && !partial.getType().equals(PartialType.SKIPPED)) {
				// Checking distance measurements accuracy
				if(partial.getSensorDistance() != null) {
					if(validateDistancePartial(partial, partialDeviation)) {
						certifiedPartials++;
						partial.setValid(true);
						partial.setStatus(SessionStatus.CERTIFICATED);
					} else {
						partial.setValid(false);
						partial.setStatus(SessionStatus.DISTANCE_ERROR);
					}
				}
				// Checking partial speed peak
				if(!partial.getTimestamp().isEqual(oldTime)) {
					if(validateSpeedPartial(partial, oldTime, speedThreshold)) {
						timeCounter = 0;    // if speed is OK, counter is raised to 0
					} else {
						timeCounter = oldTime.until(partial.getTimestamp(), MILLIS);    // otherwise, the counter is incremented
					}
					if(onFoot) {
						onFoot = validateSpeedPartial(partial, oldTime, onFootThreshold);
					}
				}
				oldTime = partial.getTimestamp();
				if(TimeUnit.MILLISECONDS.toMinutes(timeCounter) >= timeThreshold) {    // if peak lasts >= 4 minutes
					validation.setValid(false);
					validation.setStatus(SessionStatus.SPEED_ERROR);
					partial.setValid(false);
					partial.setStatus(SessionStatus.SPEED_ERROR);
				}
			}
//          Updating valid partials counter
			if(Optional.ofNullable(partial.getValid()).orElse(false)) {
				validPartials++;
			}
		}
		// Checking total valid partials
		boolean isValid = (double) validPartials / partials.size() > partialQty;
		if(!isValid && validation.getValid()) {
			validation.setStatus(SessionStatus.DISTANCE_ERROR);
			validation.setValid(false);
		}
		boolean setCertificated = (double) certifiedPartials / partials.size() > partialQty;
		validation.setCertificated(setCertificated);
		if(!setCertificated) validation.setStatus(SessionStatus.NOT_CERTIFICATED);
		validation.setType((onFoot && partials.stream().allMatch(p -> p.getDeltaRevs() == null)) ? SessionType.FOOT : SessionType.BIKE);
		return validation;
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

	/**
	 * Checks whether the session is a home to work journey or the opposite, compared to each path of the list given
	 *
	 * @param paths    paths list where find path from
	 * @param partials session's partials to check
	 * @return path found
	 */
	public HomeWorkPath findHomeWorkPath(Set<HomeWorkPath> paths, List<Partial> partials) {
		if(paths == null || paths.size() == 0) {
			return null;
		}
		Partial start = partials.get(0);
		Partial end = partials.get(partials.size() - 1);
		double homeTolerance, workTolerance;
		for(HomeWorkPath path : paths) {
			homeTolerance = organizationSettingsService.get(path.getSeat().getOrganization(), "homeWorkPointsTolerance", Double.class);
			workTolerance = Optional.ofNullable(path.getSeat().getDestinationTolerance()).orElse(BigDecimal.valueOf(homeTolerance)).doubleValue();
			if(validateHomeWorkPath(start, end, path, homeTolerance, workTolerance)
					|| validateHomeWorkPath(end, start, path, homeTolerance, workTolerance)) {
				return path;
			}
		}
		return null;
	}

	/**
	 * Checks whether starting and ending point are nearby home and seat address and if distance is compatible with the path data given
	 *
	 * @param start         start partial
	 * @param end           end partial
	 * @param path          path to check
	 * @param homeTolerance
	 * @return true if start and end partials given are compatible with path
	 */
	public boolean validateHomeWorkPath(Partial start, Partial end, HomeWorkPath path, double homeTolerance, double workTolerance) {
		boolean s = distanceKm(start.getLatitude(), start.getLongitude(), path.getHomeAddress().getLatitude(), path.getHomeAddress().getLongitude()) <= homeTolerance;
		boolean e = distanceKm(end.getLatitude(), end.getLongitude(), path.getSeat().getLatitude(), path.getSeat().getLongitude()) <= workTolerance;
		return s && e;
	}

	public boolean validateSpeedPartial(Partial partial, LocalDateTime timeReference) {
		return validateSpeedPartial(partial, timeReference, DEFAULT_SPEED_THRESHOLD);
	}

	public int calculateDuration(Session session) {
		int duration = 0;
		Partial partial, prevPartial;
		List<Partial> partials = session.getPartials();
		for(int i = 1; i < partials.size(); i++) {
			partial = partials.get(i);
			prevPartial = partials.get(i - 1);
			if(!sessionService.isStartingPartial(partial)) {
				duration += (int) Duration.between(prevPartial.getTimestamp(), partial.getTimestamp()).getSeconds();
			}
		}
		return duration;
	}

	static class Distances {
		private BigDecimal gpsDistance = BigDecimal.ZERO;
		private BigDecimal gmapsDistance = BigDecimal.ZERO;
		private BigDecimal gyroDistance = BigDecimal.ZERO;
		private String gmapsPolyline = "";
		private String rawPolyline = "";

		public BigDecimal getGpsDistance() {
			return gpsDistance;
		}

		public void addGpsDistance(BigDecimal gpsDistance) {
			this.gpsDistance = this.gpsDistance.add(gpsDistance);
		}

		public BigDecimal getGmapsDistance() {
			return gmapsDistance;
		}

		public void addGmapsDistance(BigDecimal gmapsDistance) {
			this.gmapsDistance = this.gmapsDistance.add(gmapsDistance);
		}

		public BigDecimal getGyroDistance() {
			return gyroDistance;
		}

		public void addGyroDistance(BigDecimal gyroDistance) {
			this.gyroDistance = this.gyroDistance.add(gyroDistance);
		}

		public String getGmapsPolyline() {
			return gmapsPolyline;
		}

		public void setGmapsPolyline(String gmapsPolyline) {
			this.gmapsPolyline = gmapsPolyline;
		}

		public String getRawPolyline() {
			return rawPolyline;
		}

		public void setRawPolyline(String rawPolyline) {
			this.rawPolyline = rawPolyline;
		}
	}

	static class Validation {
		private Boolean valid;
		private Boolean certificated;
		private SessionStatus status;
		private SessionType type;

		public Validation(Boolean valid, Boolean certificated, SessionStatus status, SessionType type) {
			this.valid = valid;
			this.certificated = certificated;
			this.status = status;
			this.type = type;
		}

		public Boolean getValid() {
			return valid;
		}

		public void setValid(Boolean valid) {
			this.valid = valid;
		}

		public Boolean getCertificated() {
			return certificated;
		}

		public void setCertificated(Boolean certificated) {
			this.certificated = certificated;
		}

		public SessionStatus getStatus() {
			return status;
		}

		public void setStatus(SessionStatus status) {
			this.status = status;
		}

		public SessionType getType() {
			return type;
		}

		public void setType(SessionType type) {
			this.type = type;
		}
	}

	static class HomeWorkCheck {
		private boolean isHomeWork;
		private HomeAddress homeAddress;
		private WorkAddress workAddress;

		public boolean isHomeWork() {
			return isHomeWork;
		}

		public void setHomeWork(boolean homeWork) {
			isHomeWork = homeWork;
		}

		public HomeAddress getHomeAddress() {
			return homeAddress;
		}

		public void setHomeAddress(HomeAddress homeAddress) {
			this.homeAddress = homeAddress;
		}

		public WorkAddress getWorkAddress() {
			return workAddress;
		}

		public void setWorkAddress(WorkAddress workAddress) {
			this.workAddress = workAddress;
		}
	}
}
