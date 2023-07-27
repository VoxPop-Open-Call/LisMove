package net.nextome.lismove.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import net.nextome.lismove.models.enums.OrganizationType;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "organizations")
public class Organization {

	@Id
	@SequenceGenerator(name = "organizationsseq", sequenceName = "organizations_seq_id", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "organizationsseq")
	private Long id;
	private OrganizationType type; //PA or COMPANY
	private String title;
	private String logo;
	private String initiativeLogo;
	private String notificationLogo;
	private String termsConditions;
	private String code;
	private Boolean validation;
	private String validatorEmail;
	private String regulation;

	@Column(columnDefinition = "varchar")
	private String pageDescription;

	@JsonRawValue
	private String geojson;

	@JsonIgnore
	@OneToMany(mappedBy = "organization")
	private Set<User> managers;

	@JsonIgnore
	@OneToMany(mappedBy = "organization")
	private Set<Enrollment> enrollments;

	@OneToMany(mappedBy = "organization")
	private Set<Seat> seats;

	@ManyToMany
	private Set<City> cities;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OrganizationType getType() {
		return type;
	}

	public void setType(OrganizationType type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getInitiativeLogo() {
		return initiativeLogo;
	}

	public void setInitiativeLogo(String initiativeLogo) {
		this.initiativeLogo = initiativeLogo;
	}

	public String getNotificationLogo() {
		return notificationLogo;
	}

	public void setNotificationLogo(String notificationLogo) {
		this.notificationLogo = notificationLogo;
	}

	public String getTermsConditions() {
		return termsConditions;
	}

	public void setTermsConditions(String termsConditions) {
		this.termsConditions = termsConditions;
	}

	public String getPageDescription() {
		return pageDescription;
	}

	public void setPageDescription(String pageDescription) {
		this.pageDescription = pageDescription;
	}

	public Set<User> getManagers() {
		return managers;
	}

	public void setManagers(Set<User> managers) {
		this.managers = managers;
	}

	public Set<Enrollment> getEnrollments() {
		return enrollments;
	}

	public Set<Seat> getSeats() {
		return seats;
	}

	public void setSeats(Set<Seat> seats) {
		this.seats = seats;
	}

	public void setEnrollments(Set<Enrollment> enrollments) {
		this.enrollments = enrollments;
	}

	public String getGeojson() {
		return geojson;
	}

	public void setGeojson(String geojson) {
		this.geojson = geojson;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getValidation() {
		return validation;
	}

	public void setValidation(Boolean validation) {
		this.validation = validation;
	}

	public String getValidatorEmail() {
		return validatorEmail;
	}

	public void setValidatorEmail(String validatorEmail) {
		this.validatorEmail = validatorEmail;
	}

	public String getRegulation() {
		return regulation;
	}

	public void setRegulation(String regulation) {
		this.regulation = regulation;
	}

	public Set<City> getCities() {
		return cities;
	}

	public void setCities(Set<City> cities) {
		this.cities = cities;
	}
}
