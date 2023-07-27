package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.models.query.UserDashboardSession;
import net.nextome.lismove.models.query.UserRankingPosition;
import net.nextome.lismove.repositories.EnrollmentRepository;
import net.nextome.lismove.repositories.NotificationMessageDeliveryRepository;
import net.nextome.lismove.repositories.SmartphoneRepository;
import net.nextome.lismove.repositories.UserRepository;
import net.nextome.lismove.rest.dto.LismoverUserDto;
import net.nextome.lismove.rest.dto.UserDashboard;
import net.nextome.lismove.rest.dto.UserOverviewDto;
import net.nextome.lismove.rest.dto.VendorDto;
import net.nextome.lismove.rest.mappers.UserMapper;
import net.nextome.lismove.rest.mappers.VendorMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService extends UtilitiesService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SmartphoneRepository smartphoneRepository;
	@Autowired
	private EnrollmentRepository enrollmentRepository;
	@Autowired
	private AddressService addressService;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private CityService cityService;
	@Autowired
	private FirebaseAuthService firebaseAuthService;
	@Autowired
	private CustomFieldService customFieldService;
	@Autowired
	private VendorMapper vendorMapper;
	@Autowired
	private NotificationMessageDeliveryRepository notificationMessageDeliveryRepository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public User save(User user) {
		return userRepository.save(user);
	}

	public User create(User user) {
		if(user.getUsername() != null) {
			userRepository.findByUsername(user.getUsername()).ifPresent(u -> {
				throw new LismoveException("Username already registered", HttpStatus.BAD_REQUEST);
			});
		}
		//Controllo ridondante (eseguito già da Firebase)
		if(user.getEmail() != null) {
			userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
				throw new LismoveException("Email already registered", HttpStatus.BAD_REQUEST);
			});
		}
		userRepository.findByUid(user.getUid()).ifPresent(u -> {
			throw new LismoveException("User already created", HttpStatus.BAD_REQUEST);
		});
		return userRepository.save(user);
	}

	public User update(User upd, String password, User old, String... ignore) {
		if(upd.getUsername() == null && old.getUsername() == null) {
//			throw new LismoveException("Username must be set", HttpStatus.BAD_REQUEST);
		} else if(upd.getUsername() != null) {
			if(old.getUsername() == null) {
				userRepository.findByUsername(upd.getUsername()).ifPresent(u -> {
					throw new LismoveException("Username already registered", HttpStatus.BAD_REQUEST);
				});
			}
//			TODO: temporaneamente rimosso
//			else {
//				if(!upd.getUsername().equals(old.getUsername())) {
//					throw new LismoveException("Username cannot be modified", HttpStatus.BAD_REQUEST);
//				}
//			}
		}
		if(notNullAndNotEqual(old.getEmail(), upd.getEmail())) {
			userRepository.findByEmail(upd.getEmail()).ifPresent(u -> {
				if(!u.getUid().equals(old.getUid())) {
					throw new LismoveException("Email already registered", HttpStatus.BAD_REQUEST);
				}
			});
		}
		notNullBeanCopy(upd, old, ignore);
		if(notNullAndNotEqual(old.getEmail(), upd.getEmail()) || password != null && !password.isEmpty()) {
			firebaseAuthService.updateUser(old, password);
		}
		return userRepository.save(old);
	}

	public void registerLogin(String uid, String appVersion, String appOs) {
		userRepository.updateLastLoggedIn(uid);
		getActivePhone(findByUid(uid).orElseThrow(() -> new LismoveException("Utente non trovato"))).ifPresent(s -> {
			s.setAppVersion(appVersion);
			s.setPlatform(appOs);
			smartphoneRepository.save(s);
		});
	}

	//Bisogna rimuovere la transazionalità in questo metodo, perché altrimenti quando si prova a salvare lo smartphone,
	//lo user non è ancora stato salvato e viene lanciata un'eccezione da hibernate
	@Transactional(propagation = Propagation.NEVER)
	public User createLismover(LismoverUserDto u) {
		User user = userMapper.dtoToUser(u);
		user.setUserType(UserType.ROLE_LISMOVER);
		user.setEnabled(true);
		create(user);
		if(u.getActivePhone() != null) {
			Smartphone active = new Smartphone(u.getActivePhone(), user);
			active.setModel(u.getActivePhoneModel());
			active.setFcmToken(u.getActivePhoneToken());
			active.setAppVersion(u.getActivePhoneVersion());
			smartphoneRepository.save(active);
		}
		if(u.getHomeAddress() != null) {
			addressService.saveHomeAddress(prepareHomeAddress(u), user);
		}
		return userRepository.save(user);
	}

	public User createVendor(VendorDto dto) {
		User user = vendorMapper.dtoToUser(dto);
		user.setUserType(UserType.ROLE_VENDOR);
		user.setEnabled(true);
		//imposto come username un la prima parte della mail e un incrementale se gia esiste
		String username = user.getEmail().split("@")[0];
		int increment = userRepository.countByUsernameLike(username);
		if(increment != 0) {
			username += increment;
		}
		user.setUsername(username);
		return create(user);
	}

	public User updateLismover(User old, LismoverUserDto update) {
		User upd = userMapper.dtoToUser(update);
		update(upd, update.getPassword(), old, "uid", "userType", "homeAddress", "workAddress", "homeWorkPath");
		Optional<Smartphone> phone = getActivePhone(old);
		Smartphone active = null;
		//check #1 smartphone don't exists -> save a new association
		//check #2 smartphone exists but the imei is null and the new imei is not null -> end last association and create a new one
		//check #3 smartphone exists, the received imei is not null and is not equal to the old one -> end last association and create a new one
		//check #4 smartphone exists, appVersion is different(imei and old/new appVersion not null) -> end last association and create a new one
		if(/*#1*/(!phone.isPresent() && !nullOrEmptyOrBlank(update.getActivePhone())) ||
				/*#2*/(phone.isPresent() && nullOrEmptyOrBlank(phone.get().getImei()) && !nullOrEmptyOrBlank(update.getActivePhone())) ||
				/*#3*/(phone.isPresent() && !nullOrEmptyOrBlank(update.getActivePhone()) && !phone.get().getImei().equals(update.getActivePhone())) ||
				/*#4*/(phone.isPresent() && !nullOrEmptyOrBlank(update.getActivePhone()) && !nullOrEmptyOrBlank(phone.get().getAppVersion()) && !nullOrEmptyOrBlank(update.getActivePhoneVersion()) && !update.getActivePhoneVersion().equals(phone.get().getAppVersion()))) {
			if(phone.isPresent() && !nullOrEmptyOrBlank(update.getActivePhoneVersion())) {
				phone.get().setEndAssociation(LocalDateTime.now());
				smartphoneRepository.save(phone.get());
			}
			active = new Smartphone(update.getActivePhone(), old);
			smartphoneRepository.save(active);
		} else {
			if(phone.isPresent()) {
				active = phone.get();
			}
		}
		if(active != null) {
			//if the attribute is null/blank keep the old attribute into the db
			if(!nullOrEmptyOrBlank(update.getActivePhoneModel()) && !nullOrEmptyOrBlank(update.getActivePhone())) {
				active.setModel(update.getActivePhoneModel());
			}
			if(!nullOrEmptyOrBlank(update.getActivePhoneToken()) && !nullOrEmptyOrBlank(update.getActivePhone())) {
				active.setFcmToken(update.getActivePhoneToken());
			}
			if(!nullOrEmptyOrBlank(update.getActivePhoneVersion()) && !nullOrEmptyOrBlank(update.getActivePhone())) {
				active.setAppVersion(update.getActivePhoneVersion());
			}
			//save the updated smartphone
			smartphoneRepository.save(active);
		}

		HomeAddress updHomeAddress = prepareHomeAddress(update);
		if(updHomeAddress.getAddress() != null || updHomeAddress.coordinatesNotNull()) {
			HomeAddress newHomeAddress = new HomeAddress();
			Optional<HomeAddress> activeHomeAddress = addressService.getActiveHomeAddress(old);
			if(activeHomeAddress.isPresent()) {
				notNullBeanCopy(activeHomeAddress, newHomeAddress, "id", "startAssociation", "endAssociation", "latitude", "longitude");
			}
			notNullBeanCopy(updHomeAddress, newHomeAddress, "id");
			addressService.saveHomeAddress(updHomeAddress, old);
		}
		if(update.getWorkAddresses() != null) {
			addressService.saveWorkAddresses(update.getWorkAddresses(), old);
		}
		addressService.refreshHomeWorkPathsByUser(old);
		userRepository.save(old);
		return old;
	}

	public Optional<User> findByUid(String uid) {
		return userRepository.findById(uid);
	}

	public Set<User> findAllByType(UserType type) {
		return userRepository.findAllByUserType(type);
	}

	public List<User> findAll() {
		List<User> users = new LinkedList<>();
		userRepository.findAll().forEach(users::add);
		return users;
	}

	public Optional<Smartphone> getActivePhone(User u) {
		return smartphoneRepository.findByUser(u).stream().filter(s -> s.getEndAssociation() == null).findFirst();
	}

	public List<Smartphone> getSmartphones(User u) {
		return smartphoneRepository.findByUser(u);
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public Optional<User> findByCoinWallet(String address) {
		return userRepository.findByCoinWallet(address);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	private HomeAddress prepareHomeAddress(LismoverUserDto u) {
		HomeAddress address = new HomeAddress();
		if(u.getHomeAddress() != null) {
			address.setAddress(u.getHomeAddress());
			address.setNumber(u.getHomeNumber());
			if(u.getHomeCity() != null) {
				address.setCity(cityService.findById(u.getHomeCity()).orElse(null));
			}
		}
		if(u.getHomeLatitude() != null && u.getHomeLongitude() != null) {
			address.setLatitude(u.getHomeLatitude());
			address.setLongitude(u.getHomeLongitude());
		}
		return address;
	}

	public List<UserRankingPosition> findAllOrderByEarnedNationalPointsDesc() {
		return userRepository.findAllOrderByEarnedNationalPointsDesc();
	}

	public List<Enrollment> getActiveEnrollments(User u) {
		return enrollmentRepository.findAllEnabledByUser(u.getUid(), LocalDate.now()).orElse(new LinkedList<>());
	}

	public List<Enrollment> getActiveEnrollmentsAt(User u, LocalDate at) {
		return enrollmentRepository.findAllEnabledByUser(u.getUid(), at).orElse(new LinkedList<>());
	}

	//Incremento punti nazionali
	public void addNationalPoints(User user, BigDecimal points) {
		user.setPoints(Optional.ofNullable(user.getPoints()).orElse(BigDecimal.ZERO).add(points));
		user.setEarnedNationalPoints(Optional.ofNullable(user.getEarnedNationalPoints()).orElse(BigDecimal.ZERO).add(points));
		save(user);
	}

	public boolean isUserInFilter(User user, RankingFilter filter, String value, Organization organization) {
		if(filter == null) {
			return true;
		} else if(filter.equals(RankingFilter.GENDER)) {
			return user.getGender() != null && user.getGender().equalsIgnoreCase(value);
		} else if(filter.equals(RankingFilter.AGE)) {
			return user.getAge() != null && isInAgeInterval(user.getAge(), value);
		} else {
			return customFieldService.findByType(organization, filter, user).orElse(new CustomFieldValue(null, null)).getValue();
		}
	}

	private boolean isInAgeInterval(Integer age, String value) {
		Integer[] interval = getInterval(value);
		return age >= interval[0] && age <= interval[1];
	}

	public UserDashboard getUserDashboard(String uid) {
		UserDashboard dashboard = new UserDashboard();
		UserDashboardSession userSessions = userRepository.findCountSessionByUser(uid);
		dashboard.setSessionNumber(userSessions.getSessions());
		dashboard.setDistance(userSessions.getDistance());
		dashboard.setSessionDistanceAvg(userSessions.getAvgDistance());
		dashboard.setCo2(userSessions.getCo2());
		dashboard.setEuro(0.0);
		dashboard.setDailyDistance(userRepository.findDistanceByUserInMonths(uid));
		dashboard.setMessages(notificationMessageDeliveryRepository.countUnreadMessages(uid));
		return dashboard;
	}

	public void fixer() {
		List<User> users = userRepository.findAllByHomeAddressIsNull();
		logger.info("{} broken user(s) found", users.size());
		users.forEach(user -> {
			addressService.getActiveHomeAddress(user).ifPresent(a -> {
				user.setHomeAddress(a);
				userRepository.save(user);
				logger.info("User {} fixed", user.getUid());
			});
		});
	}

	public List<User> findByOrganization(Organization o) {
		return userRepository.findAllByOrganization(o);
	}

	public List<UserOverviewDto> getOverview(UserType userType, Organization o) {
		List<User> users;
		if(userType.equals(UserType.ROLE_ADMIN))
			users = this.findAll();
		else if(userType.equals(UserType.ROLE_MANAGER)) {
			users = findByOrganization(o);
		} else users = new LinkedList<>();
		return userMapper.userToOverviewDto(users);
	}
}
