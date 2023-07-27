package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SessionOverviewDto {

	private UUID id;
	private Integer type;
	private Boolean valid;
	private Integer status;
	private Boolean certificated;
	private Long startTime;
	private Long endTime;
	private Integer duration;
	private List<String> polyline;
	private List<String> rawPolyline;
	private List<String> gmapsPolyline;
	private BigDecimal gyroDistance;
	private BigDecimal gpsDistance;
	private BigDecimal gmapsDistance;
	private BigDecimal nationalKm;
	private BigDecimal nationalPoints;
	private BigDecimal urbanPoints;
	private Boolean isHomeWorkPath;
	private BigDecimal euro;
	private Double multiplier;
	private String description;
	private Double startBattery;
	private Double endBattery;
	private String bikeType;
	private BigDecimal wheelDiameter;
	private Integer phoneStartBattery;
	private Integer phoneEndBattery;
	private Double co2;
	private List<SessionPointDto> sessionPoints;
	//Addresses
	private String homeAddress;
	private String homeNumber;
	private Long homeCity;
	private Double homeAddressLng;
	private Double homeAddressLat;
	private String workAddress;
	private String workNumber;
	private Long workCity;
	private Double workAddressLng;
	private Double workAddressLat;
	//User
	private String uid;
	private String email;
	private String phoneNumber;
	private String username;
	private String firstName;
	private String lastName;
	private Double totalRank;
	private Double currentRank;
	private Long lastLoggedIn;
	//Sensor
	private String sensor;
	private String sensorName;
	private String firmware;
	//Smartphone
	private String appVersion;
	private String platform;
	private String phoneModel;
	//Verifica
	private Boolean verificationRequired;
	private String verificationRequiredNote;
	private Long forwardedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Boolean getCertificated() {
		return certificated;
	}

	public void setCertificated(Boolean certificated) {
		this.certificated = certificated;
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

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
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

	public BigDecimal getNationalKm() {
		return nationalKm;
	}

	public void setNationalKm(BigDecimal nationalKm) {
		this.nationalKm = nationalKm;
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

	public List<String> getPolyline() {
		return polyline;
	}

	public void setPolyline(List<String> polyline) {
		this.polyline = polyline;
	}

	public List<String> getRawPolyline() {
		return rawPolyline;
	}

	public void setRawPolyline(List<String> rawPolyline) {
		this.rawPolyline = rawPolyline;
	}

	public List<String> getGmapsPolyline() {
		return gmapsPolyline;
	}

	public void setGmapsPolyline(List<String> gmapsPolyline) {
		this.gmapsPolyline = gmapsPolyline;
	}

	public BigDecimal getGmapsDistance() {
		return gmapsDistance;
	}

	public void setGmapsDistance(BigDecimal gmapsDistance) {
		this.gmapsDistance = gmapsDistance;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}

	public String getHomeNumber() {
		return homeNumber;
	}

	public void setHomeNumber(String homeNumber) {
		this.homeNumber = homeNumber;
	}

	public Long getHomeCity() {
		return homeCity;
	}

	public void setHomeCity(Long homeCity) {
		this.homeCity = homeCity;
	}

	public Double getHomeAddressLng() {
		return homeAddressLng;
	}

	public void setHomeAddressLng(Double homeAddressLng) {
		this.homeAddressLng = homeAddressLng;
	}

	public Double getHomeAddressLat() {
		return homeAddressLat;
	}

	public void setHomeAddressLat(Double homeAddressLat) {
		this.homeAddressLat = homeAddressLat;
	}

	public String getWorkAddress() {
		return workAddress;
	}

	public void setWorkAddress(String workAddress) {
		this.workAddress = workAddress;
	}

	public String getWorkNumber() {
		return workNumber;
	}

	public void setWorkNumber(String workNumber) {
		this.workNumber = workNumber;
	}

	public Long getWorkCity() {
		return workCity;
	}

	public void setWorkCity(Long workCity) {
		this.workCity = workCity;
	}

	public Double getWorkAddressLng() {
		return workAddressLng;
	}

	public void setWorkAddressLng(Double workAddressLng) {
		this.workAddressLng = workAddressLng;
	}

	public Double getWorkAddressLat() {
		return workAddressLat;
	}

	public void setWorkAddressLat(Double workAddressLat) {
		this.workAddressLat = workAddressLat;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Double getTotalRank() {
		return totalRank;
	}

	public void setTotalRank(Double totalRank) {
		this.totalRank = totalRank;
	}

	public Double getCurrentRank() {
		return currentRank;
	}

	public void setCurrentRank(Double currentRank) {
		this.currentRank = currentRank;
	}

	public BigDecimal getNationalPoints() {
		return nationalPoints;
	}

	public void setNationalPoints(BigDecimal nationalPoints) {
		this.nationalPoints = nationalPoints;
	}

	public BigDecimal getUrbanPoints() {
		return urbanPoints;
	}

	public void setUrbanPoints(BigDecimal urbanPoints) {
		this.urbanPoints = urbanPoints;
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

	public List<SessionPointDto> getSessionPoints() {
		return sessionPoints;
	}

	public void setSessionPoints(List<SessionPointDto> sessionPoints) {
		this.sessionPoints = sessionPoints;
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

	public Long getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(Long lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
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

	public Long getForwardedAt() {
		return forwardedAt;
	}

	public void setForwardedAt(Long forwardedAt) {
		this.forwardedAt = forwardedAt;
	}
}
