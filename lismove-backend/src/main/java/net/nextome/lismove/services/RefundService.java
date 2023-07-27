package net.nextome.lismove.services;

import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.RefundStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefundService {
	@Autowired
	private OrganizationSettingsService organizationSettingsService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private SessionPointService sessionPointService;
	@Autowired
	private UserService userService;

	//Incremento punti iniziativa o urbani

	public void addInitiativePoints(SessionPoint sp) {
		addInitiativePoints(sp.getSession().getUser(), sp.getOrganization(), sp.getPoints(), sp.getSession().getStartTime());
	}

	public void addInitiativePoints(AwardCustomUser award) {
		addInitiativePoints(award.getUser(), award.getAwardCustom().getOrganization(), award.getAwardCustom().getValue());
	}

	private void addInitiativePoints(User user, Organization org, BigDecimal points) {
		addInitiativePoints(user, org, points, null);
	}

	private void addInitiativePoints(User user, Organization org, BigDecimal points, LocalDateTime timestamp) {
		//Controllo se i punti urbani sono attivi e se il periodo è valido
		points = Optional.ofNullable(points).orElse(BigDecimal.ZERO);
		if(organizationSettingsService.get(org, "isActiveUrbanPoints", Boolean.class)) {
			if(timestamp == null
					|| timestamp.toLocalDate().isAfter(organizationSettingsService.get(org, "startDateUrbanPoints", LocalDate.class))
					&& timestamp.toLocalDate().isBefore(organizationSettingsService.get(org, "endDateUrbanPoints", LocalDate.class))) {
				Optional<Enrollment> enrollment = organizationService.findActiveByUserAndOrganization(user, org);
				if(enrollment.isPresent()) {
					enrollment.get().setPoints(Optional.ofNullable(enrollment.get().getPoints()).orElse(BigDecimal.ZERO).add(points));
					organizationService.save(enrollment.get());
				}
			}
		}
	}

	//Incremento denaro

	public void addMoney(SessionPoint sp) {
		addMoney(sp.getSession().getUser(), sp.getOrganization(), sp.getEuro(), sessionPointService.isHomeWork(sp));
	}

	public void addMoney(Coupon coupon) {
		addMoney(coupon.getUser(), coupon.getOrganization(), coupon.getValue(), false);
	}

	private void addMoney(User user, Organization org, BigDecimal euro, boolean isHomeWork) {
		if(euro.compareTo(BigDecimal.ZERO) > 0) {
			user.setEuro(Optional.ofNullable(user.getEuro()).orElse(BigDecimal.ZERO).add(euro));
			user.setTotalMoneyEarned(Optional.ofNullable(user.getTotalMoneyEarned()).orElse(BigDecimal.ZERO).add(euro));
			if (isHomeWork) {
				user.setTotalMoneyRefundHomeWork(Optional.ofNullable(user.getTotalMoneyRefundHomeWork()).orElse(BigDecimal.ZERO).add(euro));
			} else {
				user.setTotalMoneyRefundNotHomeWork(Optional.ofNullable(user.getTotalMoneyRefundNotHomeWork()).orElse(BigDecimal.ZERO).add(euro));
			}
			Optional<Enrollment> enrollment = organizationService.findActiveByUserAndOrganization(user, org);
			if (enrollment.isPresent()) {
				enrollment.get().setEuro(Optional.ofNullable(enrollment.get().getEuro()).orElse(BigDecimal.ZERO).add(euro));
				organizationService.save(enrollment.get());
			}
			userService.save(user);
		}
	}
	public EuroRefundLimit checkRefundLimits(BigDecimal euro, Enrollment enrollment, LocalDateTime timestamp) {
		Organization org = enrollment.getOrganization();
		BigDecimal currentInitiative = sessionPointService.getTotInitiativeEuros(enrollment);
		BigDecimal currentMonthly = sessionPointService.getTotMonthlyEuros(enrollment, timestamp);
		BigDecimal currentDaily = sessionPointService.getTotDailyEuros(enrollment, timestamp);
		Optional<Double> initiativeLimit = Optional.ofNullable(organizationSettingsService.get(org, "euroMaxRefundInATime", Double.class));
		Optional<Double> monthLimit = Optional.ofNullable(organizationSettingsService.get(org, "euroMaxRefundInAMonth", Double.class));
		Optional<Double> dayLimit = Optional.ofNullable(organizationSettingsService.get(org, "euroMaxRefundInADay", Double.class));

		if(initiativeLimit.isPresent() && currentInitiative.compareTo(BigDecimal.valueOf(initiativeLimit.get())) == 0) {
			return new EuroRefundLimit(BigDecimal.ZERO, RefundStatus.LIMIT_INITIATIVE);
		} else if(monthLimit.isPresent() && currentMonthly.compareTo(BigDecimal.valueOf(monthLimit.get())) == 0) {
			return new EuroRefundLimit(BigDecimal.ZERO, RefundStatus.LIMIT_MONTHLY);
		} else if(dayLimit.isPresent() && currentDaily.compareTo(BigDecimal.valueOf(dayLimit.get())) == 0) {
			return new EuroRefundLimit(BigDecimal.ZERO, RefundStatus.LIMIT_DAILY);
		} else if(initiativeLimit.isPresent() && currentInitiative.add(euro).compareTo(BigDecimal.valueOf(initiativeLimit.get())) > 0) {
			return new EuroRefundLimit(BigDecimal.valueOf(initiativeLimit.get()).subtract(currentInitiative), RefundStatus.PARTIAL_INITIATIVE);
		} else if(monthLimit.isPresent() && currentMonthly.add(euro).compareTo(BigDecimal.valueOf(monthLimit.get())) > 0) {
			return new EuroRefundLimit(BigDecimal.valueOf(monthLimit.get()).subtract(currentMonthly), RefundStatus.PARTIAL_MONTHLY);
		} else if(dayLimit.isPresent() && currentDaily.add(euro).compareTo(BigDecimal.valueOf(dayLimit.get())) > 0) {
			return new EuroRefundLimit(BigDecimal.valueOf(dayLimit.get()).subtract(currentDaily), RefundStatus.PARTIAL_DAILY);
		} else {
			return new EuroRefundLimit(euro, RefundStatus.REFUND_DONE);
		}
	}

	public static class EuroRefundLimit {
		private BigDecimal refundEuro;
		private RefundStatus refundStatus;

		public EuroRefundLimit(BigDecimal refundDistance, RefundStatus refundStatus) {
			this.refundEuro = refundDistance;
			this.refundStatus = refundStatus;
		}

		public BigDecimal getRefundEuro() {
			return refundEuro;
		}

		public void setRefundEuro(BigDecimal refundEuro) {
			this.refundEuro = refundEuro;
		}

		public RefundStatus getRefundStatus() {
			return refundStatus;
		}

		public void setRefundStatus(RefundStatus refundStatus) {
			this.refundStatus = refundStatus;
		}
	}
}
