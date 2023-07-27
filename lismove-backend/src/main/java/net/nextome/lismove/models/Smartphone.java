package net.nextome.lismove.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "smartphones")
public class Smartphone {

	@Id
	@SequenceGenerator(name = "smartphoneseq", sequenceName = "smartphone_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "smartphoneseq")
	private Long id;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime startAssociation;
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime endAssociation;
	private String imei;
	private String appVersion;
	private String platform;
	private String model;
	private String fcmToken;

	@ManyToOne
	private User user;

	public Smartphone() {
	}

	public Smartphone(String imei, User user) {
		this.imei = imei;
		this.user = user;
		startAssociation = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getStartAssociation() {
		return startAssociation;
	}

	public void setStartAssociation(LocalDateTime associationStart) {
		this.startAssociation = associationStart;
	}

	public LocalDateTime getEndAssociation() {
		return endAssociation;
	}

	public void setEndAssociation(LocalDateTime associationEnd) {
		this.endAssociation = associationEnd;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getFcmToken() {
		return fcmToken;
	}

	public void setFcmToken(String fcmModel) {
		this.fcmToken = fcmModel;
	}
}
