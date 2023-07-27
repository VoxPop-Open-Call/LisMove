package net.nextome.lismove.models;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seats")
public class Seat extends Address {

	@Id
	@SequenceGenerator(name = "seatseq", sequenceName = "seat_seq_id", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seatseq")
	private Long id;
	private String name;
	private Boolean validated = true;
	private BigDecimal destinationTolerance;    //km
	@Column(columnDefinition = "boolean default false", nullable = false)
	private Boolean deleted = false;

	@ManyToOne
	@JoinColumn(name = "organization_id")
	private Organization organization;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Boolean getValidated() {
		return validated;
	}

	public Boolean getValidatedAndNotNull() {
		return validated != null && getValidated();
	}

	public void setValidated(Boolean validated) {
		this.validated = validated;
	}

	public BigDecimal getDestinationTolerance() {
		return destinationTolerance;
	}

	public void setDestinationTolerance(BigDecimal destinationTolerance) {
		this.destinationTolerance = destinationTolerance;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
