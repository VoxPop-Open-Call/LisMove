package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "notification_messages_delivery")
public class NotificationMessageDelivery extends AuditableEntity {
	@Id
	@SequenceGenerator(name = "notifications_d_seq", sequenceName = "notifications_del_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "notifications_d_seq")
	private Long id;
	private Boolean read;

	@ManyToOne
	private User user;

	@ManyToOne
	private NotificationMessage notificationMessage;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public NotificationMessage getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessage(NotificationMessage notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
