package net.nextome.lismove;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.*;
import net.nextome.lismove.rest.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Generator {
	public static User generateUser(String username, String uid) {
		User user = generateUser(username);
		user.setUid(uid);
		return user;
	}

	public static User generateUser(String username) {
		User u = new User();
		u.setUid(UUID.randomUUID().toString());
		if(username != null) {
			u.setUsername(username);
		} else {
			u.setUsername(u.getUid().substring(u.getUid().length() - 6));
		}
		u.setFirstName("Nome");
		u.setLastName("Cognome");
		u.setEmail(u.getUsername() + "@test.test");
		u.setEarnedNationalPoints(BigDecimal.ZERO);
		return u;
	}

	public static User generateUser() {
		return generateUser(null);
	}

	public static User generateLismover(String username, String gender, LocalDate birth, BigDecimal natPoints) {
		User u = generateUser(username);
		u.setEmail("test@test.test");
		u.setBirthDate(birth);
		u.setGender(gender);
		u.setUserType(UserType.ROLE_LISMOVER);
		u.setEarnedNationalPoints(natPoints);
		return u;
	}

	public static LismoverUserDto generateLismoverDto(String username, String email) {
		LismoverUserDto user = new LismoverUserDto();
//            Account
		user.setUid(UUID.randomUUID().toString());
		if(username != null) {
			user.setUsername(username);
		} else {
			user.setUsername(user.getUid().substring(user.getUid().length() - 6));
		}
		user.setFirstName("Mario");
		user.setLastName("Rossi");
		user.setEmail(email);
		user.setBirthDate(946681200000L);
		user.setGender("m");
		user.setActivePhone("123456789");
		user.setHomeAddress("Via Dante Alighieri");
		user.setHomeNumber("5");
		user.setHomeCity(72006L);
		return user;
	}

	public static ManagerDto generateManager(String username) {
		ManagerDto m = new ManagerDto();
		m.setFirstName("fname1");
		m.setLastName("lname1");
		m.setEmail("a@a.a");
		m.setUsername(username);
		m.setPassword("managerPass");
		return m;
	}

	public static OrganizationDto generateOrganizationDto(Integer type) {
		OrganizationDto o = new OrganizationDto();
		o.setType(type);
		o.setTitle("Organizzazione " + type);
		return o;
	}

	public static Organization generateOrganization(String title, OrganizationType type) {
		Organization o = new Organization();
		o.setType(type);
		o.setTitle(title);
		return o;
	}

	public static CustomFieldValue generateCustomFieldValue(CustomField cf, Enrollment enrollment, Boolean value) {
		CustomFieldValue cfv = new CustomFieldValue();
		cfv.setCustomField(cf);
		cfv.setValue(value);
		cfv.setEnrollment(enrollment);
		return cfv;
	}

	public static SeatDto generateSeatDto(Long organization) {
		SeatDto seat = new SeatDto();
		seat.setAddress("Via Gorizia");
		seat.setNumber("13");
		seat.setCity(72006L);
		seat.setOrganization(organization);
		seat.setLatitude(41.118813);
		seat.setLongitude(16.884188);
		return seat;
	}

	public static Seat generateSeat(String address, String number, City city) {
		return generateSeat(address, number, city, null, null);
	}

	public static Seat generateSeat(String address, String number, City city, Double latitude, Double longitude) {
		Seat a = new Seat();
		a.setAddress(address);
		a.setNumber(number);
		a.setCity(city);
		a.setLatitude(latitude);
		a.setLongitude(longitude);
		return a;
	}

	public static City generateCity(Long id, String name) {
		City city = new City();
		city.setCity(name);
		city.setIstatId(id);
		city.setCap("701xx");
		city.setProvince("BA");
		return city;
	}

	public static Session generateSession(User user, Double points, LocalDateTime start, List<Organization> orgs, WorkAddress workAddress) {
		Session session = new Session();
		session.setUser(user);
		session.setWorkAddress(workAddress);
		session.setGyroDistance(new BigDecimal("10.0"));
		session.setNationalPoints(BigDecimal.valueOf(100.0));
		session.setNationalKm(new BigDecimal("10.0"));
		session.setValid(true);
		session.setStatus(SessionStatus.VALID);
		session.setHomeWorkPath(workAddress != null);
		session.setStartTime(start);
		session.setEndTime(session.getStartTime().plusMinutes(20));
		session.setType(SessionType.BIKE);

		session.setSessionPoints(generateSessionPoints(session, orgs, BigDecimal.valueOf(Optional.ofNullable(points).orElse(0D)), BigDecimal.valueOf(5)));
		return session;
	}

	public static Session generateSession(User user, LocalDateTime start, List<Organization> orgs, WorkAddress workAddress, Boolean isHomeWorkPath, BigDecimal natKm, BigDecimal points, SessionType type) {
		Session s = generateSession(user, null, start, orgs, workAddress);
		s.setHomeWorkPath(isHomeWorkPath);
		s.setNationalPoints(points);
		s.setNationalKm(natKm);
		s.setType(type);
		s.setSessionPoints(generateSessionPoints(s, orgs, points, natKm.multiply(BigDecimal.valueOf(0.5))));
		return s;
	}

	public static List<SessionPoint> generateSessionPoints(Session session, List<Organization> orgs, BigDecimal points, BigDecimal distance) {
		List<SessionPoint> sp = new LinkedList<>();
		for(Organization oid : orgs) {
			SessionPoint sessionPoint = new SessionPoint();
			sessionPoint.setOrganization(oid);
			sessionPoint.setPoints(points);
			sessionPoint.setDistance(distance);
			sessionPoint.setMultiplier(1.0);
			sessionPoint.setSession(session);
			sp.add(sessionPoint);
		}
		return sp;
	}

	public static Enrollment generateEnrollment(User user, Organization org, LocalDate startDate, LocalDate endDate) {
		Enrollment enrollment = new Enrollment();
		enrollment.setUser(user);
		enrollment.setOrganization(org);
		enrollment.setActivationDate(LocalDateTime.now());
		enrollment.setStartDate(startDate);
		enrollment.setEndDate(endDate);
		return enrollment;
	}

	public static HomeAddress generateHomeAddress(String address, String number, City city, Double latitude, Double longitude) {
		HomeAddress h = new HomeAddress();
		h.setAddress(address);
		h.setNumber(number);
		h.setCity(city);
		h.setLatitude(latitude);
		h.setLongitude(longitude);
		return h;
	}

	public static Ranking generateRanking(Organization org, LocalDate start, LocalDate end, RankingValue value, RankingFilter filter, String filterValue) {
		Ranking r = new Ranking();
		r.setOrganization(org);
		r.setValue(value);
		r.setFilter(filter);
		r.setFilterValue(filterValue);
		r.setStartDate(start);
		r.setEndDate(end);
		return r;
	}

	public static AwardRanking generateAwardRanking(AwardType type, BigDecimal value, Ranking ranking, Integer pos) {
		AwardRanking award = new AwardRanking();
		award.setType(type);
		award.setValue(value);
		award.setRanking(ranking);
		award.setPosition(pos);
		return award;
	}

	public static AwardRanking generateAwardRanking(AwardType type, BigDecimal value, Ranking ranking, String range) {
		AwardRanking award = new AwardRanking();
		award.setType(type);
		award.setValue(value);
		award.setRanking(ranking);
		award.setRange(range);
		return award;
	}

	public static Achievement generateAchievement(LocalDate start, LocalDate end, Organization org, RankingValue value, BigDecimal target, RankingFilter filter, String filterValue) {
		Achievement a = new Achievement();
		a.setStartDate(start);
		a.setEndDate(end);
		a.setDuration(7);
		a.setValue(value);
		a.setOrganization(org);
		a.setTarget(target);
		a.setFilter(filter);
		a.setFilterValue(filterValue);
		return a;
	}

	public static AwardAchievement generateAwardAchievement(AwardType type, BigDecimal value, Achievement achievement) {
		AwardAchievement award = new AwardAchievement();
		award.setType(type);
		award.setValue(value);
		award.setAchievement(achievement);
		return award;
	}

	public static AwardPosition generateAwardPosition(AwardType type, BigDecimal value, LocalDate start, LocalDate end, BigDecimal radius, String address, String number, City city, Double lat, Double lng) {
		AwardPosition award = new AwardPosition();
		award.setType(type);
		award.setValue(value);
		award.setStartDate(start);
		award.setEndDate(end);
		award.setRadius(radius);
		award.setAddress(address);
		award.setNumber(number);
		award.setCity(city);
		award.setLatitude(lat);
		award.setLongitude(lng);
		return award;
	}

	public static OrganizationSettingValueDto generateOrganizationSetting(Long organization, String setting, String value) {
		OrganizationSettingValueDto dto = new OrganizationSettingValueDto();
		dto.setOrganization(organization);
		dto.setOrganizationSetting(setting);
		dto.setValue(value);
		return dto;
	}
}
