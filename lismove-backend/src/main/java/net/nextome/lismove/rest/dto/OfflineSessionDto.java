package net.nextome.lismove.rest.dto;

import java.util.UUID;

public class OfflineSessionDto {

	private UUID id;
	private String username;
	private String email;
	private Long startTime;
	private Long endTime;
	private Integer startRevs;
	private Integer endRevs;
	private Double distance;
	private String sensor;
	private String user;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public Integer getStartRevs() {
		return startRevs;
	}

	public void setStartRevs(Integer startRevs) {
		this.startRevs = startRevs;
	}

	public Integer getEndRevs() {
		return endRevs;
	}

	public void setEndRevs(Integer endRevs) {
		this.endRevs = endRevs;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
