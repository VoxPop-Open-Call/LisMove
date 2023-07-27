package net.nextome.lismove.integrations.autodata.models;

public class Brand {
	private Long id;
	private String name;
	private Models models;

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

	public Models getModels() {
		return models;
	}

	public void setModels(Models models) {
		this.models = models;
	}
}
