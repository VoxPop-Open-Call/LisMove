package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.nextome.lismove.rest.mappers.UtilMapper;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrganizationDto extends UtilMapper {
	private Long id;
	private Integer type;
	private String title;
	private String logo;
	private String initiativeLogo;
	private String notificationLogo;
	private String termsConditions;
	private String pageDescription;
	private String geojson;
	private Boolean validation;
	private String validatorEmail;
	private String regulation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getInitiativeLogo() {
		return initiativeLogo;
	}

	public void setInitiativeLogo(String initiativeLogo) {
		this.initiativeLogo = initiativeLogo;
	}

	public String getNotificationLogo() {
		return notificationLogo;
	}

	public void setNotificationLogo(String notificationLogo) {
		this.notificationLogo = notificationLogo;
	}

	public String getTermsConditions() {
		return termsConditions;
	}

	public void setTermsConditions(String termsConditions) {
		this.termsConditions = termsConditions;
	}

	public String getPageDescription() {
		return pageDescription;
	}

	public void setPageDescription(String pageDescription) {
		this.pageDescription = pageDescription;
	}

	public String getGeojson() {
		return geojson;
	}

	public void setGeojson(String geojson) {
		this.geojson = geojson;
	}

	public Boolean getValidation() {
		return validation;
	}

	public void setValidation(Boolean validation) {
		this.validation = validation;
	}

	public String getValidatorEmail() {
		return validatorEmail;
	}

	public void setValidatorEmail(String validatorEmail) {
		this.validatorEmail = validatorEmail;
	}

	public String getRegulation() {
		return regulation;
	}

	public void setRegulation(String regulation) {
		this.regulation = regulation;
	}
}
