package net.nextome.lismove.rest.dto;

public class NotificationMessageReceiverDto {

	private Long id;
	private Boolean read;
	private String uid;
	private String username;

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
}
