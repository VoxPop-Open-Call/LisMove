package net.nextome.lismove.rest.dto;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SessionPointDto {
	private Long id;
	@ApiModelProperty("Server generated")
	private String sessionId;
	@ApiModelProperty("Server generated")
	private Integer type;
	private Long organizationId;
	private String organizationTitle;
	private Integer points;
	@ApiModelProperty("Server generated")
	private Double euro;
	private Double distance;
	@ApiModelProperty("Server generated")
	private Double refundDistance;
	private Double multiplier;
	@ApiModelProperty("Server generated")
	private Integer refundStatus;
	private Double multiplierDistance;
	private Double multiplierPoints;

	public SessionPointDto() {
	}

	public SessionPointDto(Long organizationId, Double points, Double distance, Double multiplier) {
		this.organizationId = organizationId;
		this.points = BigDecimal.valueOf(points).setScale(0, RoundingMode.FLOOR).intValue();
		this.distance = distance;
		this.multiplier = multiplier;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public String getOrganizationTitle() {
		return organizationTitle;
	}

	public void setOrganizationTitle(String organizationTitle) {
		this.organizationTitle = organizationTitle;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public Double getEuro() {
		return euro;
	}

	public void setEuro(Double euro) {
		this.euro = euro;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getRefundDistance() {
		return refundDistance;
	}

	public void setRefundDistance(Double refundDistance) {
		this.refundDistance = refundDistance;
	}

	public Double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(Double multiplier) {
		this.multiplier = multiplier;
	}

	public Integer getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(Integer refundStatus) {
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
