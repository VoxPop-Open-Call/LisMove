package net.nextome.lismove.services;

import com.google.firebase.auth.UserRecord;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.OrganizationType;
import net.nextome.lismove.models.enums.UserType;
import net.nextome.lismove.repositories.*;
import net.nextome.lismove.rest.dto.*;
import net.nextome.lismove.rest.mappers.OrganizationMapper;
import net.nextome.lismove.rest.mappers.UserMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationService extends UtilitiesService {

	@Autowired
	private FirebaseAuthService firebaseAuthService;
	@Autowired
	private UserService userService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private EnrollmentRepository enrollmentRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private OrganizationSettingRepository organizationSettingRepository;
	@Autowired
	private OrganizationSettingValueRepository organizationSettingValueRepository;
	@Autowired
	private OrganizationMapper organizationMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private PartialRepository partialRepository;

	public Organization create(OrganizationDto dto) {
		Organization organization = organizationMapper.dtoToOrganization(dto);
		if(organization.getType().equals(OrganizationType.COMPANY)) {
			organization.setValidation(false);
		}
		if(organization.getValidation() == null) {
			organization.setValidation(false);
		}
		if(organization.getType().equals(OrganizationType.PA) && organization.getValidation() && organization.getValidatorEmail() == null) {
			throw new LismoveException("Missing validator email", HttpStatus.BAD_REQUEST);
		}
		return organizationRepository.save(organization);
	}

	public Organization update(Organization old, OrganizationDto dto) {
		Organization upd = organizationMapper.dtoToOrganization(dto);
		this.notNullBeanCopy(upd, old, "id");
		return organizationRepository.save(old);
	}

	public void delete(Long oid) {
		organizationRepository.deleteById(oid);
	}

	public User createManager(ManagerDto dto, Organization org) {
		User user = userMapper.managerDtoToUser(dto);
		UserRecord authUser = firebaseAuthService.createUser(user, dto.getPassword());
		user.setUid(authUser.getUid());
		user.setOrganization(org);
		user.setUserType(UserType.ROLE_MANAGER);
		user.setEnabled(true);
		return userService.create(user);
	}

	public User updateManager(User old, ManagerDto update) {
		User upd = userMapper.managerDtoToUser(update);
		return userService.update(upd, update.getPassword(), old, "uid", "userType");
	}

	public Optional<Organization> findById(Long oid) {
		return organizationRepository.findById(oid);
	}

	public Set<User> findAllManagers(Organization org) {
		return userService.findAllByType(UserType.ROLE_MANAGER).stream().filter(user ->
				user.getOrganization().getId().equals(org.getId())).collect(Collectors.toSet());
	}

	public List<Organization> getAll() {
		return (List<Organization>) this.organizationRepository.findAll();
	}

	public List<Enrollment> generateCodes(EnrollmentCodeGenerator codeGenerator, Organization o) {
		List<Enrollment> enrollments = new LinkedList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate start = LocalDate.parse(codeGenerator.getStart(), formatter);
		LocalDate end = LocalDate.parse(codeGenerator.getEnd(), formatter);
		for(int i = 0; i < codeGenerator.getN(); i++) {
			Enrollment e = new Enrollment();
			e.setOrganization(o);
			e.setStartDate(start);
			e.setEndDate(end);
			String ts = UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
			e.setCode(o.getCode() + ts.substring(ts.length() - 6));
			enrollments.add(e);
		}
		enrollmentRepository.saveAll(enrollments);
		return enrollments;
	}

	public List<Enrollment> editCodes(EnrollmentCodeEditor codeEditor, Organization o) {
		List<Enrollment> enrollments = new LinkedList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate start = LocalDate.parse(codeEditor.getStart(), formatter);
		LocalDate end = LocalDate.parse(codeEditor.getEnd(), formatter);
		List<Long> enrollmentsId = codeEditor.getSelectedEnrollments();
		for(Long eid : enrollmentsId) {
			Enrollment e = enrollmentRepository.findById(eid).orElse(null);
			if(e != null) {
				e.setStartDate(start);
				e.setEndDate(end);
				enrollments.add(e);
			}
		}
		enrollmentRepository.saveAll(enrollments);
		return enrollments;
	}

	public Enrollment verifyCode(String code, User u) {
		Enrollment e = enrollmentRepository.findByCode(code).orElseThrow(() -> new LismoveException("Codice non trovato", HttpStatus.NOT_FOUND));
		if(e.getUser() != null) {
			throw new LismoveException("Codice già riscattato", HttpStatus.BAD_REQUEST);
		}
		if(e.getStartDate().isAfter(LocalDate.now())) {
			throw new LismoveException("Codice non ancora disponibile", HttpStatus.BAD_REQUEST);
		}
		if(e.getEndDate().isBefore(LocalDate.now())) {
			throw new LismoveException("Codice scaduto", HttpStatus.BAD_REQUEST);
		}
		findActiveByUserAndOrganization(u, e.getOrganization()).ifPresent(enrollment -> {
			throw new LismoveException("Partecipi già a questa iniziativa", HttpStatus.BAD_REQUEST);
		});
		return e;
	}

	public Enrollment consumeCode(String code, User u) {
		Enrollment e = verifyCode(code, u);
		e.setUser(u);
		e.setActivationDate(LocalDateTime.now());
		e.setPoints(BigDecimal.ZERO);
		return enrollmentRepository.save(e);
	}

	public List<Enrollment> getUserEnrollments(String uid) {
		return enrollmentRepository.findByUserUid(uid);
	}

	public Optional<Enrollment> findEnrollmentById(Long id) {
		return enrollmentRepository.findById(id);
	}

	public Optional<Enrollment> findActiveByUserAndOrganization(User user, Organization organization) {
		return enrollmentRepository.findActiveByUserAndOrganization(user.getUid(), organization.getId());
	}

	public Optional<Enrollment> findActiveByUserAndOrganization(User user, Organization organization, LocalDate date) {
		return enrollmentRepository.findActiveByUserAndOrganization(user.getUid(), organization.getId(), date);
	}

	public List<Enrollment> findActivesByUserAt(User u, LocalDateTime dateTime) {
		return enrollmentRepository.findAllEnabledByUser(u.getUid(), dateTime.toLocalDate()).orElse(new LinkedList<>());
	}

	public Enrollment save(Enrollment enrollment) {
		return enrollmentRepository.save(enrollment);
	}

	public List<OrganizationSettingValue> save(List<OrganizationSettingValueDto> valueDtos) {
		valueDtos.forEach(dto -> {
			if(!organizationSettingRepository.existsById(dto.getOrganizationSetting())) {
				organizationSettingRepository.save(new OrganizationSetting(dto.getOrganizationSetting(), dto.getValue() == null ? "" : dto.getValue()));
			}
			organizationSettingsService.findByOrganizationAndOrganizationSetting(dto.getOrganization(), dto.getOrganizationSetting()).ifPresent(value -> {
//				notNullBeanCopy(organizationMapper.dtoToOrganizationSettingValue(dto), value);
				dto.setId(value.getId());
			});
		});
		List<OrganizationSettingValue> values = organizationMapper.dtoToOrganizationSettingValue(valueDtos);
		organizationSettingValueRepository.saveAll(values);
		return values;
	}

	public List<PartialsOverviewDto> findPartialsByOrganization(Long oid, Boolean isHomeWorkPath, Integer minAge, Integer maxAge, LocalDateTime minDate, LocalDateTime maxDate, LocalTime minTime, LocalTime maxTime){
		if(isHomeWorkPath) return partialRepository.findByOrganizationWhereIsHomeWorkPath(oid, minAge, maxAge, minDate, maxDate, minTime, maxTime);
		else return partialRepository.findByOrganization(oid, minAge, maxAge, minDate, maxDate, minTime, maxTime);
	}
}
