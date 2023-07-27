package net.nextome.lismove.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import net.nextome.lismove.models.enums.UserType;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

	@Id
	private String uid;

	//dati generali utente
	private UserType userType; //LISMOVER, MANAGER, VENDOR, ADMIN
	private String firstName;
	private String lastName;
	private String fiscalCode;
	private String username;
	private String email;
	private String phoneNumber;
	private String cityId;
	private Boolean enabled;
	private String gender;
	private String avatarUrl;
	private Boolean emailVerified;
	private Boolean signupCompleted;
	private String cityLisMove;
	private LocalDate birthDate;
	@Formula("date_part('year', age(birth_date))")
	private Integer age;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime lastLoggedIn;

	//dati di un lismover
	private Double totalRank;
	private Double currentRank;
	private String scooterModel;
	private String scooterFirmware;
	private String carModel;
	private Boolean activeCarpooling;
	private Integer numberRankingsWon;
	private String iban;
	private Boolean termsAccepted;
	private Boolean marketingTermsAccepted;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal euro;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal totalMoneyEarned;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal totalMoneyRefundHomeWork;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal totalMoneyRefundNotHomeWork;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal points;
	@Column(columnDefinition = "numeric(10,5)")
	private BigDecimal earnedNationalPoints;
	private Integer positionNationalRanking;
	private Integer numberNationalAwardsWon;
	private Integer numberCupsWon;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateLastCupWon;
	private Integer numberReportsMade;
	private Integer numberTracksSaved;
	@Column(name = "reset_password_required")
	private Boolean resetPasswordRequired;
	@Column(name = "old_user_id")
	private Integer oldUserId;
	private String coinWallet;
	@Column(columnDefinition = "numeric(12,7)")
	private BigDecimal coin;
	@ManyToOne()
	@JoinColumn(name = "home_address_id")
	private HomeAddress homeAddress;
	@OneToMany(mappedBy = "user")
	private Set<HomeWorkPath> homeWorkPaths;
	@ManyToOne
	private Organization organization;  //se userType = MANAGER, questa è l'organizzazione di cui è manager
	@ManyToOne
	private CarModification car;

	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private Set<Smartphone> smartphones;

	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private Set<Sensor> sensors;

	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private Set<Session> sessions;

	@JsonIgnore
	@OneToMany(mappedBy = "user")
	private Set<Enrollment> enrollments;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public LocalDateTime getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(LocalDateTime lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
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

	public String getScooterModel() {
		return scooterModel;
	}

	public void setScooterModel(String scooterModel) {
		this.scooterModel = scooterModel;
	}

	public String getScooterFirmware() {
		return scooterFirmware;
	}

	public void setScooterFirmware(String scooterFirmware) {
		this.scooterFirmware = scooterFirmware;
	}

	public String getCarModel() {
		return carModel;
	}

	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}

	public Boolean getActiveCarpooling() {
		return activeCarpooling;
	}

	public void setActiveCarpooling(Boolean activeCarpooling) {
		this.activeCarpooling = activeCarpooling;
	}

	public Integer getNumberRankingsWon() {
		return numberRankingsWon;
	}

	public void setNumberRankingsWon(Integer numberRankingsWon) {
		this.numberRankingsWon = numberRankingsWon;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
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

	public BigDecimal getEuro() {
		return euro;
	}

	public void setEuro(BigDecimal euroWallet) {
		this.euro = euroWallet;
	}

	public BigDecimal getTotalMoneyEarned() {
		return totalMoneyEarned;
	}

	public void setTotalMoneyEarned(BigDecimal totalMoneyEarned) {
		this.totalMoneyEarned = totalMoneyEarned;
	}

	public BigDecimal getTotalMoneyRefundHomeWork() {
		return totalMoneyRefundHomeWork;
	}

	public void setTotalMoneyRefundHomeWork(BigDecimal totalMoneyRefundHomeWork) {
		this.totalMoneyRefundHomeWork = totalMoneyRefundHomeWork;
	}

	public BigDecimal getTotalMoneyRefundNotHomeWork() {
		return totalMoneyRefundNotHomeWork;
	}

	public void setTotalMoneyRefundNotHomeWork(BigDecimal totalMoneyRefundNotHomeWork) {
		this.totalMoneyRefundNotHomeWork = totalMoneyRefundNotHomeWork;
	}

	public BigDecimal getPoints() {
		return points;
	}

	public void setPoints(BigDecimal pointsWallet) {
		this.points = pointsWallet;
	}

	public BigDecimal getEarnedNationalPoints() {
		return earnedNationalPoints;
	}

	public void setEarnedNationalPoints(BigDecimal earnedNationalPoints) {
		this.earnedNationalPoints = earnedNationalPoints;
	}

	public Integer getPositionNationalRanking() {
		return positionNationalRanking;
	}

	public void setPositionNationalRanking(Integer positionNationalRanking) {
		this.positionNationalRanking = positionNationalRanking;
	}

	public Integer getNumberNationalAwardsWon() {
		return numberNationalAwardsWon;
	}

	public void setNumberNationalAwardsWon(Integer numberNationalAwardsWon) {
		this.numberNationalAwardsWon = numberNationalAwardsWon;
	}

	public Integer getNumberCupsWon() {
		return numberCupsWon;
	}

	public void setNumberCupsWon(Integer numberCupsWon) {
		this.numberCupsWon = numberCupsWon;
	}

	public LocalDateTime getDateLastCupWon() {
		return dateLastCupWon;
	}

	public void setDateLastCupWon(LocalDateTime dateLastCupWon) {
		this.dateLastCupWon = dateLastCupWon;
	}

	public Integer getNumberReportsMade() {
		return numberReportsMade;
	}

	public void setNumberReportsMade(Integer numberReportsMade) {
		this.numberReportsMade = numberReportsMade;
	}

	public Integer getNumberTracksSaved() {
		return numberTracksSaved;
	}

	public void setNumberTracksSaved(Integer numberTracksSaved) {
		this.numberTracksSaved = numberTracksSaved;
	}

	public Boolean getResetPasswordRequired() {
		return resetPasswordRequired;
	}

	public void setResetPasswordRequired(Boolean resetPasswordRequired) {
		this.resetPasswordRequired = resetPasswordRequired;
	}

	public Integer getOldUserId() {
		return oldUserId;
	}

	public void setOldUserId(Integer oldUserId) {
		this.oldUserId = oldUserId;
	}

	public HomeAddress getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(HomeAddress homeAddress) {
		this.homeAddress = homeAddress;
	}

	public Set<HomeWorkPath> getHomeWorkPaths() {
		return homeWorkPaths;
	}

	public void setHomeWorkPaths(Set<HomeWorkPath> homeWorkPaths) {
		this.homeWorkPaths = homeWorkPaths;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public CarModification getCar() {
		return car;
	}

	public void setCar(CarModification car) {
		this.car = car;
	}

	public Set<Smartphone> getSmartphones() {
		return smartphones;
	}

	public void setSmartphones(Set<Smartphone> smartphones) {
		this.smartphones = smartphones;
	}

	public Set<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(Set<Sensor> sensors) {
		this.sensors = sensors;
	}

	public Set<Session> getSessions() {
		return sessions;
	}

	public void setSessions(Set<Session> sessions) {
		this.sessions = sessions;
	}

	public Set<Enrollment> getEnrollments() {
		return enrollments;
	}

	public void setEnrollments(Set<Enrollment> enrollments) {
		this.enrollments = enrollments;
	}

	public String getCoinWallet() {
		return coinWallet;
	}

	public void setCoinWallet(String coinWallet) {
		this.coinWallet = coinWallet;
	}

	public BigDecimal getCoin() {
		return coin;
	}

	public void setCoin(BigDecimal coin) {
		this.coin = coin;
	}
}
