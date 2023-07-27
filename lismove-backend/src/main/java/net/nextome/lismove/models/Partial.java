package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.PartialType;
import net.nextome.lismove.models.enums.SessionStatus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partials")
public class Partial {

	@Id
	@SequenceGenerator(name = "partialseq", sequenceName = "partial_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "partialseq")
	private Long id;
	private PartialType type;
	private SessionStatus status;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal sensorDistance;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal gpsDistance;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal gmapsDistance;
	private Double deltaRevs;
	private Boolean urban;
	private Boolean valid;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime timestamp;
	private Long rawData_wheel;
	private Long rawData_ts;
	@Column(columnDefinition = "varchar")
	private String extra;

	@ManyToOne
	private Session session;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PartialType getType() {
		return type;
	}

	public void setType(PartialType type) {
		this.type = type;
	}

	public SessionStatus getStatus() {
		return status;
	}

	public void setStatus(SessionStatus status) {
		this.status = status;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public BigDecimal getSensorDistance() {
		return sensorDistance;
	}

	public void setSensorDistance(BigDecimal sensorDistance) {
		this.sensorDistance = sensorDistance;
	}

	public BigDecimal getGpsDistance() {
		return gpsDistance;
	}

	public void setGpsDistance(BigDecimal gpsDistance) {
		this.gpsDistance = gpsDistance;
	}

	public BigDecimal getGmapsDistance() {
		return gmapsDistance;
	}

	public void setGmapsDistance(BigDecimal gmapsDistance) {
		this.gmapsDistance = gmapsDistance;
	}

	public Double getDeltaRevs() {
		return deltaRevs;
	}

	public void setDeltaRevs(Double deltaRevs) {
		this.deltaRevs = deltaRevs;
	}

	public Boolean getUrban() {
		return urban;
	}

	public void setUrban(Boolean urban) {
		this.urban = urban;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Long getRawData_wheel() {
		return rawData_wheel;
	}

	public void setRawData_wheel(Long rawData_wheel) {
		this.rawData_wheel = rawData_wheel;
	}

	public Long getRawData_ts() {
		return rawData_ts;
	}

	public void setRawData_ts(Long rawData_ts) {
		this.rawData_ts = rawData_ts;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
