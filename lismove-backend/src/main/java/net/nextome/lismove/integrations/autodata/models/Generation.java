package net.nextome.lismove.integrations.autodata.models;

public class Generation {
	private Long id;
	private String name;
	private Integer modelYear;
	private Modifications modifications;

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

	public Modifications getModifications() {
		return modifications;
	}

	public void setModifications(Modifications modifications) {
		this.modifications = modifications;
	}
}
