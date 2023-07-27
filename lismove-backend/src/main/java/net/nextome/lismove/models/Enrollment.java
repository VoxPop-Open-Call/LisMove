package net.nextome.lismove.models;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
public class Enrollment extends AuditableEntity {

	@Id
	@SequenceGenerator(name = "enrollseq", sequenceName = "enroll_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "enrollseq")
	private Long id;
	@Column(unique = true)
	private String code;
	private LocalDate startDate;
	private LocalDate endDate;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime activationDate;
	@Column(columnDefinition = "numeric(7,3)")
	private BigDecimal points;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal euro;
	private Boolean sessionForwarding;

	@ManyToOne
	private User user;
	@ManyToOne
	private Organization organization;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public LocalDateTime getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(LocalDateTime activationDate) {
		this.activationDate = activationDate;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Boolean getSessionForwarding() {
		return sessionForwarding;
	}

	public void setSessionForwarding(Boolean sessionForwarding) {
		this.sessionForwarding = sessionForwarding;
	}
}
