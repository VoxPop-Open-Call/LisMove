package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserOverviewDto {
	private String uid;
	private String email;
	private String phoneNumber;
	private String username;
	private String firstName;
	private String lastName;
	private String fiscalCode;
	private Long lastLoggedIn;
	//Address
	private List<AddressOverviewDto> homeAddresses;
	private List<AddressOverviewDto> workAddresses;

	//SmartPhone
	private String activePhone;
	private String appVersion;
	//Sensor
	private List<SensorDto> sensors;

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

	public String getFiscalCode() {
		return fiscalCode;
	}

	public void setFiscalCode(String fiscalCode) {
		this.fiscalCode = fiscalCode;
	}

	public Long getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(Long lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	public List<AddressOverviewDto> getHomeAddresses() {
		return homeAddresses;
	}

	public void setHomeAddresses(List<AddressOverviewDto> homeAddresses) {
		this.homeAddresses = homeAddresses;
	}

	public List<AddressOverviewDto> getWorkAddresses() {
		return workAddresses;
	}

	public void setWorkAddresses(List<AddressOverviewDto> workAddresses) {
		this.workAddresses = workAddresses;
	}

	public String getActivePhone() {
		return activePhone;
	}

	public void setActivePhone(String activePhone) {
		this.activePhone = activePhone;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public List<SensorDto> getSensors() {
		return sensors;
	}

	public void setSensors(List<SensorDto> sensors) {
		this.sensors = sensors;
	}
}
