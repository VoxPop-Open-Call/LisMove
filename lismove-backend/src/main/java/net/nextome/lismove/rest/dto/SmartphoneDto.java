package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SmartphoneDto {
	private Long id;
	private Long startAssociation;
	private Long endAssociation;
	private String imei;
	private String appVersion;
	private String platform;
	private String model;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getStartAssociation() {
		return startAssociation;
	}

	public void setStartAssociation(Long startAssociation) {
		this.startAssociation = startAssociation;
	}

	public Long getEndAssociation() {
		return endAssociation;
	}

	public void setEndAssociation(Long endAssociation) {
		this.endAssociation = endAssociation;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
}
