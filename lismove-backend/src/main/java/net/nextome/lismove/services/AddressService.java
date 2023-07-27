package net.nextome.lismove.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.bugsnag.Bugsnag;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.LatLng;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.repositories.HomeAddressRepository;
import net.nextome.lismove.repositories.HomeWorkPathRepository;
import net.nextome.lismove.repositories.SeatRepository;
import net.nextome.lismove.repositories.WorkAddressRepository;
import net.nextome.lismove.rest.dto.AddressOverviewDto;
import net.nextome.lismove.rest.dto.SeatDto;
import net.nextome.lismove.rest.mappers.AddressMapper;
import net.nextome.lismove.rest.mappers.SeatMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import net.nextome.lismove.services.utils.WaypointsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressService extends UtilitiesService {
	public static final String SECRET_JWT_KEY = "";

	@Autowired
	private HomeAddressRepository homeAddressRepository;
	@Autowired
	private WorkAddressRepository workAddressRepository;
	@Autowired
	private HomeWorkPathRepository homeWorkPathRepository;
	@Autowired
	private SeatRepository seatRepository;
	@Autowired
	private SeatMapper seatMapper;
	@Autowired
	private AddressMapper addressMapper;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private GoogleMapsService googleMapsService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private Bugsnag bugsnag;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${my.server.address}")
	private String base_url;

	public void saveHomeAddress(HomeAddress address, User user) {
		if(!address.coordinatesNotNull()) {
			initCoordinates(address);
		}
		Optional<HomeAddress> old = getActiveHomeAddress(user);
		if(!old.isPresent() || !old.get().equals(address)) {
			old.ifPresent(a -> {
				a.setEndAssociation(LocalDateTime.now());
				homeAddressRepository.save(a);
			});
			address.setStartAssociation(LocalDateTime.now());
			address.setUser(user);
			user.setHomeAddress(address);
			homeAddressRepository.save(address);
		}
	}

	public void saveWorkAddresses(Set<SeatDto> newWorkAddresses, User user) {
		Set<WorkAddress> oldWorkAddresses = workAddressRepository.findByUserAndEndAssociationNull(user);
//        Disassociate old work addresses and deletes old paths
		for(WorkAddress work : oldWorkAddresses) {
			if(newWorkAddresses.stream().anyMatch(s -> s.getId() != null && s.getId().equals(work.getSeat().getId()))) {
				newWorkAddresses.removeIf(seatDto -> seatDto.getId() != null && seatDto.getId().equals(work.getSeat().getId()));
			} else {
				work.setEndAssociation(LocalDateTime.now());
			}
		}
		workAddressRepository.saveAll(oldWorkAddresses);
//        Create new work addresses and paths
		for(SeatDto dto : newWorkAddresses) {
			Optional<Seat> seat;
			if(dto.getId() != null) {
				seat = findSeatById(dto.getId());
				if(seat.isPresent()) {
					createWorkAddress(user, seat.get());
					if(seat.get().getValidatedAndNotNull()) {
						createPath(user, seat.get());
					}
				}
			}
		}
	}

	public WorkAddress createWorkAddress(User user, Seat seat) {
		WorkAddress work = new WorkAddress();
		work.setSeat(seat);
		work.setStartAssociation(LocalDateTime.now());
		work.setUser(user);
		return workAddressRepository.save(work);
	}

	public HomeWorkPath createPath(User user, Seat seat) {
		Optional<HomeAddress> homeAddress = getActiveHomeAddress(user);
		if(homeAddress.isPresent()) {
			HomeWorkPath path = new HomeWorkPath();
			path.setUser(user);
			path.setHomeAddress(homeAddress.get());
			path.setSeat(seat);
			Path simplePath = calculatePolylineAndDistance(homeAddress.get(), seat);
			path.setDistance(simplePath.getDistance());
			path.setPolyline(simplePath.getPolyline());
			return homeWorkPathRepository.save(path);
		}
		return null;
	}

	public void refreshHomeWorkPathsByUser(User user) {
		Set<HomeWorkPath> paths = homeWorkPathRepository.findByUser(user);
		Set<Seat> workAddressSeats = workAddressRepository.findByUserAndEndAssociationNull(user).stream().map(WorkAddress::getSeat).collect(Collectors.toSet());
//        Refresh old paths
		for(HomeWorkPath path : paths) {
			if(workAddressSeats.stream().anyMatch(seat -> seat.getId().equals(path.getSeat().getId()))) {
				Optional<HomeAddress> homeAddress = getActiveHomeAddress(user);
				if(homeAddress.isPresent() && !path.getHomeAddress().equals(homeAddress.get())
						&& path.getSeat().getValidatedAndNotNull() && !path.getSeat().getDeleted()) {
					path.setHomeAddress(homeAddress.get());
					Path simplePath = calculatePolylineAndDistance(path.getHomeAddress(), path.getSeat());
					path.setDistance(simplePath.getDistance());
					path.setPolyline(simplePath.getPolyline());
					homeWorkPathRepository.save(path);
				}
			} else {
				homeWorkPathRepository.delete(path);
			}
		}
//        Find if new paths are available
		for(Seat seat : workAddressSeats) {
			if(paths.stream().noneMatch(p -> p.getSeat().getId().equals(seat.getId())) && seat.getValidatedAndNotNull() && !seat.getDeleted()) {
				createPath(user, seat);
			}
		}
	}

	public void refreshHomeWorkPathsBySeat(Seat oldSeat, Seat newSeat) {
		homeWorkPathRepository.findBySeat(oldSeat).forEach(path -> {
			path.setSeat(newSeat);
			Path simplePath = calculatePolylineAndDistance(path.getHomeAddress(), path.getSeat());
			path.setDistance(simplePath.getDistance());
			path.setPolyline(simplePath.getPolyline());
			homeWorkPathRepository.save(path);
		});
	}

	public Seat requestSeat(User user, SeatDto dto) {
		Seat seat;
		Organization org = organizationService.findById(dto.getOrganization()).orElseThrow(() -> new LismoveException("Organization not found", HttpStatus.NOT_FOUND));

		seat = createSeat(org, dto);
		if(org.getType().equals(OrganizationType.PA) && (org.getValidation() != null && org.getValidation())) {
			createWorkAddress(user, seat);

			Algorithm algorithm = Algorithm.HMAC256(SECRET_JWT_KEY);
			String token1 = JWT.create()
					.withExpiresAt(Date.from(LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant()))
					.withClaim("seat", seat.getId())
					.withClaim("validate", true)
					.sign(algorithm);
			String token2 = JWT.create()
					.withExpiresAt(Date.from(LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant()))
					.withClaim("seat", seat.getId())
					.withClaim("validate", false)
					.sign(algorithm);

			Context ctx = new Context();
			ctx.setVariables(new HashMap<String, Object>() {{
				put("user", user.getUsername());
				put("id", seat.getId());
				put("name", seat.getName());
				put("address", seat.formatAddress());
				put("organization", org.getTitle());
				put("approveTkn", token1);
				put("rejectTkn", token2);
				put("url", base_url + "organizations/" + org.getId() + "/seats/" + seat.getId());
			}});

			emailService.send(
					org.getValidatorEmail(),
					"Seat validation requested by user: " + user.getUsername(),
					"seat-validation",
					ctx
			);
		}

		return seat;
	}

	public Seat createSeat(Organization org, SeatDto dto) {
		Seat seat = seatMapper.dtoToSeat(dto);
		if(dto.getLatitude() == null && dto.getLongitude() == null) {
			initCoordinates(seat);
		}
		for(Seat s : findAllSeatsByOrganization(org)) {
			if(equals(s, seat)) {
				return s;
			}
		}
		seat.setOrganization(org);
		seat.setDeleted(false);
		seat.setValidated(true);
		if(org.getType().equals(OrganizationType.PA) && org.getValidation() != null && org.getValidation()) {
			seat.setValidated(null);
		}
		return seatRepository.save(seat);
	}

	private boolean equals(Seat a, Seat b) {
		return a.equals(b) && Optional.ofNullable(a.getName()).orElse("").equalsIgnoreCase(Optional.ofNullable(b.getName()).orElse(""));
	}

	public Seat updateSeat(Seat old, SeatDto update) {
		Seat upd = seatMapper.dtoToSeat(update);
		Seat newSeat = new Seat();
		notNullBeanCopy(old, newSeat, "id");
		notNullBeanCopy(upd, newSeat, "id", "organization");
		if(!upd.coordinatesNotNull()) {
			initCoordinates(newSeat);
		}
		seatRepository.save(newSeat);
		old.setDeleted(true);
		seatRepository.save(old);
		findAllActiveWorkAddressesBySeat(old).forEach(workAddress -> {
			workAddress.setEndAssociation(LocalDateTime.now());
			workAddressRepository.save(workAddress);
			createWorkAddress(workAddress.getUser(), newSeat);
		});
		refreshHomeWorkPathsBySeat(old, newSeat);
		return newSeat;
	}

	public void deleteSeat(Seat seat) {
//        Delete all paths
		homeWorkPathRepository.deleteAllBySeat(seat);
//        Disassociate all work addresses
		for(WorkAddress work : workAddressRepository.findBySeat(seat)) {
			work.setEndAssociation(LocalDateTime.now());
			workAddressRepository.save(work);
		}
		seat.setDeleted(true);
		seatRepository.save(seat);
	}

	public void approveSeat(Seat seat) {
		if(seat.getValidated() != null) {
			throw new LismoveException("Seat already validated or rejected", HttpStatus.BAD_REQUEST);
		}
		seat.setValidated(true);
		seatRepository.save(seat);
	}

	public void rejectSeat(Seat seat) {
		if(seat.getValidated() != null) {
			throw new LismoveException("Seat already validated or rejected", HttpStatus.BAD_REQUEST);
		}
		seat.setValidated(false);
		seatRepository.save(seat);
	}

	private void initCoordinates(Address address) {
		LatLng latLng = calculateLatLng(address);
		address.setLatitude(latLng.lat != 0 ? latLng.lat : null);
		address.setLongitude(latLng.lng != 0 ? latLng.lng : null);
	}

	public LatLng calculateLatLng(Address address) {
		LatLng latLng = new LatLng(0,0);
		try {
			latLng = googleMapsService.generateLatLng(address);
		} catch(Exception e) {
			logger.error(e.getMessage());
			bugsnag.notify(e);
		}
		return latLng;
	}

	public Path calculatePolylineAndDistance(Address home, Address work) {
		Path path = new Path();
		if (home != null && work != null
				&& home.coordinatesNotNull() && work.coordinatesNotNull()) {
			WaypointsList list = new WaypointsList() {{
				add(new LatLng(home.getLatitude(), home.getLongitude()));
				add(new LatLng(work.getLatitude(), work.getLongitude()));
			}};
			WaypointsList.WaypointsListResult result;
			try {
				result = googleMapsService.generateWaypointsListResult(list);
				path.setDistance(BigDecimal.ZERO);
				for (DirectionsLeg leg : result.getDirectionsLegs()) {
					path.setDistance(path.getDistance().add(BigDecimal.valueOf(leg.distance.inMeters).divide(BigDecimal.valueOf(1000), new MathContext(5, RoundingMode.HALF_UP))));
				}
				path.setPolyline(result.getPolyline());
			} catch (IOException | InterruptedException | ApiException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	public Optional<Seat> findSeatById(Long sid) {
		Optional<Seat> seat = seatRepository.findByIdAndDeletedFalse(sid);
		seat.ifPresent(a -> {
			if (!a.coordinatesNotNull()) {
				initCoordinates(a);
			}
		});
		return seat;
	}

	public Set<Seat> findAllSeatsByOrganization(Organization org) {
		Set<Seat> seats = seatRepository.findAllByOrganizationAndDeletedFalse(org);
		seats.forEach(a -> {
			if (!a.coordinatesNotNull()) {
				initCoordinates(a);
			}
		});
		return seats;
	}

	public Set<WorkAddress> findAllActiveWorkAddressesBySeat(Seat seat) {
		Set<WorkAddress> addresses = workAddressRepository.findAllByEndAssociationIsNullAndSeat(seat);
		addresses.forEach(a -> {
			if (!a.getSeat().coordinatesNotNull()) {
				initCoordinates(a.getSeat());
			}
		});
		return addresses;
	}

	public Optional<HomeAddress> getActiveHomeAddress(User u) {
		Optional<HomeAddress> address = homeAddressRepository.findByUserAndEndAssociationNull(u);
		address.ifPresent(a -> {
			if (!a.coordinatesNotNull()) {
				initCoordinates(a);
			}
		});
		return address;
	}

	public Set<AddressOverviewDto> getActiveHomeAddressesAt(User u, Long timestamp) {
		HomeAddress homeAddress;
		List<Enrollment> enrollments;
		Set<AddressOverviewDto> addresses = new HashSet<>();
		if (timestamp != null) {
			homeAddress = getActiveHomeAddressAt(u, timestamp).orElse(null);
		} else {
			homeAddress = getActiveHomeAddress(u).orElse(null);
		}
		if (homeAddress != null) {
			if (timestamp != null) {
				enrollments = organizationService.findActivesByUserAt(u, LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
			} else {
				enrollments = organizationService.getUserEnrollments(u.getUid());
			}
			enrollments.forEach(e -> {
				Double tolerance = organizationSettingsService.get(e.getOrganization(), "homeWorkPointsTolerance", Double.class);
				if (addresses.stream().noneMatch(dto -> dto.getTolerance().equals(tolerance))) {
					AddressOverviewDto a = addressMapper.homeAddressToDto(homeAddress);
					a.setTolerance(tolerance);
					addresses.add(a);
				}
			});
		}
		return addresses;
	}

	public Optional<HomeAddress> getActiveHomeAddressAt(User u, Long timestamp) {
		Optional<HomeAddress> address;
		if (timestamp == null) {
			address = getActiveHomeAddress(u);
		} else {
			address = homeAddressRepository.findActiveByUserAt(u, LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
		}
		address.ifPresent(a -> {
			if (!a.coordinatesNotNull()) {
				initCoordinates(a);
			}
		});
		return address;
	}

	public Set<WorkAddress> getActiveWorkAddresses(User u) {
		Set<WorkAddress> addresses = workAddressRepository.findByUserAndEndAssociationNull(u);
		addresses.forEach(a -> {
			if (!a.getSeat().coordinatesNotNull()) {
				initCoordinates(a.getSeat());
			}
		});
		return addresses;
	}

	public Set<WorkAddress> getActiveWorkAddressesAt(User u, Long timestamp) {
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
		Set<WorkAddress> addresses = workAddressRepository.findByUserAndStartAssociationLessThanEqualAndEndAssociationGreaterThanEqual(u, dateTime, dateTime);
		addresses.addAll(getActiveWorkAddresses(u).stream().filter(workAddress -> workAddress.getStartAssociation().isBefore(dateTime)).collect(Collectors.toSet()));
		addresses.forEach(a -> {
			if (!a.getSeat().coordinatesNotNull()) {
				initCoordinates(a.getSeat());
			}
		});
		return addresses;
	}

	public Set<HomeWorkPath> getActiveHomeWorkPaths(User u) {
		return homeWorkPathRepository.findByUser(u);
	}

	public List<HomeAddress> getHomeAddressesHistory(User u) {
		return homeAddressRepository.findByUserOrderByIdAsc(u);
	}

	public List<WorkAddress> getWorkAddressesHistory(User u) {
		return workAddressRepository.findByUserOrderByIdAsc(u);
	}

	static class Path {
		private Address start;
		private Address end;
		private BigDecimal distance;
		private String polyline;

		public Address getStart() {
			return start;
		}

		public void setStart(Address start) {
			this.start = start;
		}

		public Address getEnd() {
			return end;
		}

		public void setEnd(Address end) {
			this.end = end;
		}

		public BigDecimal getDistance() {
			return distance;
		}

		public void setDistance(BigDecimal distance) {
			this.distance = distance;
		}

		public String getPolyline() {
			return polyline;
		}

		public void setPolyline(String polyline) {
			this.polyline = polyline;
		}
	}

}
