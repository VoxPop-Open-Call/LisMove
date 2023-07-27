package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationMessageDeliveryDto {

	private Long message;
	private String title;
	private String body;
	private String imageUrl;
	private Long createdDate;
	private Long organization;
	private Boolean sent;
	private Boolean read;
	private String uid;
	private String username;

	public Long getMessage() {
		return message;
	}

	public void setMessage(Long message) {
		this.message = message;
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

	public Boolean getSent() {
		return sent;
	}

	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Long createdDate) {
		this.createdDate = createdDate;
	}
}
