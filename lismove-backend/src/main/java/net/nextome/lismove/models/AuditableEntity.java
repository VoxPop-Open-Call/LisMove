package net.nextome.lismove.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditableEntity {
	@CreatedDate
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdDate;
	@LastModifiedDate
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastModifiedDate;

	public LocalDateTime getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getLastModifiedDate() {
		return this.lastModifiedDate;
	}

	public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
