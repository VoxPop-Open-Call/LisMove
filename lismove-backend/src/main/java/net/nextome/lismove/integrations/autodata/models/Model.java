package net.nextome.lismove.integrations.autodata.models;

public class Model {
	private Long id;
	private String name;
	private Generations generations;

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

	public Generations getGenerations() {
		return generations;
	}

	public void setGenerations(Generations generations) {
		this.generations = generations;
	}
}
