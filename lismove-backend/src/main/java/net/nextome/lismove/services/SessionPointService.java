package net.nextome.lismove.services;

import com.bugsnag.Bugsnag;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.*;
import net.nextome.lismove.repositories.SessionPointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SessionPointService {

	@Autowired
	private SessionPointRepository sessionPointRepository;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private RefundService refundService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private AddressService addressService;
	@Autowired
	private AwardService awardService;
	@Autowired
	private Bugsnag bugsnag;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SessionPoint save(SessionPoint sessionPoint) {
		return sessionPointRepository.save(sessionPoint);
	}

	public List<SessionPoint> findBySession(Session session) {
		return sessionPointRepository.findBySession(session);
	}

	public void delete(SessionPoint sessionPoint) {
		sessionPointRepository.delete(sessionPoint);
	}

	public void deleteAllBySession(Session session) {
		sessionPointRepository.deleteAllBySession(session);
	}

	public void assignSessionPoints(List<SessionPoint> sessionPoints) {
		sessionPoints.forEach(sp -> {
			try {
				organizationService.findActiveByUserAndOrganization(sp.getSession().getUser(), sp.getOrganization()).ifPresent(enrollment -> {
					Session session = sp.getSession();
                    User user = sp.getSession().getUser();
                    Organization org = sp.getOrganization();
                    LocalDateTime sessionStartTime = session.getStartTime();
                    sp.setDistance(sp.getDistance().setScale(5, RoundingMode.HALF_UP));
                    // Assegnazione punti iniziativa per tutti i SessionPoints
                    refundService.addInitiativePoints(sp);


					if (!isHomeWork(sp)) {
						// Rimborso percorso urbano

						sp.setRefundDistance(sp.getDistance().setScale(5, RoundingMode.HALF_UP));
						// Calcolo e assegnazione rimborsi
						if (organizationSettingsService.get(org, "isActiveUrbanPathRefunds", Boolean.class)) {
							BigDecimal euro = sp.getRefundDistance().multiply(BigDecimal.valueOf(getConstant(sp))).setScale(2, RoundingMode.FLOOR);
							RefundService.EuroRefundLimit limits = refundService.checkRefundLimits(euro, enrollment, sessionStartTime);
							sp.setEuro(limits.getRefundEuro());
							sp.setRefundStatus(limits.getRefundStatus());
							refundService.addMoney(sp);
						}
					} else {
						// Rimborso percorso casa-lavoro

						// Se il sessionPoint è di tipo HOME_WORK_PATH_POINTS (quindi generato da server),
						// acquisisco il moltiplicatore per il calcolo dei punti con bonus e valorizzo i campi del sessionPoint.
						sp.setMultiplier(calculateMultiplier(sp));
						sp.setRefundDistance(calculateRefundDistance(sp).setScale(5, RoundingMode.HALF_UP));
						// Calcolo e assegnazione rimborsi
						if (organizationSettingsService.get(org, "isActiveHomeWorkRefunds", Boolean.class)) {
							BigDecimal amount = sp.getRefundDistance().multiply(BigDecimal.valueOf(getConstant(sp)));
							if (organizationSettingsService.get(org, "homeWorkRefundType", String.class).equals("urbanPoints")) {
								AwardCustomUser awardCustomUser = awardService.generateAndAssign(
										AwardType.POINTS,
										amount.setScale(0, RoundingMode.FLOOR),
										AwardCustomIssuer.CREATED_BY_REFUNDS,
										org,
										1,
										"Rimborso punti",
										"Rimborso punti calcolati da sessione casa-lavoro",
										null,
										user);
								refundService.addInitiativePoints(awardCustomUser);
							} else if (organizationSettingsService.get(org, "homeWorkRefundType", String.class).equals("euro")) {
								RefundService.EuroRefundLimit limits = refundService.checkRefundLimits(amount.setScale(2, RoundingMode.FLOOR), enrollment, sessionStartTime);
								sp.setEuro(limits.getRefundEuro());
								sp.setRefundStatus(limits.getRefundStatus());
								refundService.addMoney(sp);
							}
						}
					}
					save(sp);
				});
			} catch (Exception e) {
				logger.error(e.getMessage());
				bugsnag.notify(e, report -> {
					User user = sp.getSession().getUser();
					report.setUser(user.getUid(), user.getEmail(), user.getUsername());
					report.addToTab("Session info", "uuid", sp.getSession().getId());
				});
			}
		});
	}

	public Double calculateMultiplier(SessionPoint sessionPoint) {
		Double multiplier = 1D;
		Organization org = sessionPoint.getOrganization();
		LocalDateTime startTime = sessionPoint.getSession().getStartTime();
		if(startTime.toLocalDate().isAfter(organizationSettingsService.get(org, "startDateBonus", LocalDate.class))
				&& startTime.toLocalDate().isBefore(organizationSettingsService.get(org, "endDateBonus", LocalDate.class))) {
			// Bonus su fascia oraria
			if(!organizationSettingsService.get(org, "isActiveTimeSlotBonus", Boolean.class)
					|| organizationSettingsService.get(org, "isActiveTimeSlotBonus", Boolean.class)
					&& startTime.toLocalTime().isAfter(organizationSettingsService.get(org, "startTimeBonus", LocalTime.class))
					&& startTime.toLocalTime().isBefore(organizationSettingsService.get(org, "endTimeBonus", LocalTime.class))) {
				multiplier = Optional.ofNullable(organizationSettingsService.get(org, "multiplier", Double.class)).orElse(1D);
			}
		}
		return multiplier;
	}

	public BigDecimal calculateRefundDistance(SessionPoint sessionPoint) {
		BigDecimal maxDistance = addressService.calculatePolylineAndDistance(sessionPoint.getSession().getHomeAddress(), sessionPoint.getSession().getWorkAddress().getSeat()).getDistance()
				.multiply(BigDecimal.valueOf(organizationSettingsService.get(sessionPoint.getOrganization(), "homeWorkPathTolerancePerc", Double.class) + 1));
		if (sessionPoint.getDistance().compareTo(maxDistance) <= 0) {
			return sessionPoint.getDistance();
		} else {
			return maxDistance;
		}
	}

	private Double getConstant(SessionPoint sp) {
		String setting;
		Double settingValue = null;
		if (isHomeWork(sp)) {
			setting = "valueKmHomeWork";
		} else {
			setting = "euroValueKmUrbanPath";
		}
		switch(sp.getSession().getType()) {
			case ELECTRIC_BIKE:
				settingValue = organizationSettingsService.get(sp.getOrganization(), setting + "ElectricBike", Double.class);
			case BIKE:
				if(settingValue == null) {
					settingValue = organizationSettingsService.get(sp.getOrganization(), setting + "Bike", Double.class);
				}
				break;
			default:
				settingValue = (double) 0;
				break;
		}
		return settingValue;
	}

	public SessionPoint createHomeWorkSessionPoints(Session session) {
		return new SessionPoint(session, session.getWorkAddress().getSeat().getOrganization(), session.getNationalPoints(), session.getNationalKm(), 1.0);
	}

	public void addInitiativePoints(SessionPoint sp, BigDecimal points) {
		Enrollment e = organizationService.findActiveByUserAndOrganization(sp.getSession().getUser(), sp.getOrganization(), sp.getSession().getStartTime().toLocalDate()).orElse(null);
		if(e != null) {
			sp.setPoints(Optional.ofNullable(sp.getPoints()).orElse(BigDecimal.ZERO).add(points));
			e.setPoints(Optional.ofNullable(e.getPoints()).orElse(BigDecimal.ZERO).add(points));
			save(sp);
			organizationService.save(e);
		}
	}

	public void resetSessionPoints(Session session) {
		sessionPointRepository.deleteAllBySession(session);
		entityManager.flush();
		if(session.getSessionPoints() != null) {
			sessionPointRepository.saveAll(session.getSessionPoints());
		}
	}

	public boolean isHomeWork(SessionPoint sessionPoint) {
		return sessionPoint.getHomeWorkDistance() != null && sessionPoint.getHomeWorkDistance().compareTo(BigDecimal.ZERO) > 0;
	}

	public BigDecimal getTotInitiativeEuros(Enrollment enrollment) {
		return sessionPointRepository.getTotInitiativeEuros(enrollment.getUser().getUid(), enrollment.getOrganization().getId()).orElse(BigDecimal.ZERO);
	}

	public BigDecimal getTotMonthlyEuros(Enrollment enrollment, LocalDateTime date) {
		return sessionPointRepository.getTotMonthlyEuros(enrollment.getUser().getUid(), enrollment.getOrganization().getId(), date.getYear(), date.getMonth().getValue()).orElse(BigDecimal.ZERO);
	}

	public BigDecimal getTotDailyEuros(Enrollment enrollment, LocalDateTime date) {
		return sessionPointRepository.getTotDailyEuros(enrollment.getUser().getUid(), enrollment.getOrganization().getId(), date.getYear(), date.getMonth().getValue(), date.getDayOfMonth()).orElse(BigDecimal.ZERO);
	}
}