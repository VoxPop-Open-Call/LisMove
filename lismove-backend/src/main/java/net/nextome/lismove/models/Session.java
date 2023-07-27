package net.nextome.lismove.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.nextome.lismove.models.enums.SessionStatus;
import net.nextome.lismove.models.enums.SessionType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	//a piedi, bici elettrica, bici muscolare, carpooling, monopattino
	private SessionType type;
	private Boolean valid;
	private SessionStatus status;
	private Boolean certificated;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime startTime;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime endTime;
	private Integer duration;
	@Column(columnDefinition = "varchar")
	private String polyline;
	@Column(columnDefinition = "varchar")
	private String rawPolyline;
	@Column(columnDefinition = "varchar")
	private String gmapsPolyline;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal gyroDistance;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal gpsDistance;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal gpsOnlyDistance;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal gmapsDistance;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal urbanKm;
	private BigDecimal urbanPoints;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal nationalKm;
	private BigDecimal nationalPoints;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal totalKm;
	private Boolean isHomeWorkPath;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal euro;
	private Double multiplier;
	private String description;
	private Double startBattery;
	private Double endBattery;
	private String bikeType;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal wheelDiameter;
	private Integer phoneStartBattery;
	private Integer phoneEndBattery;
	private Double co2;
	private String sensor;
	private String sensorName;
	private String firmware;
	private String appVersion;
	private String platform;
	private String phoneModel;
	private String oldSessionId;
	private LocalDateTime validatedDate;
	//Verifica
	private Boolean verificationRequired;
	private String verificationRequiredNote;
	private LocalDateTime forwardedAt;

	@ManyToOne
	private User user;

	@ManyToOne
	@JoinColumn(name = "home_address_id")
	private HomeAddress homeAddress;
	@ManyToOne
	@JoinColumn(name = "work_address_id")
	private WorkAddress workAddress;

	@JsonIgnore
	@OrderBy("id ASC")
	@OneToMany(mappedBy = "session")
	private List<Partial> partials;

	@JsonIgnore
	@OrderBy("id ASC")
	@OneToMany(mappedBy = "session")
	private List<SessionPoint> sessionPoints;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public SessionType getType() {
		return type;
	}

	public void setType(SessionType type) {
		this.type = type;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public SessionStatus getStatus() {
		return status;
	}

	public void setStatus(SessionStatus status) {
		this.status = status;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getPolyline() {
		return polyline;
	}

	public void setPolyline(String polyline) {
		this.polyline = polyline;
	}

	public String getRawPolyline() {
		return rawPolyline;
	}

	public void setRawPolyline(String rawPolyine) {
		this.rawPolyline = rawPolyine;
	}

	public String getGmapsPolyline() {
		return gmapsPolyline;
	}

	public void setGmapsPolyline(String gmapsPolyline) {
		this.gmapsPolyline = gmapsPolyline;
	}

	public BigDecimal getGyroDistance() {
		return gyroDistance;
	}

	public void setGyroDistance(BigDecimal gyroDistance) {
		this.gyroDistance = gyroDistance;
	}

	public BigDecimal getGpsDistance() {
		return gpsDistance;
	}

	public void setGpsDistance(BigDecimal gpsDistance) {
		this.gpsDistance = gpsDistance;
	}

	public BigDecimal getGpsOnlyDistance() {
		return gpsOnlyDistance;
	}

	public void setGpsOnlyDistance(BigDecimal gpsOnlyDistance) {
		this.gpsOnlyDistance = gpsOnlyDistance;
	}

	public BigDecimal getGmapsDistance() {
		return gmapsDistance;
	}

	public void setGmapsDistance(BigDecimal gmapsDistance) {
		this.gmapsDistance = gmapsDistance;
	}

	public BigDecimal getUrbanKm() {
		return urbanKm;
	}

	public void setUrbanKm(BigDecimal urbanKm) {
		this.urbanKm = urbanKm;
	}

	public BigDecimal getUrbanPoints() {
		return urbanPoints;
	}

	public void setUrbanPoints(BigDecimal urbanPoints) {
		this.urbanPoints = urbanPoints;
	}

	public BigDecimal getNationalKm() {
		return nationalKm;
	}

	public void setNationalKm(BigDecimal nationalKm) {
		this.nationalKm = nationalKm;
	}

	public BigDecimal getTotalKm() {
		return totalKm;
	}

	public void setTotalKm(BigDecimal totalKm) {
		this.totalKm = totalKm;
	}

	public BigDecimal getNationalPoints() {
		return nationalPoints;
	}

	public void setNationalPoints(BigDecimal nationalPoints) {
		this.nationalPoints = nationalPoints;
	}

	public Boolean getHomeWorkPath() {
		return isHomeWorkPath;
	}

	public void setHomeWorkPath(Boolean homeWorkPath) {
		isHomeWorkPath = homeWorkPath;
	}

	public BigDecimal getEuro() {
		return euro;
	}

	public void setEuro(BigDecimal euro) {
		this.euro = euro;
	}

	public Double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(Double multiplier) {
		this.multiplier = multiplier;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getStartBattery() {
		return startBattery;
	}

	public void setStartBattery(Double startBattery) {
		this.startBattery = startBattery;
	}

	public Double getEndBattery() {
		return endBattery;
	}

	public void setEndBattery(Double endBattery) {
		this.endBattery = endBattery;
	}

	public List<Partial> getPartials() {
		return partials;
	}

	public void setPartials(List<Partial> partials) {
		this.partials = partials;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Boolean getCertificated() {
		return certificated;
	}

	public void setCertificated(Boolean certificate) {
		this.certificated = certificate;
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

	public HomeAddress getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(HomeAddress homeAddress) {
		this.homeAddress = homeAddress;
	}

	public WorkAddress getWorkAddress() {
		return workAddress;
	}

	public void setWorkAddress(WorkAddress workAddress) {
		this.workAddress = workAddress;
	}

	public Integer getPhoneStartBattery() {
		return phoneStartBattery;
	}

	public void setPhoneStartBattery(Integer phoneStartBattery) {
		this.phoneStartBattery = phoneStartBattery;
	}

	public Integer getPhoneEndBattery() {
		return phoneEndBattery;
	}

	public void setPhoneEndBattery(Integer phoneEndBattery) {
		this.phoneEndBattery = phoneEndBattery;
	}

	public Double getCo2() {
		return co2;
	}

	public void setCo2(Double co2) {
		this.co2 = co2;
	}

	public List<SessionPoint> getSessionPoints() {
		return sessionPoints;
	}

	public void setSessionPoints(List<SessionPoint> sessionPoints) {
		this.sessionPoints = sessionPoints;
	}

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getFirmware() {
		return firmware;
	}

	public void setFirmware(String firmware) {
		this.firmware = firmware;
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

	public String getOldSessionId() {
		return oldSessionId;
	}

	public void setOldSessionId(String oldSessionId) {
		this.oldSessionId = oldSessionId;
	}

	public LocalDateTime getValidatedDate() {
		return validatedDate;
	}

	public void setValidatedDate(LocalDateTime validatedDate) {
		this.validatedDate = validatedDate;
	}

	public String getPhoneModel() {
		return phoneModel;
	}

	public void setPhoneModel(String phoneModel) {
		this.phoneModel = phoneModel;
	}

	public Boolean getVerificationRequired() {
		return verificationRequired;
	}

	public void setVerificationRequired(Boolean verificationRequired) {
		this.verificationRequired = verificationRequired;
	}

	public String getVerificationRequiredNote() {
		return verificationRequiredNote;
	}

	public void setVerificationRequiredNote(String verificationRequiredNote) {
		this.verificationRequiredNote = verificationRequiredNote;
	}

	public LocalDateTime getForwardedAt() {
		return forwardedAt;
	}

	public void setForwardedAt(LocalDateTime forwardedAt) {
		this.forwardedAt = forwardedAt;
	}
}
