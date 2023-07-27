package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SensorDto {

	private String uuid;
	private String firmware;
	private Long startAssociation;
	private Long endAssociation;
	private String bikeType;
	private BigDecimal wheelDiameter;
	private BigDecimal hubCoefficient;
	private Boolean stolen;
	private String name;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFirmware() {
		return firmware;
	}

	public void setFirmware(String firmware) {
		this.firmware = firmware;
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

	public String getBikeType() {
		return bikeType;
	}

	public void setBikeType(String bikeType) {
		this.bikeType = bikeType;
	}

	public BigDecimal getWheelDiameter() {
		return wheelDiameter;
	}

	public void setWheelDiameter(BigDecimal wheelDiameter) {
		this.wheelDiameter = wheelDiameter;
	}

	public BigDecimal getHubCoefficient() {
		return hubCoefficient;
	}

	public void setHubCoefficient(BigDecimal hubCoefficient) {
		this.hubCoefficient = hubCoefficient;
	}

	public Boolean getStolen() {
		return stolen;
	}

	public void setStolen(Boolean stolen) {
		this.stolen = stolen;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
