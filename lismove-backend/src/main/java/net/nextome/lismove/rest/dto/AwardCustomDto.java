package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AwardCustomDto {
	private Long id;
	private String name;
	private String description;
	private BigDecimal value;
	private Integer type;
	private String imageUrl;
	private Integer winningsAllowed;
	private Integer issuer;
	private Long organization;
	private String uid;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Integer getWinningsAllowed() {
		return winningsAllowed;
	}

	public void setWinningsAllowed(Integer winningsAllowed) {
		this.winningsAllowed = winningsAllowed;
	}

	public Integer getIssuer() {
		return issuer;
	}

	public void setIssuer(Integer issuer) {
		this.issuer = issuer;
	}

	public Long getOrganization() {
		return organization;
	}

	public void setOrganization(Long organization) {
		this.organization = organization;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
