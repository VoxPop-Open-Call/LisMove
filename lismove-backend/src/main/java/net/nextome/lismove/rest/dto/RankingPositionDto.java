package net.nextome.lismove.rest.dto;

import net.nextome.lismove.models.User;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RankingPositionDto {
	private String username;
	private Integer points;
	private String avatarUrl;
	private Integer position;

	public RankingPositionDto() {
	}

	public RankingPositionDto(User user, Double points) {
		this.username = user.getUsername();
		this.avatarUrl = user.getAvatarUrl();
		this.points = BigDecimal.valueOf(points).setScale(0, RoundingMode.FLOOR).intValue();
	}

	public RankingPositionDto(User user, Long points) {
		this.username = user.getUsername();
		this.avatarUrl = user.getAvatarUrl();
		this.points = BigDecimal.valueOf(points).setScale(0, RoundingMode.FLOOR).intValue();
	}

	public RankingPositionDto(User user, BigDecimal points) {
		this.username = user.getUsername();
		this.avatarUrl = user.getAvatarUrl();
		this.points = points.setScale(0, RoundingMode.FLOOR).intValue();
	}

	public RankingPositionDto(User user, BigDecimal points, Integer position) {
		this.username = user.getUsername();
		this.avatarUrl = user.getAvatarUrl();
		this.points = points.setScale(0, RoundingMode.FLOOR).intValue();
		this.position = position;
	}

	public RankingPositionDto(String username, BigDecimal points, String avatarUrl, Integer position) {
		this.username = username;
		this.points = points.setScale(0, RoundingMode.FLOOR).intValue();
		this.avatarUrl = avatarUrl;
		this.position = position;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}
}
