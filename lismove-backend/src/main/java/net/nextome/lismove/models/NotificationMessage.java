package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "notification_messages")
public class NotificationMessage extends AuditableEntity {
	@Id
	@SequenceGenerator(name = "notificationssseq", sequenceName = "notifications_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "notificationsseq")
	private Long id;

	private String title;
	private String body;
	private String imageUrl;

	@ManyToOne
	@JoinColumn(name = "organization")
	private Organization organization;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization org) {
		this.organization = org;
	}
}
