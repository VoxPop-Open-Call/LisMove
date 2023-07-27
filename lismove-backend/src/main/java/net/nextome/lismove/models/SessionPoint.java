package net.nextome.lismove.models;


import net.nextome.lismove.models.enums.RefundStatus;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "session_points")
public class SessionPoint {

	@Id
	@SequenceGenerator(name = "sessionpointsseq", sequenceName = "session_points_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "sessionpointsseq")
	private Long id;
	@Column(columnDefinition = "numeric(6,2)")
	private BigDecimal points;
	@Column(columnDefinition = "numeric(6,2)")
	private BigDecimal euro;
	@Column(columnDefinition = "numeric(6,2)")
	private BigDecimal distance;
	@Column(columnDefinition = "numeric(6,2)")
	private BigDecimal refundDistance; // distanza considerata per i rimborsi urbani
	@Column(columnDefinition = "numeric(6,2)")
	private BigDecimal homeWorkDistance; // distanza considerata per i rimborsi casa-lavoro
	@Column(columnDefinition = "numeric(6,2)")
	private Double multiplier;
	private RefundStatus refundStatus;
	private Double multiplierDistance;
	private Double multiplierPoints;

	@ManyToOne
	private Session session;

	@ManyToOne
	private Organization organization;

	public SessionPoint() {
	}

	public SessionPoint(Session session, Organization organization, BigDecimal points, BigDecimal distance, Double multiplier) {
		this.session = session;
		this.organization = organization;
		this.points = points;
		this.distance = distance;
		this.multiplier = multiplier;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization enrollment) {
		this.organization = enrollment;
	}

	public BigDecimal getPoints() {
		return points;
	}

	public void setPoints(BigDecimal points) {
		this.points = points;
	}

	public BigDecimal getEuro() {
		return euro;
	}

	public void setEuro(BigDecimal euro) {
		this.euro = euro;
	}

	public BigDecimal getDistance() {
		return distance;
	}

	public void setDistance(BigDecimal distance) {
		this.distance = distance;
	}

	public BigDecimal getRefundDistance() {
		return refundDistance;
	}

	public void setRefundDistance(BigDecimal refundDistance) {
		this.refundDistance = refundDistance;
	}

	public BigDecimal getHomeWorkDistance() {
		return homeWorkDistance;
	}

	public void setHomeWorkDistance(BigDecimal homeWorkDistance) {
		this.homeWorkDistance = homeWorkDistance;
	}

	public Double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(Double multiplier) {
		this.multiplier = multiplier;
	}

	public RefundStatus getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(RefundStatus refundStatus) {
		this.refundStatus = refundStatus;
	}

	public Double getMultiplierDistance() {
		return multiplierDistance;
	}

	public void setMultiplierDistance(Double multiplierDistance) {
		this.multiplierDistance = multiplierDistance;
	}

	public Double getMultiplierPoints() {
		return multiplierPoints;
	}

	public void setMultiplierPoints(Double multiplierPoints) {
		this.multiplierPoints = multiplierPoints;
	}
}
