package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LismoverUserDto {

	private String uid;
	private Integer userType;
	private String phoneNumber;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String iban;
	//	Home address
	private String homeAddress;
	private String homeNumber;
	private Long homeCity;
	private Double homeLatitude;
	private Double homeLongitude;
	//	Work addresses
	private Set<SeatDto> workAddresses;
	private Long birthDate;
	private Boolean termsAccepted;
	private Boolean marketingTermsAccepted;
	private String gender;
	private String avatarUrl;
	private Boolean emailVerified;
	private Boolean signupCompleted;
	private String cityLisMove;
	private String activePhone;
	private String activePhoneModel;
	private String activePhoneToken;
	private String activePhoneVersion;
	private Long phoneActivationTime;
	private Long lastLoggedIn;
	private String password;
	private Boolean resetPasswordRequired;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
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

	public Double getHomeLatitude() {
		return homeLatitude;
	}

	public void setHomeLatitude(Double homeLatitude) {
		this.homeLatitude = homeLatitude;
	}

	public Double getHomeLongitude() {
		return homeLongitude;
	}

	public void setHomeLongitude(Double homeLongitude) {
		this.homeLongitude = homeLongitude;
	}

	public Set<SeatDto> getWorkAddresses() {
		return workAddresses;
	}

	public void setWorkAddresses(Set<SeatDto> workAddresses) {
		this.workAddresses = workAddresses;
	}

	public Long getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Long birthDate) {
		this.birthDate = birthDate;
	}

	public Boolean getTermsAccepted() {
		return termsAccepted;
	}

	public void setTermsAccepted(Boolean termsAccepted) {
		this.termsAccepted = termsAccepted;
	}

	public Boolean getMarketingTermsAccepted() {
		return marketingTermsAccepted;
	}

	public void setMarketingTermsAccepted(Boolean marketingTermsAccepted) {
		this.marketingTermsAccepted = marketingTermsAccepted;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public Boolean getSignupCompleted() {
		return signupCompleted;
	}

	public void setSignupCompleted(Boolean signupCompleted) {
		this.signupCompleted = signupCompleted;
	}

	public String getCityLisMove() {
		return cityLisMove;
	}

	public void setCityLisMove(String cityLisMove) {
		this.cityLisMove = cityLisMove;
	}

	public String getActivePhone() {
		return activePhone;
	}

	public void setActivePhone(String activePhone) {
		this.activePhone = activePhone;
	}

	public String getActivePhoneModel() {
		return activePhoneModel;
	}

	public void setActivePhoneModel(String activePhoneModel) {
		this.activePhoneModel = activePhoneModel;
	}

	public String getActivePhoneToken() {
		return activePhoneToken;
	}

	public void setActivePhoneToken(String activePhoneToken) {
		this.activePhoneToken = activePhoneToken;
	}

	public String getActivePhoneVersion() {
		return activePhoneVersion;
	}

	public void setActivePhoneVersion(String activePhoneVersion) {
		this.activePhoneVersion = activePhoneVersion;
	}

	public Long getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(Long lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	public Long getPhoneActivationTime() {
		return phoneActivationTime;
	}

	public void setPhoneActivationTime(Long phoneActivationTime) {
		this.phoneActivationTime = phoneActivationTime;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getResetPasswordRequired() {
		return resetPasswordRequired;
	}

	public void setResetPasswordRequired(Boolean resetPasswordRequired) {
		this.resetPasswordRequired = resetPasswordRequired;
	}
}
