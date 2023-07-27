package net.nextome.lismove.rest.dto;

public class CarGenerationDto {
	private Long id;
	private String name;
	private Integer modelYear;
	private Long model;

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

	public Integer getModelYear() {
		return modelYear;
	}

	public void setModelYear(Integer modelYear) {
		this.modelYear = modelYear;
	}

	public Long getModel() {
		return model;
	}

	public void setModel(Long model) {
		this.model = model;
	}
}
