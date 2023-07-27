package net.nextome.lismove.rest.dto;

import java.util.List;

public class NotificationMessageDto {

	private Long id;
	private String title;
	private String body;
	private String imageUrl;
	private Long organization;
	private Long createdDate;
	private List<NotificationMessageReceiverDto> receivers;

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

	public Long getOrganization() {
		return organization;
	}

	public void setOrganization(Long organization) {
		this.organization = organization;
	}

	public Long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Long createdDate) {
		this.createdDate = createdDate;
	}

	public List<NotificationMessageReceiverDto> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<NotificationMessageReceiverDto> receivers) {
		this.receivers = receivers;
	}
}
