package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CustomFieldDto {
	private Long id;
	private String name;
	private String description;
	private Integer type;
	private Long organization;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getOrganization() {
		return organization;
	}

	public void setOrganization(Long organization) {
		this.organization = organization;
	}
}
