package net.nextome.lismove.models;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offline_sessions")
public class OfflineSession extends AuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime startTime;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime endTime;
	private Integer startRevs;
	private Integer endRevs;
	@Column(columnDefinition = "numeric(10,5)")
	private Double distance;
	private String sensor;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime start) {
		this.startTime = start;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime end) {
		this.endTime = end;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
