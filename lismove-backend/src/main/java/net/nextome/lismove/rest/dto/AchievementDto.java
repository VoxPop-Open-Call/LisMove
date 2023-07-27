package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AchievementDto {
	private Long id;
	private String name;
	private Integer duration;
	private Long startDate;
	private Long endDate;
	private Integer value;
	private Integer filter;
	private String filterValue;
	private String logo;
	private BigDecimal target;
	private Long organization;
	private String organizationTitle;
	private String user;
	private String username;
	private BigDecimal score;
	private Boolean fullfilled;

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

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getFilter() {
		return filter;
	}

	public void setFilter(Integer filter) {
		this.filter = filter;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public BigDecimal getTarget() {
		return target;
	}

	public void setTarget(BigDecimal target) {
		this.target = target;
	}

	public Long getOrganization() {
		return organization;
	}

	public void setOrganization(Long organization) {
		this.organization = organization;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public BigDecimal getScore() {
		return score;
	}

	public void setScore(BigDecimal score) {
		this.score = score;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public void setOrganizationTitle(String organizationTitle) {
		this.organizationTitle = organizationTitle;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Boolean getFullfilled() {
		return fullfilled;
	}

	public void setFullfilled(Boolean fullfilled) {
		this.fullfilled = fullfilled;
	}
}
