package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.RankingFilter;
import net.nextome.lismove.models.enums.RankingValue;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "achievements")
public class Achievement extends AuditableEntity {

	@Id
	@SequenceGenerator(name = "achievementsseq", sequenceName = "achievements_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "achievementsseq")
	private Long id;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private Integer duration;
	private RankingValue value;
	private RankingFilter filter;
	private String filterValue;
	private String logo;
	@Column(columnDefinition = "numeric(12,2)")
	private BigDecimal target;
	@ManyToOne
	@JoinColumn(name = "organization")
	private Organization organization;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public RankingValue getValue() {
		return value;
	}

	public void setValue(RankingValue value) {
		this.value = value;
	}

	public RankingFilter getFilter() {
		return filter;
	}

	public void setFilter(RankingFilter filter) {
		this.filter = filter;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	public BigDecimal getTarget() {
		return target;
	}

	public void setTarget(BigDecimal target) {
		this.target = target;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
