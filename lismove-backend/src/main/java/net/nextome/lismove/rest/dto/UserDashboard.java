package net.nextome.lismove.rest.dto;

import net.nextome.lismove.models.query.UserDistanceStats;

import java.util.List;

public class UserDashboard {

	private Integer sessionNumber;
	private Double sessionDistanceAvg;
	private Double distance;
	private Double co2;
	private Double euro;
	private List<UserDistanceStats> dailyDistance;
	private Integer messages;

	public Integer getSessionNumber() {
		return sessionNumber;
	}

	public void setSessionNumber(Integer sessionNumber) {
		this.sessionNumber = sessionNumber;
	}

	public Double getSessionDistanceAvg() {
		return sessionDistanceAvg;
	}

	public void setSessionDistanceAvg(Double sessionDistanceAvg) {
		this.sessionDistanceAvg = sessionDistanceAvg;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getCo2() {
		return co2;
	}

	public void setCo2(Double co2) {
		this.co2 = co2;
	}

	public Double getEuro() {
		return euro;
	}

	public void setEuro(Double euro) {
		this.euro = euro;
	}

	public List<UserDistanceStats> getDailyDistance() {
		return dailyDistance;
	}

	public void setDailyDistance(List<UserDistanceStats> dailyDistance) {
		this.dailyDistance = dailyDistance;
	}

	public Integer getMessages() {
		return messages;
	}

	public void setMessages(Integer messages) {
		this.messages = messages;
	}
}
